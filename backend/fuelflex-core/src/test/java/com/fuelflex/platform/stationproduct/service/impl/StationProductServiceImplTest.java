package com.fuelflex.platform.stationproduct.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
import com.fuelflex.platform.stationproduct.dto.request.StationProductRequest;
import com.fuelflex.platform.stationproduct.entity.StationProduct;
import com.fuelflex.platform.stationproduct.mapper.StationProductMapper;
import com.fuelflex.platform.stationproduct.repository.StationProductRepository;
import com.fuelflex.platform.tank.repository.TankRepository;

@ExtendWith(MockitoExtension.class)
class StationProductServiceImplTest {
    @Mock private StationProductRepository repository;
    @Mock private EntityLookupService lookupService;
    @Mock private AuthorizationService authorizationService;
    @Mock private TankRepository tankRepository;

    private StationProductServiceImpl service;
    private UUID organizationId;
    private UUID stationId;
    private UUID productId;
    private Station station;
    private Product product;

    @BeforeEach
    void setUp() {
        service = new StationProductServiceImpl(repository, new StationProductMapper(), lookupService,
                authorizationService, tankRepository);
        organizationId = UUID.randomUUID();
        stationId = UUID.randomUUID();
        productId = UUID.randomUUID();
        Organization organization = new Organization();
        organization.setId(organizationId);
        station = new Station();
        station.setId(stationId);
        station.setOrganization(organization);
        product = new Product();
        product.setId(productId);
        product.setOrganization(organization);
        product.setCode("GO");
        product.setName("Gasoil");
        product.setUnit(ProductUnit.LITRE);
    }

    @Test
    void createValidDefaultsToActiveAndDisplayOrderOne() {
        arrangeLookups();
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(organizationId, stationId, request(null, null));

        assertThat(response.isActive()).isTrue();
        assertThat(response.getDisplayOrder()).isEqualTo(1);
        assertThat(response.getProductId()).isEqualTo(productId);
    }

    @Test
    void createPreservesExplicitInactive() {
        arrangeLookups();
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.create(organizationId, stationId, request(3, false)).isActive()).isFalse();
    }

    @Test
    void createRejectsProductFromAnotherOrganization() {
        arrangeLookups();
        Organization other = new Organization();
        other.setId(UUID.randomUUID());
        product.setOrganization(other);

        assertThatThrownBy(() -> service.create(organizationId, stationId, request(null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("même organisation");
        verify(repository, never()).save(any());
    }

    @Test
    void createRejectsStationOutsideOrganizationScope() {
        doThrow(new BusinessException("Station introuvable dans cette organisation."))
                .when(lookupService).findStation(organizationId, stationId);

        assertThatThrownBy(() -> service.create(organizationId, stationId, request(null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Station introuvable");
        verify(lookupService, never()).findProduct(any(), any());
    }

    @Test
    void createRejectsDuplicate() {
        arrangeLookups();
        when(repository.existsByStationIdAndProductId(stationId, productId)).thenReturn(true);

        assertThatThrownBy(() -> service.create(organizationId, stationId, request(null, null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("déjà associé");
    }

    @Test
    void findAllAndFindActiveUseScopedQueries() {
        arrangeStationScope();
        StationProduct entity = entity(true);
        when(repository.findByStationIdOrderByDisplayOrderAsc(stationId)).thenReturn(List.of(entity));
        when(repository.findByStationIdAndActiveTrueOrderByDisplayOrderAsc(stationId)).thenReturn(List.of(entity));

        assertThat(service.findAllByStation(organizationId, stationId)).hasSize(1);
        assertThat(service.findActiveByStation(organizationId, stationId)).hasSize(1);
    }

    @Test
    void updateChangesDisplayOrderButRejectsProductChange() {
        arrangeStationScope();
        UUID id = UUID.randomUUID();
        StationProduct entity = entity(true);
        when(repository.findByIdAndStationId(id, stationId)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        assertThat(service.update(organizationId, stationId, id, request(4, true)).getDisplayOrder()).isEqualTo(4);

        StationProductRequest changed = request(4, true);
        changed.setProductId(UUID.randomUUID());
        assertThatThrownBy(() -> service.update(organizationId, stationId, id, changed))
                .isInstanceOf(BusinessException.class).hasMessageContaining("ne peut pas être modifié");
    }

    @Test
    void deleteIsLogical() {
        arrangeStationScope();
        UUID id = UUID.randomUUID();
        StationProduct entity = entity(true);
        when(repository.findByIdAndStationId(id, stationId)).thenReturn(Optional.of(entity));

        service.delete(organizationId, stationId, id);

        assertThat(entity.isActive()).isFalse();
        verify(repository).save(entity);
        verify(repository, never()).delete(any());
    }

    @Test
    void deleteRejectsProductUsedByTank() {
        arrangeStationScope();
        UUID id = UUID.randomUUID();
        StationProduct entity = entity(true);
        when(repository.findByIdAndStationId(id, stationId)).thenReturn(Optional.of(entity));
        when(tankRepository.existsByDepotStationIdAndProductId(stationId, productId)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(organizationId, stationId, id))
                .isInstanceOf(BusinessException.class).hasMessageContaining("citerne");
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    void updateSupportsReactivation() {
        arrangeStationScope();
        UUID id = UUID.randomUUID();
        StationProduct entity = entity(false);
        when(repository.findByIdAndStationId(id, stationId)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        assertThat(service.update(organizationId, stationId, id, request(1, true)).isActive()).isTrue();
    }

    @Test
    void everyReadChecksOrganizationAccess() {
        doThrow(new BusinessException("Accès refusé")).when(authorizationService)
                .checkOrganizationAccess(organizationId);

        assertThatThrownBy(() -> service.findAllByStation(organizationId, stationId))
                .isInstanceOf(BusinessException.class).hasMessage("Accès refusé");
        verifyNoInteractions(repository);
    }

    private void arrangeLookups() {
        arrangeStationScope();
        when(lookupService.findProduct(organizationId, productId)).thenReturn(product);
    }

    private void arrangeStationScope() {
        when(lookupService.findStation(organizationId, stationId)).thenReturn(station);
    }

    private StationProductRequest request(Integer displayOrder, Boolean active) {
        StationProductRequest request = new StationProductRequest();
        request.setProductId(productId);
        request.setDisplayOrder(displayOrder);
        request.setActive(active);
        return request;
    }

    private StationProduct entity(boolean active) {
        return StationProduct.builder().id(UUID.randomUUID()).station(station).product(product)
                .displayOrder(1).active(active).build();
    }
}
