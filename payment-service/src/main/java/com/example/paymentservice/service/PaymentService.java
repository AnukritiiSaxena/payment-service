package com.example.paymentservice.service;

import com.example.paymentservice.event.OrderCreatedEvent;
import com.example.paymentservice.event.PaymentFailedEvent;
import com.example.paymentservice.event.PaymentProcessedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class PaymentService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public PaymentService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Retryable(maxAttempts = 3, backoff = @org.springframework.retry.annotation.Backoff(delay = 2000))
    public void processPayment(OrderCreatedEvent event) throws Exception {
        System.out.println("Processing payment for Order ID: " + event.getOrderId());

        // Simulate payment delay
        Thread.sleep(3000);

        // Simulate random success/failure
        boolean success = new Random().nextBoolean();

        if (success) {
            PaymentProcessedEvent processedEvent = new PaymentProcessedEvent(event.getOrderId());
            kafkaTemplate.send("payment-processed", toJson(processedEvent));
            System.out.println("Payment successful for order " + event.getOrderId());
        } else {
            throw new RuntimeException("Payment failed for order " + event.getOrderId());
        }
    }

    private String toJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    public void publishFailureEvent(String orderId) {
        PaymentFailedEvent failedEvent = new PaymentFailedEvent(orderId);
        kafkaTemplate.send("payment-failed", toJson(failedEvent));
        System.out.println("Published PaymentFailedEvent for order " + orderId);
    }
}
