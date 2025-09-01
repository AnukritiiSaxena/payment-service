package com.example.paymentservice.event;

public class OrderCreatedEvent {
    private String orderId;
    private double amount;

    // Getters, setters, constructors
    public OrderCreatedEvent() {}

    public OrderCreatedEvent(String orderId, double amount) {
        this.orderId = orderId;
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public double getAmount() {
        return amount;
    }
}

