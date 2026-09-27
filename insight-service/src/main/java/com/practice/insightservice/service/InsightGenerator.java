package com.practice.insightservice.service;

public interface InsightGenerator {
    String summarizeOrder(String orderId);
    String dailyDigest();
}
