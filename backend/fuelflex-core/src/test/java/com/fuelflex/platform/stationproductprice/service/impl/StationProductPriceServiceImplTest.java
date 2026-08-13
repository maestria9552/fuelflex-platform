package com.fuelflex.platform.stationproductprice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fuelflex.platform.common.exception.BusinessException;
import com.fuelflex.platform.common.security.AuthorizationService;
import com.fuelflex.platform.common.service.EntityLookupService;
import com.fuelflex.platform.organization.entity.Organization;
import com.fuelflex.platform.product.entity.Product;
import com.fuelflex.platform.product.entity.ProductUnit;
import com.fuelflex.platform.station.entity.Station;
import com.fuelflex.platform.stationproduct.entity.StationProduct;
import com.fuelflex.platform.stationproduct.repository.StationProductRepository;
import com.fuelflex.platform.stationproductprice.dto.request.StationProductPriceRequest;
import com.fuelflex.platform.stationproductprice.dto.request.StationProductPriceUpdateRequest;
import com.fuelflex.platform.stationproductprice.entity.StationProductPrice;
import com.fuelflex.platform.stationproductprice.mapper.StationProductPriceMapper;
import com.fuelflex.platform.stationproductprice.repository.StationProductPriceRepository;
import com.fuelflex.platform.tariffcategory.entity.TariffCategory;
import com.fuelflex.platform.tariffcategory.repository.TariffCategoryRepository;

@ExtendWith(MockitoExtension.class)
class StationProductPriceServiceImplTest {
    @Mock private StationProductPriceRepository repository;
    @Mock private StationProductRepository stationProductRepository;
    @Mock private TariffCategoryRepository tariffCategoryRepository;
    @Mock private EntityLookupService entityLookupService;
    @Mock private AuthorizationService authorizationService;

    private StationProductPriceServiceImpl service;
    private UUID organizationId;
    private UUID stationId;
    private UUID stationProductId;
    private UUID categoryId;
    private Organization organization;
    private Station station;
    private StationProduct stationProduct;
    private TariffCategory category;

    @BeforeEach
    void setUp() {
        service = new StationProductPriceServiceImpl(repository, stationProductRepository,
                tariffCategoryRepository, new StationProductPriceMapper(), entityLookupService,
                authorizationService);
        organizationId = UUID.randomUUID();
        stationId = UUID.randomUUID();
        stationProductId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        organization = new Organization();
        organization.setId(organizationId);
        station = Station.builder().id(stationId).organization(organization).build();
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setOrganization(organization);
        product.setCode("ESS");
        product.setName("Essence");
        product.setShortName("Ess");
        product.setUnit(ProductUnit.LITRE);
        stationProduct = StationProduct.builder().id(stationProductId).station(station)
                .product(product).active(true).displayOrder(1).build();
        category = TariffCategory.builder().id(categoryId).organization(organization).code("CASH")
                .name("Cash").system(true).active(true).displayOrder(1).build();
    }

    @Test
    void createsValidPriceDefaultingToActive() {
        arrangeScope();
        when(tariffCategoryRepository.findByIdAndOrganizationId(categoryId, organizationId))
                .thenReturn(Optional.of(category));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(organizationId, stationId, stationProductId,
                createRequest(categoryId, "3400.125"));

        assertThat(response.getPrice()).isEqualByComparingTo("3400.125");
        assertThat(response.isActive()).isTrue();
        assertThat(response.getTariffCategoryCode()).isEqualTo("CASH");
        assertThat(response.getOrganizationId()).isEqualTo(organizationId);
    }

    @Test
    void createRequestDoesNotExposeActive() {
        assertThat(StationProductPriceRequest.class.getDeclaredFields())
                .extracting(Field::getName)
                .doesNotContain("active");
    }

