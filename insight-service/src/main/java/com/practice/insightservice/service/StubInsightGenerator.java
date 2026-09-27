package com.practice.insightservice.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Profile("stub")
public class StubInsightGenerator implements InsightGenerator {

    @Override
    public String summarizeOrder(String orderId) {
        return "Order " + orderId + " is being processed. No delays detected (stub response — "
                + "wire a BedrockInsightGenerator behind this interface for a real summary).";
    }

    @Override
    public String dailyDigest() {
        return "Daily fulfillment digest for " + LocalDate.now()
                + ": stub summary — no live order data queried in this local phase.";
    }
}
