package com.anastasiaeverstova.myeduserver.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@ToString
@RequiredArgsConstructor
public class SalesDTO {
    private String paymentId;
    private Instant createdAt;
    private BigDecimal amount;
    private Long numOfItems;


    public SalesDTO(String paymentId, Instant createdAt, BigDecimal amount, Long numOfItems) {
        this.paymentId = paymentId;
        this.createdAt = createdAt;
        this.amount = amount;
        this.numOfItems = numOfItems;
    }
}
