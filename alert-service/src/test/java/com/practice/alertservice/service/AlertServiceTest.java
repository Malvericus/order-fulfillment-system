package com.practice.alertservice.service;

import com.practice.events.OrderRejectedEvent;
import com.practice.events.ShipmentDelayedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AlertServiceTest {

    @Test
    void sendOrderRejectedAlert_sendsEmailMentioningOrderIdAndReason() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        AlertService service = new AlertService(mailSender);

        service.sendOrderRejectedAlert(new OrderRejectedEvent("order-1", "OUT_OF_STOCK", Instant.now()));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getText()).contains("order-1", "OUT_OF_STOCK");
    }

    @Test
    void sendShipmentDelayedAlert_sendsEmailMentioningShipmentIdAndReason() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        AlertService service = new AlertService(mailSender);

        service.sendShipmentDelayedAlert(new ShipmentDelayedEvent("order-2", "ship-1", "carrier-outage", Instant.now()));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).contains("ship-1", "carrier-outage");
    }
}
