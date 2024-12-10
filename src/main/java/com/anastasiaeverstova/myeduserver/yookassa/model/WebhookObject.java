package com.anastasiaeverstova.myeduserver.yookassa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@FieldDefaults(level = AccessLevel.PUBLIC)
public class WebhookObject {
    String id;
    String status;
    Amount amount;
    Amount income_amount;
    String description;
    Recipient recipient;
    PaymentMethod payment_method;
    String captured_at;
    String created_at;
    boolean test;
    Amount refunded_amount;
    boolean paid;
    boolean refundable;
    Metadata metadata;
    AuthorizationDetails authorization_details;
    private WebhookObject paymentMethod;

    public String getId() {
        return id;
    }

    public WebhookObject getPaymentMethod() {
        return paymentMethod;
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
class Recipient {
    String account_id;
    String gateway_id;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Card {
    String first6;
    String last4;
    String expiry_year;
    String expiry_month;
    String card_type;
    CardProduct card_product;
    String issuer_country;
}
@JsonIgnoreProperties(ignoreUnknown = true)
class CardProduct {
    String code;
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Metadata {
}
@JsonIgnoreProperties(ignoreUnknown = true)
class AuthorizationDetails {
    String rrn;
    String auth_code;
    ThreeDSecure three_d_secure;
}
@JsonIgnoreProperties(ignoreUnknown = true)
class ThreeDSecure {
    boolean applied;
    String protocol;
    boolean method_completed;
    boolean challenge_completed;
}
