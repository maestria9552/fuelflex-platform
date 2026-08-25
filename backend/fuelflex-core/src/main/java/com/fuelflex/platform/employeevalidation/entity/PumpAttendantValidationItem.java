package com.fuelflex.platform.employeevalidation.entity;

import java.time.LocalDate;
import java.util.UUID;

import com.fuelflex.platform.user.entity.User;
import com.fuelflex.platform.user.model.Gender;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "pump_attendant_validation_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pump_validation_item_request_employee",
                        columnNames = {"request_id", "pump_attendant_id"}
                ),
                @UniqueConstraint(
                        name = "uk_pump_validation_item_employee",
                        columnNames = "pump_attendant_id"
                )
        },
        indexes = @Index(
                name = "idx_pump_validation_item_request",
                columnList = "request_id"
        )
)
@Getter
@Setter
@NoArgsConstructor
public class PumpAttendantValidationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "request_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pump_validation_item_request")
    )
    private PumpAttendantValidationRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "pump_attendant_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pump_validation_item_employee")
    )
    private User pumpAttendant;

    @Column(name = "first_name_snapshot", nullable = false, length = 100)
    private String firstNameSnapshot;

    @Column(name = "last_name_snapshot", nullable = false, length = 100)
    private String lastNameSnapshot;

    @Column(name = "post_name_snapshot", length = 100)
    private String postNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_snapshot", length = 20)
    private Gender genderSnapshot;

    @Column(name = "birth_place_snapshot", length = 150)
    private String birthPlaceSnapshot;

    @Column(name = "birth_date_snapshot")
    private LocalDate birthDateSnapshot;

    @Column(name = "address_snapshot", length = 500)
    private String addressSnapshot;

    @Column(name = "email_snapshot", nullable = false, length = 180)
    private String emailSnapshot;

    @Column(name = "phone_number_snapshot", nullable = false, length = 30)
    private String phoneNumberSnapshot;

    @Column(name = "operational_code_snapshot", nullable = false, length = 20)
    private String operationalCodeSnapshot;

    public void refreshSnapshot() {
        firstNameSnapshot = pumpAttendant.getFirstName();
        lastNameSnapshot = pumpAttendant.getLastName();
        postNameSnapshot = pumpAttendant.getPostName();
        genderSnapshot = pumpAttendant.getGender();
        birthPlaceSnapshot = pumpAttendant.getBirthPlace();
        birthDateSnapshot = pumpAttendant.getBirthDate();
        addressSnapshot = pumpAttendant.getAddress();
        emailSnapshot = pumpAttendant.getEmail();
        phoneNumberSnapshot = pumpAttendant.getPhoneNumber();
        operationalCodeSnapshot = pumpAttendant.getOperationalCode();
    }
}
