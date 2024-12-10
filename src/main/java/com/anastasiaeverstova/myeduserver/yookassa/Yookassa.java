package com.anastasiaeverstova.myeduserver.yookassa;

import com.anastasiaeverstova.myeduserver.yookassa.implementation.RealYookassa;
import lombok.NonNull;
import com.anastasiaeverstova.myeduserver.yookassa.event.YookassaEvent;
import com.anastasiaeverstova.myeduserver.yookassa.exception.BadRequestException;
import com.anastasiaeverstova.myeduserver.yookassa.exception.UnspecifiedShopInformation;
import com.anastasiaeverstova.myeduserver.yookassa.implementation.RealYookassa;
import com.anastasiaeverstova.myeduserver.yookassa.model.Amount;
import com.anastasiaeverstova.myeduserver.yookassa.model.Payment;
import com.anastasiaeverstova.myeduserver.yookassa.model.Refund;
import com.anastasiaeverstova.myeduserver.yookassa.model.Webhook;
import com.anastasiaeverstova.myeduserver.yookassa.model.collecting.PaymentList;
import com.anastasiaeverstova.myeduserver.yookassa.model.collecting.RefundList;
import com.anastasiaeverstova.myeduserver.yookassa.model.collecting.WebhookList;

import java.io.IOException;
import java.util.UUID;


public interface Yookassa {

    Payment capturePayment(@NonNull UUID paymentId, @NonNull Amount amount, @NonNull String idempotenceKey) throws UnspecifiedShopInformation, BadRequestException, IOException;

    Payment createPayment(@NonNull Amount amount, @NonNull String description, @NonNull String redirectUrl) throws UnspecifiedShopInformation, BadRequestException, IOException;

    // payment types: https://yookassa.ru/developers/payment-acceptance/getting-started/payment-methods#all
    Payment createPayment(@NonNull String type, @NonNull boolean saveMethod, @NonNull Amount amount, @NonNull String description, @NonNull String redirectUrl) throws UnspecifiedShopInformation, BadRequestException, IOException;

    Payment createRecurrentPayment(@NonNull UUID methodId, @NonNull Amount amount, @NonNull String description) throws UnspecifiedShopInformation, BadRequestException, IOException;

    Payment getPayment(@NonNull UUID paymentIdentifier) throws UnspecifiedShopInformation, BadRequestException, IOException;

    // todolimit, filters (status, date, paidDate..)
    PaymentList getPayments() throws UnspecifiedShopInformation, BadRequestException, IOException;

    Refund createRefund(@NonNull UUID paymentIdentifier, @NonNull Amount amount) throws UnspecifiedShopInformation, BadRequestException, IOException;

    Refund getRefund(@NonNull UUID refundIdentifier) throws UnspecifiedShopInformation, BadRequestException, IOException;

    RefundList getRefunds() throws UnspecifiedShopInformation, BadRequestException, IOException;

    Webhook createWebhook(@NonNull YookassaEvent event, @NonNull String url) throws UnspecifiedShopInformation, BadRequestException, IOException;
    void deleteWebhook(@NonNull UUID webhookIdentifier) throws UnspecifiedShopInformation, BadRequestException, IOException;
    WebhookList getWebhooks() throws UnspecifiedShopInformation, BadRequestException, IOException;


    static Yookassa initialize(int shopIdentifier, @NonNull String shopToken) {
        return new RealYookassa(shopIdentifier, shopToken);
    }
}
