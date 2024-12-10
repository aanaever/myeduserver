package com.anastasiaeverstova.myeduserver.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "sales")
@Getter
@RequiredArgsConstructor
public class Sales {

    @Id
    @Column(name = "transaction_id", nullable = false, length = 36)
    private String transactionId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonBackReference
    private User user;

    @Column(name = "payment_id", nullable = false, length = 36)
    private String paymentId;

    @Column(precision = 6, scale = 2, nullable = false)
    @NotNull
    @Min(1)
    private BigDecimal amount;

    @CreationTimestamp
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(nullable = false)
    private Instant createdAt;


    public Sales(String transactionId, User user, BigDecimal amount) {
        this.transactionId = transactionId;
        this.user = user;
        this.amount = amount;
    }
    public Sales(String transactionId, User user, String paymentId, BigDecimal amount) {
        this.transactionId = transactionId;
        this.user = user;
        this.paymentId = paymentId;
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Sales sales = (Sales) o;
        return transactionId != null && Objects.equals(transactionId, sales.transactionId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