    @Test
    void acceptsSystemAndCustomCategoriesWithoutCodeBranches() {
        arrangeScope();
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        for (String code : List.of("CASH", "CREDIT", "INTERNAL", "VIP")) {
            TariffCategory current = TariffCategory.builder().id(UUID.randomUUID()).organization(organization)
                    .code(code).name(code).system(!code.equals("VIP")).active(true).displayOrder(1).build();
            when(tariffCategoryRepository.findByIdAndOrganizationId(current.getId(), organizationId))
                    .thenReturn(Optional.of(current));
            assertThat(service.create(organizationId, stationId, stationProductId,
                    createRequest(current.getId(), "100")).getTariffCategoryCode()).isEqualTo(code);
        }
    }

    @Test
    void rejectsZeroNegativeAndNullPrices() {
        arrangeScope();
        when(tariffCategoryRepository.findByIdAndOrganizationId(categoryId, organizationId))
                .thenReturn(Optional.of(category));
        for (String value : List.of("0", "-1")) {
            assertThatThrownBy(() -> service.create(organizationId, stationId, stationProductId,
                    createRequest(categoryId, value)))
                    .isInstanceOf(BusinessException.class).hasMessageContaining("supérieur à zéro");
        }
        StationProductPriceRequest request = createRequest(categoryId, "1");
        request.setPrice(null);
        assertThatThrownBy(() -> service.create(organizationId, stationId, stationProductId, request))
                .isInstanceOf(BusinessException.class).hasMessageContaining("supérieur à zéro");
    }

    @Test
    void rejectsInactiveStationProductAndInactiveCategory() {
        arrangeScope();
        stationProduct.setActive(false);
        assertThatThrownBy(() -> service.create(organizationId, stationId, stationProductId,
                createRequest(categoryId, "1"))).hasMessageContaining("produit inactif");

        stationProduct.setActive(true);
        category.setActive(false);
        when(tariffCategoryRepository.findByIdAndOrganizationId(categoryId, organizationId))
                .thenReturn(Optional.of(category));
        assertThatThrownBy(() -> service.create(organizationId, stationId, stationProductId,
                createRequest(categoryId, "1"))).hasMessageContaining("catégorie tarifaire inactive");
    }

    @Test
    void rejectsCategoryFromAnotherOrganization() {
        arrangeScope();
        Organization other = new Organization();
        other.setId(UUID.randomUUID());
        category.setOrganization(other);
        when(tariffCategoryRepository.findByIdAndOrganizationId(categoryId, organizationId))
                .thenReturn(Optional.of(category));

        assertThatThrownBy(() -> service.create(organizationId, stationId, stationProductId,
                createRequest(categoryId, "1"))).hasMessageContaining("même organisation");
    }

    @Test
    void rejectsActiveDuplicate() {
        arrangeCreate();
        when(repository.findByStationProductIdAndTariffCategoryId(stationProductId, categoryId))
                .thenReturn(Optional.of(price(true)));

        assertThatThrownBy(() -> service.create(organizationId, stationId, stationProductId,
                createRequest(categoryId, "2"))).hasMessageContaining("existe déjà");
    }

    @Test
    void sameCategoryIsAllowedForAnotherStationProduct() {
        arrangeCreate();
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.create(organizationId, stationId, stationProductId, createRequest(categoryId, "1"));

        verify(repository).findByStationProductIdAndTariffCategoryId(stationProductId, categoryId);
        verify(repository).save(any());
    }

    @Test
    void findAllAndFindActiveUseScopedOrderedQueries() {
        arrangeScope();
        when(repository.findByStationProductIdOrderByTariffCategoryDisplayOrderAsc(stationProductId))
                .thenReturn(List.of(price(true)));
        when(repository.findByStationProductIdAndActiveTrueOrderByTariffCategoryDisplayOrderAsc(stationProductId))
                .thenReturn(List.of(price(true)));

        assertThat(service.findAllByStationProduct(organizationId, stationId, stationProductId)).hasSize(1);
        assertThat(service.findActiveByStationProduct(organizationId, stationId, stationProductId)).hasSize(1);
    }

