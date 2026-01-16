package com.gateway.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.jobs.ProcessPaymentJob;
import com.gateway.services.JobQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class PaymentWorker implements Runnable {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate; // To update the DB

    @Autowired
    private JobQueueService jobQueueService; // To enqueue webhooks

    @Value("${TEST_MODE:false}")
    private boolean testMode;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run() {
        System.out.println("PaymentWorker started watching: " + JobQueueService.PAYMENT_QUEUE);
        
        while (true) {
            try {
                // 1. Pop a job from Redis (waits 5 seconds if empty, then loops)
                Object jobObj = redisTemplate.opsForList().rightPop(JobQueueService.PAYMENT_QUEUE, 5, TimeUnit.SECONDS);
                
                if (jobObj != null) {
                    // Convert the raw object back to our Job class
                    ProcessPaymentJob job = objectMapper.convertValue(jobObj, ProcessPaymentJob.class);
                    processPayment(job);
                }
            } catch (Exception e) {
                System.err.println("Error in PaymentWorker: " + e.getMessage());
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void processPayment(ProcessPaymentJob job) {
        String paymentId = job.getPaymentId();
        System.out.println("Processing Payment: " + paymentId);

        try {
            // 2. Simulate Delay (1-2 seconds for testing, 5-10 for prod)
            int delay = testMode ? 1000 : 5000; 
            Thread.sleep(delay);

            // 3. Determine Outcome
            // In a real app, you'd talk to a bank here. 
            // For now, let's assume success.
            String status = "success";
            
            // 4. Update Database
            String sql = "UPDATE payments SET status = ?, updated_at = NOW() WHERE id = ?";
            jdbcTemplate.update(sql, status, paymentId);
            
            System.out.println("Payment " + paymentId + " marked as " + status);

            // 5. Trigger Webhook (Event Driven!)
            // We will add this later once we finish the Webhook Worker
            // jobQueueService.enqueueWebhook(...);

        } catch (Exception e) {
            System.err.println("Failed to process payment " + paymentId);
            jdbcTemplate.update("UPDATE payments SET status = 'failed' WHERE id = ?", paymentId);
        }
    }
}