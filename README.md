# Async Payment Orchestrator

A high-performance, event-driven payment gateway built with **Spring Boot**, **Redis**, and **Docker**. It features asynchronous processing, webhook delivery, and a clean embeddable SDK.

## 🚀 Features
* **Async Processing:** Uses Redis Queues to offload payment processing from the main API.
* **Webhook Delivery:** Automatic notifications to merchants upon payment success/failure.
* **Embeddable SDK:** A zero-dependency JavaScript SDK for seamless integration.
* **Idempotency:** Prevents double-charging using unique keys.
* **Resilience:** Dockerized architecture with separate API and Worker services.

## 🛠️ Tech Stack
* **Backend:** Java 17, Spring Boot
* **Database:** PostgreSQL
* **Queue:** Redis
* **Frontend:** Vanilla JS, HTML5
* **Deployment:** Docker Compose, Render

## 🏃‍♂️ Quick Start

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/Chadikabhanu/spring-payment-orchestrator.git](https://github.com/Chadikabhanu/spring-payment-orchestrator.git)
    cd spring-payment-orchestrator
    ```

2.  **Start the System:**
    ```bash
    docker compose up -d --build
    ```

3.  **Test a Payment:**
    Open `merchant_test.html` in your browser and click "Buy Now".

## 📡 API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/payments` | Initiate a payment (Async) |
| `GET` | `/api/v1/payments/{id}` | Check payment status |
| `POST` | `/api/v1/refunds` | Initiate a refund |

## 🧪 Testing Instructions
To verify the system manually:
1.  Ensure all containers are running: `docker compose ps`
2.  Run the verification curl:
    ```bash
    curl http://localhost:8000/actuator/health
    ```