    @Test
    void updateChangesPriceWithoutChangingCategoryOrActiveState() {
        arrangeScope();
        UUID priceId = UUID.randomUUID();
        StationProductPrice entity = price(true);
        when(repository.findByIdAndStationProductId(priceId, stationProductId)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        StationProductPriceUpdateRequest request = updateRequest("3500", null);

        var response = service.update(organizationId, stationId, stationProductId, priceId, request);

        assertThat(response.getPrice()).isEqualByComparingTo("3500");
        assertThat(response.isActive()).isTrue();
        assertThat(entity.getTariffCategory()).isSameAs(category);
    }

    @Test
    void updateExplicitlyDeactivatesAndReactivatesTheSamePrice() {
        arrangeScope();
        UUID priceId = UUID.randomUUID();
        StationProductPrice entity = price(true);
        when(repository.findByIdAndStationProductId(priceId, stationProductId)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        assertThat(service.update(organizationId, stationId, stationProductId, priceId,
                updateRequest("3400", false)).isActive()).isFalse();
        assertThat(service.update(organizationId, stationId, stationProductId, priceId,
                updateRequest("3450", true)).isActive()).isTrue();
        assertThat(entity.getId()).isNotNull();
    }

    @Test
    void deleteIsLogicalAndRejectsAlreadyInactive() {
        arrangeScope();
        UUID priceId = UUID.randomUUID();
        StationProductPrice entity = price(true);
        when(repository.findByIdAndStationProductId(priceId, stationProductId)).thenReturn(Optional.of(entity));

        service.delete(organizationId, stationId, stationProductId, priceId);

        assertThat(entity.isActive()).isFalse();
        verify(repository).save(entity);
        assertThatThrownBy(() -> service.delete(organizationId, stationId, stationProductId, priceId))
                .hasMessageContaining("déjà désactivé");
        verify(repository, never()).delete(any());
    }

    @Test
    void postReactivatesExistingRowWithNewPrice() {
        arrangeCreate();
        StationProductPrice entity = price(false);
        when(repository.findByStationProductIdAndTariffCategoryId(stationProductId, categoryId))
                .thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        var response = service.create(organizationId, stationId, stationProductId,
                createRequest(categoryId, "3600"));

        assertThat(response.isActive()).isTrue();
        assertThat(response.getPrice()).isEqualByComparingTo("3600");
        assertThat(response.getId()).isEqualTo(entity.getId());
    }

    @Test
    void organizationAndStationProductScopesAreEnforced() {
        doThrow(new BusinessException("Accès refusé")).when(authorizationService)
                .checkOrganizationAccess(organizationId);
        assertThatThrownBy(() -> service.findAllByStationProduct(organizationId, stationId, stationProductId))
                .hasMessage("Accès refusé");
        verifyNoInteractions(repository);

        reset(authorizationService);
        when(entityLookupService.findStation(organizationId, stationId)).thenReturn(station);
        when(stationProductRepository.findByIdAndStationId(stationProductId, stationId))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findAllByStationProduct(organizationId, stationId, stationProductId))
                .hasMessageContaining("introuvable dans cette station");
    }

    private void arrangeScope() {
        when(entityLookupService.findStation(organizationId, stationId)).thenReturn(station);
        when(stationProductRepository.findByIdAndStationId(stationProductId, stationId))
                .thenReturn(Optional.of(stationProduct));
    }

    private void arrangeCreate() {
        arrangeScope();
        when(tariffCategoryRepository.findByIdAndOrganizationId(categoryId, organizationId))
                .thenReturn(Optional.of(category));
    }

    private StationProductPriceRequest createRequest(UUID requestedCategoryId, String value) {
        StationProductPriceRequest request = new StationProductPriceRequest();
        request.setTariffCategoryId(requestedCategoryId);
        request.setPrice(new BigDecimal(value));
        return request;
    }

    private StationProductPriceUpdateRequest updateRequest(String value, Boolean active) {
        StationProductPriceUpdateRequest request = new StationProductPriceUpdateRequest();
        request.setPrice(new BigDecimal(value));
        request.setActive(active);
        return request;
    }

    private StationProductPrice price(boolean active) {
        return StationProductPrice.builder().id(UUID.randomUUID()).stationProduct(stationProduct)
                .tariffCategory(category).price(new BigDecimal("3400")).active(active).build();
    }
}
