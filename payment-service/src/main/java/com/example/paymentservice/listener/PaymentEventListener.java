package com.example.paymentservice.listener;

import com.example.paymentservice.event.OrderCreatedEvent;
import com.example.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventListener {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PaymentEventListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "order-created", groupId = "payment-group")
    public void handleOrderCreated(ConsumerRecord<String, String> record) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(record.value(), OrderCreatedEvent.class);
            paymentService.processPayment(event);
        } catch (Exception e) {
            System.err.println("Retry exhausted or unrecoverable error: " + e.getMessage());
            try {
                OrderCreatedEvent failedEvent = objectMapper.readValue(record.value(), OrderCreatedEvent.class);
                paymentService.publishFailureEvent(failedEvent.getOrderId());
            } catch (Exception ex) {
                System.err.println("Error publishing failure event: " + ex.getMessage());
            }
        }
    }
}
