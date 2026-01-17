# Async Payment Gateway

A high-performance, event-driven payment orchestrator built with **Java Spring Boot**, **Redis**, and **Docker**. It features asynchronous payment processing, webhook delivery, and an embeddable checkout SDK.

## 🚀 Features
* **Async Processing:** Offloads heavy processing to Redis queues.
* **Webhook Delivery:** Notifies merchants of payment success/failure.
* **Idempotency:** Prevents duplicate charges using unique keys.
* **Resilience:** Microservices architecture (API + Worker).
* **Embeddable SDK:** Zero-dependency JavaScript integration.

## 🛠️ Tech Stack
* **Backend:** Java 17, Spring Boot
* **Database:** PostgreSQL
* **Queue:** Redis
* **Frontend:** HTML5, Vanilla JS, Nginx
* **Infrastructure:** Docker Compose

## 🏃‍♂️ Setup Instructions

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/Chadikabhanu/spring-payment-orchestrator.git](https://github.com/Chadikabhanu/spring-payment-orchestrator.git)
    cd spring-payment-orchestrator
    ```

2.  **Start the Application:**
    ```bash
    docker compose up -d --build
    ```

3.  **Access Services:**
    * **API:** `http://localhost:8000`
    * **Checkout Page:** `http://localhost:3001/checkout.html`
    * **Redis:** Port `6379`

## 📡 API Documentation

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/v1/payments` | Initiate a payment (Async) |
| `GET` | `/api/v1/payments/{id}` | Check payment status |
| `POST` | `/api/v1/refunds` | Initiate a refund |
| `GET` | `/actuator/health` | Health check |

### Environment Variables
All configuration is handled via `docker-compose.yml`. Key variables:
* `SPRING_DATASOURCE_URL`: PostgreSQL connection string.
* `SPRING_DATA_REDIS_HOST`: Redis host address.

## 🧪 Testing Instructions

**Manual Test:**
1. Open `http://localhost:3001/merchant_test.html` in your browser.
2. Click "Buy Now" to open the widget.
3. Click "Pay" to simulate a transaction.

**Automated Verification:**
Run the verification commands used in `submission.yml`:
```bash
curl -f http://localhost:8000/actuator/health