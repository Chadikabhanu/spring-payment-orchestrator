package com.gateway;

import com.gateway.workers.PaymentWorker;
import com.gateway.workers.WebhookWorker;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WorkerMain {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(WorkerMain.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Bean
    public CommandLineRunner run(PaymentWorker paymentWorker, WebhookWorker webhookWorker) {
        return args -> {
            System.out.println("🚀 Gateway Worker Service Started...");
            
            // Start the workers in separate threads
            new Thread(paymentWorker).start();
            new Thread(webhookWorker).start();
            
            System.out.println("✅ Workers are listening for jobs...");
            
            // Keep the main thread alive
            Thread.currentThread().join();
        };
    }
}