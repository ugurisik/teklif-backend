package com.teklif.app.dto.response;

import com.teklif.app.enums.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private String id;
    private String tenantId;
    private CustomerType type;
    private String companyName;
    private String firstName;
    private String lastName;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String taxNumber;
    private String taxOffice;
    private String notes;
    private Boolean isActive;
    private Long offerCount;
    private Instant createdAt;
    private Instant updatedAt;
}