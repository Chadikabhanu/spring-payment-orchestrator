package com.gateway.services;

import com.gateway.jobs.DeliverWebhookJob;
import com.gateway.jobs.ProcessPaymentJob;
import com.gateway.jobs.ProcessRefundJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class JobQueueService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public static final String PAYMENT_QUEUE = "queue:payments";
    public static final String WEBHOOK_QUEUE = "queue:webhooks";
    public static final String REFUND_QUEUE = "queue:refunds";

    public void enqueuePayment(ProcessPaymentJob job) {
        redisTemplate.opsForList().leftPush(PAYMENT_QUEUE, job);
        System.out.println("Enqueued Payment Job: " + job.getPaymentId());
    }

    public void enqueueWebhook(DeliverWebhookJob job) {
        redisTemplate.opsForList().leftPush(WEBHOOK_QUEUE, job);
        System.out.println("Enqueued Webhook Job: " + job.getEventType());
    }

    public void enqueueRefund(ProcessRefundJob job) {
        redisTemplate.opsForList().leftPush(REFUND_QUEUE, job);
        System.out.println("Enqueued Refund Job: " + job.getRefundId());
    }
}
