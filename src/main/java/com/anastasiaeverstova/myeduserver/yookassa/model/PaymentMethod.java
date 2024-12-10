package com.anastasiaeverstova.myeduserver.yookassa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentMethod {
    String type;
    UUID id;
    boolean saved;
    String title;
    Card card;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}
