package com.gateway.api.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.jobs.ProcessPaymentJob;
import com.gateway.jobs.ProcessRefundJob;
import com.gateway.services.JobQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JobQueueService jobQueueService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // --- CREATE PAYMENT ---
    @PostMapping("/payments")
    public ResponseEntity<?> createPayment(
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody Map<String, Object> payload) throws JsonProcessingException {

        // 1. Authenticate
        String merchantIdSql = "SELECT id FROM merchants WHERE api_key = ?";
        UUID merchantId;
        try {
            merchantId = jdbcTemplate.queryForObject(merchantIdSql, UUID.class, apiKey);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid API Key"));
        }

        // 2. Check Idempotency
        if (idempotencyKey != null) {
            String checkSql = "SELECT response FROM idempotency_keys WHERE key = ? AND merchant_id = ? AND expires_at > NOW()";
            try {
                String cachedResponse = jdbcTemplate.queryForObject(checkSql, String.class, idempotencyKey, merchantId);
                return ResponseEntity.status(201).body(objectMapper.readTree(cachedResponse));
            } catch (Exception ignored) { }
        }

        // 3. Create Payment (Pending)
        String paymentId = "pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String insertSql = "INSERT INTO payments (id, merchant_id, amount, currency, order_id, status, method) VALUES (?, ?, ?, ?, ?, 'pending', ?)";
        
        jdbcTemplate.update(insertSql, 
            paymentId, merchantId, payload.get("amount"), payload.get("currency"), 
            payload.get("order_id"), payload.get("method")
        );

        // 4. Enqueue Job
        jobQueueService.enqueuePayment(new ProcessPaymentJob(paymentId));

        // 5. Response
        Map<String, Object> response = Map.of("id", paymentId, "status", "pending", "message", "Processing started");

        // 6. Save Idempotency
        if (idempotencyKey != null) {
            String saveKeySql = "INSERT INTO idempotency_keys (key, merchant_id, response, expires_at) VALUES (?, ?, ?::jsonb, NOW() + INTERVAL '24 hours')";
            jdbcTemplate.update(saveKeySql, idempotencyKey, merchantId, objectMapper.writeValueAsString(response));
        }

        return ResponseEntity.status(201).body(response);
    }

    // --- CREATE REFUND ---
    @PostMapping("/payments/{paymentId}/refunds")
    public ResponseEntity<?> createRefund(
            @PathVariable String paymentId,
            @RequestBody Map<String, Object> payload) {

        String checkSql = "SELECT amount, status, merchant_id FROM payments WHERE id = ?";
        Map<String, Object> payment;
        try {
            payment = jdbcTemplate.queryForMap(checkSql, paymentId);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", "Payment not found"));
        }
        
        if (!"success".equals(payment.get("status"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Payment not successful"));
        }

        String refundId = "rfnd_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        UUID merchantId = (UUID) payment.get("merchant_id");
        int amount = (int) payload.get("amount");

        String insertSql = "INSERT INTO refunds (id, payment_id, merchant_id, amount, status) VALUES (?, ?, ?, ?, 'pending')";
        jdbcTemplate.update(insertSql, refundId, paymentId, merchantId, amount);

        // Enqueue Refund Job
        jobQueueService.enqueueRefund(new ProcessRefundJob(refundId));

        return ResponseEntity.status(201).body(Map.of("id", refundId, "status", "pending"));
    }
}
