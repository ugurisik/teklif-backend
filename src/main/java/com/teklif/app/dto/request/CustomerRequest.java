package com.teklif.app.dto.request;

import com.teklif.app.enums.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerRequest {

    @NotNull(message = "Customer type is required")
    private CustomerType type;

    // For COMPANY
    private String companyName;

    // For INDIVIDUAL
    private String firstName;
    private String lastName;

    @NotBlank(message = "Contact person is required")
    private String contactPerson;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private String address;
    private String taxNumber;
    private String taxOffice;
    private String notes;
}