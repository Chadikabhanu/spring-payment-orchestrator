package com.gateway.jobs;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

public class DeliverWebhookJob implements Serializable {
    private UUID merchantId;
    private String eventType;
    private Map<String, Object> payload;
    private UUID webhookLogId; // For tracking specific attempts

    public DeliverWebhookJob() {}

    public DeliverWebhookJob(UUID merchantId, String eventType, Map<String, Object> payload) {
        this.merchantId = merchantId;
        this.eventType = eventType;
        this.payload = payload;
    }

    // Getters and Setters
    public UUID getMerchantId() { return merchantId; }
    public void setMerchantId(UUID merchantId) { this.merchantId = merchantId; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    public UUID getWebhookLogId() { return webhookLogId; }
    public void setWebhookLogId(UUID webhookLogId) { this.webhookLogId = webhookLogId; }
}