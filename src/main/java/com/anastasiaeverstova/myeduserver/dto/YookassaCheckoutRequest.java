package com.anastasiaeverstova.myeduserver.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class YookassaCheckoutRequest {

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @NotNull(message = "Total amount cannot be null")
    @DecimalMin(value = "0.01", message = "Total amount must be at least 0.01")
    private BigDecimal totalAmount;

    @NotBlank(message = "Return URL cannot be empty")
    private String returnURL;

    public YookassaCheckoutRequest() {

    }


}
