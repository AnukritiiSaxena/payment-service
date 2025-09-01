package com.example.paymentservice.event;

public class PaymentProcessedEvent {
    private String orderId;
    private String status = "SUCCESS";

    public PaymentProcessedEvent() {}

    public PaymentProcessedEvent(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }
}
