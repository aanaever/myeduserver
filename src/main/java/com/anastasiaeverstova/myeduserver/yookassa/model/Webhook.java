package com.anastasiaeverstova.myeduserver.yookassa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.UUID;


@FieldDefaults(level = AccessLevel.PUBLIC)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Webhook {
    UUID id;
    String event;
    String url;
    String type;
    private WebhookObject object;

    public String getEvent() {
        return event;
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public WebhookObject getObject() {
        return object;
    }
}

