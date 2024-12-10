package com.anastasiaeverstova.myeduserver.yookassa.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;

@FieldDefaults(level = AccessLevel.PUBLIC)
public class Amount {
    BigDecimal value;
    String currency;

    @JsonCreator
    public Amount(@JsonProperty("value") BigDecimal value, @JsonProperty("currency") String currency) {
        this.value = value;
        this.currency = currency;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
