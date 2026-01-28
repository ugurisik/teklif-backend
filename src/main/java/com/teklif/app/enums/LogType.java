package com.teklif.app.enums;

public enum LogType {
    // Offer logs
    OFFER_CREATED,
    OFFER_UPDATED,
    OFFER_DELETED,
    OFFER_SENT,
    OFFER_VIEWED,
    OFFER_ACCEPTED,
    OFFER_REJECTED,
    OFFER_DUPLICATED,

    // Customer logs
    CUSTOMER_CREATED,
    CUSTOMER_UPDATED,
    CUSTOMER_DELETED,

    // Product logs
    PRODUCT_CREATED,
    PRODUCT_UPDATED,
    PRODUCT_DELETED,

    // User logs
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,
    USER_LOGIN,
    USER_LOGOUT,

    // Tenant logs
    TENANT_CREATED,
    TENANT_UPDATED,
    TENANT_DELETED,

    // System logs
    SYSTEM_ERROR,
    SYSTEM_WARNING,
    SYSTEM_INFO;

    public String getValue(){
        return name();
    }
}
