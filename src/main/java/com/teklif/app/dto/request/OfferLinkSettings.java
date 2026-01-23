package com.teklif.app.dto.request;

import lombok.Data;

@Data
public class OfferLinkSettings {
    private String password;
    private Boolean oneTimeView = false;
}