package com.gateway.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.jobs.ProcessRefundJob;
import com.gateway.services.JobQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class RefundWorker implements Runnable {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void run() {
        System.out.println("RefundWorker started watching: " + JobQueueService.REFUND_QUEUE);
        while (true) {
            try {
                Object jobObj = redisTemplate.opsForList().rightPop(JobQueueService.REFUND_QUEUE, 5, TimeUnit.SECONDS);
                if (jobObj != null) {
                    ProcessRefundJob job = objectMapper.convertValue(jobObj, ProcessRefundJob.class);
                    processRefund(job);
                }
            } catch (Exception e) {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private void processRefund(ProcessRefundJob job) {
        String refundId = job.getRefundId();
        System.out.println("Processing Refund: " + refundId);
        
        try {
            // Simulate processing
            Thread.sleep(3000);
            
            // Mark as processed
            jdbcTemplate.update(
                "UPDATE refunds SET status = 'processed', processed_at = NOW() WHERE id = ?", 
                refundId
            );
            System.out.println("✅ Refund " + refundId + " processed.");
        } catch (Exception e) {
            System.err.println("Refund failed: " + e.getMessage());
        }
    }
}