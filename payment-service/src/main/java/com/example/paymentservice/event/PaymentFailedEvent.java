package com.example.paymentservice.event;

public class PaymentFailedEvent {
    private String orderId;
    private String status = "FAILED";

    public PaymentFailedEvent() {}

    public PaymentFailedEvent(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }
}
