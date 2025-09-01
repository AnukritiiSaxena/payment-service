package com.example.paymentservice.service;

import com.example.paymentservice.event.OrderCreatedEvent;
import com.example.paymentservice.event.PaymentFailedEvent;
import com.example.paymentservice.event.PaymentProcessedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Retryable(
            maxAttempts = 3,
            backoff = @org.springframework.retry.annotation.Backoff(delay = 2000),
            include = RuntimeException.class
    )
    public void processPayment(OrderCreatedEvent event) throws Exception {
        String orderId = event.getOrderId().toLowerCase();

        System.out.println("Processing payment for Order ID: " + event.getOrderId());
        System.out.println("Attempting payment for order: " + event.getOrderId() + " at " + java.time.LocalTime.now());

        // Simulate processing delay
        Thread.sleep(3000);

        if (orderId.contains("success")) {
            sendSuccessEvent(event.getOrderId());
            return;
        }

        if (orderId.contains("fail")) {
            System.out.println("Simulating immediate failure for order: " + event.getOrderId());
            throw new RuntimeException("Immediate failure for order " + event.getOrderId());
        }

        if (orderId.contains("retry")) {
            System.out.println("Simulating retry scenario for order: " + event.getOrderId());
            throw new RuntimeException("Simulated retryable failure for order " + event.getOrderId());
        }

        // Default behavior (success)
        sendSuccessEvent(event.getOrderId());
    }

    @Recover
    public void recover(RuntimeException ex, OrderCreatedEvent event) {
        System.err.println("All retries failed for order: " + event.getOrderId());
        publishFailureEvent(event.getOrderId());
    }

    private void sendSuccessEvent(String orderId) {
        PaymentProcessedEvent processedEvent = new PaymentProcessedEvent(orderId);
        kafkaTemplate.send("payment-processed", toJson(processedEvent));
        System.out.println("✅ Payment successful for order " + orderId);
    }

    public void publishFailureEvent(String orderId) {
        PaymentFailedEvent failedEvent = new PaymentFailedEvent(orderId);
        kafkaTemplate.send("payment-failed", toJson(failedEvent));
        System.out.println("❌ Published PaymentFailedEvent for order " + orderId);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
