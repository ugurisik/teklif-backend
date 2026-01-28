package com.teklif.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBasicResponse {
    private String id;
    private String companyName;
    private String contactPerson;
    private String phone;
    private String email;
}