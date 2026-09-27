package com.practice.insightservice.api;

import com.practice.insightservice.service.InsightGenerator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/insights")
public class InsightController {

    private final InsightGenerator insightGenerator;

    public InsightController(InsightGenerator insightGenerator) {
        this.insightGenerator = insightGenerator;
    }

    @GetMapping("/orders/{orderId}")
    public Map<String, String> orderInsight(@PathVariable String orderId) {
        return Map.of("orderId", orderId, "summary", insightGenerator.summarizeOrder(orderId));
    }

    @GetMapping("/daily-digest")
    public Map<String, String> dailyDigest() {
        return Map.of("summary", insightGenerator.dailyDigest());
    }
}
