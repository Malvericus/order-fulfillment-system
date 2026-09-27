package com.practice.alertservice.messaging;

import com.practice.events.OrderRejectedEvent;
import com.practice.events.Topics;
import com.practice.alertservice.service.AlertService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderRejectedListener {

    private final AlertService alertService;

    public OrderRejectedListener(AlertService alertService) {
        this.alertService = alertService;
    }

    @KafkaListener(topics = Topics.ORDER_REJECTED, groupId = "alert-service")
    public void onOrderRejected(OrderRejectedEvent event) {
        alertService.sendOrderRejectedAlert(event);
    }
}
