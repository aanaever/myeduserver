package com.anastasiaeverstova.myeduserver.yookassa.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@FieldDefaults(level = AccessLevel.PUBLIC)
public class Payment {
    UUID id;
    private Integer userId;
    String status;
    boolean paid;
    Amount amount;
    ConfirmationType confirmation;
    String created_at;
    String description;
    JsonElement metadata;
    RecipientType recipient;
    PaymentType payment_method;
    boolean refundable;
    boolean test;
    String redirectUrl;


    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public UUID getId() {
        return id;
    }

    public PaymentType getPaymentMethod() {
        return payment_method;
    }

    public ConfirmationType getConfirmation() {
        return confirmation;
    }

    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PUBLIC)
    public static class PaymentType {
        String type;
        UUID id;
        boolean saved;

        public String getType() {
            return type;
        }
    }

    @FieldDefaults(level = AccessLevel.PUBLIC)
    public static class RecipientType {
        String account_id;
        String gateway_id;
    }

    @FieldDefaults(level = AccessLevel.PUBLIC)
    public static class ConfirmationType {
        String type = "redirect";
        String confirmation_url;

        public String getConfirmationUrl() {
            return confirmation_url;
        }
    }
}
