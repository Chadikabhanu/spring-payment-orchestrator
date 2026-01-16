package com.gateway.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.jobs.DeliverWebhookJob;
import com.gateway.services.JobQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

@Component
public class WebhookWorker implements Runnable {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${WEBHOOK_RETRY_INTERVALS_TEST:false}")
    private boolean useTestIntervals;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void run() {
        System.out.println("WebhookWorker started watching: " + JobQueueService.WEBHOOK_QUEUE);
        
        while (true) {
            try {
                // 1. Pop job from Redis
                Object jobObj = redisTemplate.opsForList().rightPop(JobQueueService.WEBHOOK_QUEUE, 5, TimeUnit.SECONDS);
                
                if (jobObj != null) {
                    DeliverWebhookJob job = objectMapper.convertValue(jobObj, DeliverWebhookJob.class);
                    processWebhook(job);
                }
            } catch (Exception e) {
                System.err.println("Error in WebhookWorker: " + e.getMessage());
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void processWebhook(DeliverWebhookJob job) {
        try {
            // 2. Fetch Merchant Config (Secret & URL)
            String sql = "SELECT webhook_url, webhook_secret FROM merchants WHERE id = ?";
            // Note: In real code, use a RowMapper. This is a quick shortcut.
            java.util.Map<String, Object> merchant = jdbcTemplate.queryForMap(sql, job.getMerchantId());
            
            String webhookUrl = (String) merchant.get("webhook_url");
            String secret = (String) merchant.get("webhook_secret");

            if (webhookUrl == null || webhookUrl.isEmpty()) {
                System.out.println("No webhook URL for merchant " + job.getMerchantId());
                return;
            }

            // 3. Prepare Payload & Signature
            String jsonPayload = objectMapper.writeValueAsString(job.getPayload());
            String signature = calculateHmacSha256(secret, jsonPayload);

            // 4. Send Request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("X-Webhook-Signature", signature);

            HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
            
            System.out.println("Sending webhook to: " + webhookUrl);
            restTemplate.postForEntity(webhookUrl, request, String.class);
            
            // 5. Log Success (Update database log if exists)
            // (Implementation of logging skipped for brevity, but you'd update 'webhook_logs' here)
            System.out.println("✅ Webhook delivered successfully!");

        } catch (Exception e) {
            System.err.println("❌ Webhook failed: " + e.getMessage());
            handleRetry(job);
        }
    }

    private void handleRetry(DeliverWebhookJob job) {
        // Simple retry logic: Push back to queue if under 5 attempts
        // In a real production system, you would check 'attempts' from DB 
        // and calculate 'next_retry_at' based on the exponential backoff schedule.
        // For this MVP, we just log it.
        System.out.println("TODO: Schedule retry for job");
    }

    private String calculateHmacSha256(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        
        byte[] rawHmac = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        // Convert to Hex
        StringBuilder hexString = new StringBuilder();
        for (byte b : rawHmac) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}