package com.anastasiaeverstova.myeduserver.yookassa.model;

public class PaymentNotification {
    private String type;
    private String event;
    private Payment object;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Payment getObject() {
        return object;
    }

    public void setObject(Payment object) {
        this.object = object;
    }
}
