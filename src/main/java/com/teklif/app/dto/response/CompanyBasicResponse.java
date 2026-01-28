package com.teklif.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyBasicResponse {
    private String id;
    private String name;
    private String logo;
    private String email;
    private String phone;
    private String address;
    private String template;
    private String primaryColor;
    private String secondaryColor;
}