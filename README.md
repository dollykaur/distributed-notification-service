# Distributed Notification Service

A distributed, event-driven notification system built with **Spring Boot**, **Apache Kafka**, **PostgreSQL**, and **Redis**. The system supports sending notifications via **Email**, **SMS**, and **Push** channels using a microservices architecture.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT                               │
│              POST /api/notifications                        │
│         { channel, recipient, message }                     │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                  NOTIFICATION API                           │
│                  (notification-api)                         │
│                                                             │
│  ┌─────────────────┐        ┌──────────────────────────┐   │
│  │   Controller    │──────▶ │       Service            │   │
│  │  POST /api/     │        │  1. Save to PostgreSQL   │   │
│  │  notifications  │        │  2. Publish to Kafka     │   │
│  └─────────────────┘        └──────────────────────────┘   │
└──────────────────────────────────────┬──────────────────────┘
                                       │
                      ┌────────────────┼────────────────┐
                      │                │                │
                      ▼                ▼                ▼
        ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
        │  KAFKA TOPIC    │  │  KAFKA TOPIC    │  │  KAFKA TOPIC    │
        │ notifications-  │  │ notifications-  │  │ notifications-  │
        │      api        │  │      api        │  │      api        │
        └────────┬────────┘  └────────┬────────┘  └────────┬────────┘
                 │                    │                     │
                 ▼                    ▼                     ▼
    ┌─────────────────────┐  ┌──────────────────┐  ┌──────────────────┐
    │   SMS WORKER        │  │  EMAIL WORKER    │  │  PUSH WORKER     │
    │ (sms-worker-group)  │  │(email-worker-    │  │(push-worker-     │
    │                     │  │    group)        │  │    group)        │
    │ 1. Check Redis      │  │ 1. Check Redis   │  │ 1. Check Redis   │
    │    (idempotency)    │  │    (idempotency) │  │    (idempotency) │
    │ 2. Send SMS         │  │ 2. Send Email    │  │ 2. Send Push     │
    │ 3. Update Postgres  │  │ 3. Update        │  │ 3. Update        │
    │ 4. Mark in Redis    │  │    Postgres      │  │    Postgres      │
    └──────────┬──────────┘  │ 4. Mark in Redis │  │ 4. Mark in Redis │
               │             └────────┬─────────┘  └────────┬─────────┘
               │                      │                      │
               └──────────────────────┼──────────────────────┘
                                      │
                      ┌───────────────┴───────────────┐
                      │                               │
                      ▼                               ▼
           ┌─────────────────────┐        ┌────────────────────┐
           │     PostgreSQL      │        │       Redis        │
           │  notifications DB   │        │  Idempotency Cache │
           │  (stores status)    │        │   TTL: 24 hours    │
           └─────────────────────┘        └────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                   MONITORING STACK                          │
│                                                             │
│   Spring Actuator ──▶ Prometheus ──▶ Grafana Dashboards    │
│   (metrics exposed)   (collects)     (visualizes)          │
└─────────────────────────────────────────────────────────────┘
```

---

## Services

| Service | Description | Port |
|---|---|---|
| `notification-api` | REST API — receives notification requests and publishes to Kafka | 8080 |
| `notification-worker-email` | Kafka consumer — processes and sends email notifications | 8081 |
| `notification-worker-sms` | Kafka consumer — processes and sends SMS notifications | 8082 |
| `notification-worker-push` | Kafka consumer — processes and sends push notifications | 8083 |

---

## Tech Stack

| Technology | Purpose |
|---|---|
| **Spring Boot 3** | Java framework for building microservices |
| **Apache Kafka** | Message broker for event-driven communication |
| **PostgreSQL** | Relational database for storing notifications |
| **Redis** | In-memory cache for idempotency checks |
| **Spring Data JPA** | ORM for database operations |
| **Lombok** | Reduces boilerplate Java code |
| **Micrometer/Actuator** | Metrics and monitoring |
| **Prometheus** | Metrics collection and alerting |
| **Grafana** | Metrics visualization and dashboards |
| **Docker & Docker Compose** | Containerized infrastructure setup |
| **Adminer** | Database UI for PostgreSQL |
| **Maven** | Build and dependency management |

---

## How It Works

### 1. Creating a Notification
A client sends a `POST` request to the Notification API:

```json
POST /api/notifications
{
  "channel": "EMAIL",
  "recipient": "user@example.com",
  "message": "Your order has been confirmed!"
}
```

### 2. Notification API
- Saves the notification to **PostgreSQL** with status `PENDING`
- Publishes a message to the **Kafka** topic `notifications-api` in the format:
```
EMAIL|user@example.com|Your order has been confirmed!
```

### 3. Worker Services
Each worker listens to the same Kafka topic `notifications-api` but in different **consumer groups**:

| Worker | Consumer Group | Processes |
|---|---|---|
| Email Worker | `email-worker-group` | Messages where channel = `EMAIL` |
| SMS Worker | `sms-worker-group` | Messages where channel = `SMS` |
| Push Worker | `push-worker-group` | Messages where channel = `PUSH` |

### 4. Idempotency with Redis
Each worker checks Redis before processing:
```
Message arrives
      ↓
Check Redis: isDuplicate(messageId)?
      ↓
YES → Skip (duplicate message)
NO  → Process → Send notification → Update PostgreSQL → Mark in Redis (TTL: 24h)
```

---

## Key Features

- ✅ **Event-driven architecture** — services communicate via Kafka, fully decoupled
- ✅ **Idempotency** — Redis prevents duplicate notifications from being sent
- ✅ **Multi-channel support** — Email, SMS and Push notifications
- ✅ **Status tracking** — notification status updated in PostgreSQL (`PENDING` → `SENT`)
- ✅ **Dead Letter Queue (DLQ)** — failed messages published to `notifications-api.DLQ`
- ✅ **Retry with exponential backoff** — 3 retries with 1s → 2s → 4s intervals before DLQ
- ✅ **SMTP Email sending** — real email sending via `JavaMailSender`
- ✅ **Metrics & Monitoring** — tracks processed/duplicate/failed/DLQ counts via Micrometer
- ✅ **Docker Compose** — full infrastructure setup with one command
- ✅ **Prometheus & Grafana** — metrics collection and visualization dashboards
- ✅ **Scalable** — each worker can be scaled independently
- ✅ **Fault tolerant** — if a worker is down, Kafka queues messages until it recovers

---

## Project Structure

```
distributed-notification-service/
│
├── infra/                                    # Infrastructure
│   ├── docker-compose.yml                    # All services (Kafka, PostgreSQL, Redis, etc.)
│   └── prometheus.yml                        # Prometheus configuration
│
├── notification-api/                         # REST API & Kafka Producer
│   └── src/main/java/com/example/
│       ├── controller/NotificationController.java
│       ├── service/NotificationService.java
│       ├── kafka/NotificationProducer.java
│       ├── model/Notification.java
│       └── repository/NotificationRepository.java
│
├── notification-worker-email/                # Email Consumer
│   └── src/main/java/com/example/
│       ├── consumer/EmailConsumer.java
│       ├── service/EmailSender.java
│       ├── redis/RedisService.java
│       └── model/Notification.java
│
├── notification-worker-sms/                  # SMS Consumer
│   └── src/main/java/com/example/
│       ├── consumer/SmsConsumer.java
│       ├── redis/RedisService.java
│       └── model/Notification.java
│
└── notification-worker-push/                 # Push Consumer
    └── src/main/java/com/example/
        ├── consumer/PushConsumer.java
        ├── redis/RedisService.java
        └── model/Notification.java
```

---

## Infrastructure Setup (Docker)

All infrastructure services run via **Docker Compose** — no manual installation needed!

### Services Started by Docker:

| Container | Purpose | Port |
|---|---|---|
| **Zookeeper** | Manages Kafka brokers | 2181 |
| **Kafka** | Message broker / event bus | 9092 |
| **PostgreSQL** | Notification database | 5432 |
| **Redis** | Idempotency cache | 6379 |
| **Prometheus** | Metrics collection | 9090 |
| **Grafana** | Metrics dashboards | 3000 |
| **Adminer** | Database UI | 8081 |

### Start All Infrastructure:

```bash
cd infra
docker-compose up -d
```

### Stop All Infrastructure:

```bash
docker-compose down
```

### Access Services:

| Service | URL |
|---|---|
| **Adminer (DB UI)** | http://localhost:8081 |
| **Prometheus** | http://localhost:9090 |
| **Grafana** | http://localhost:3000 |

---

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- Apache Kafka 3+
- Redis 7+

---

## Configuration

Each service has its own `application.properties`:

```properties
# PostgreSQL
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/notifications}
spring.datasource.username=${DB_USERNAME:notif_user}
spring.datasource.password=${DB_PASSWORD:notif_pass}

# Kafka
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

# Redis
spring.redis.host=${REDIS_HOST:localhost}
spring.redis.port=${REDIS_PORT:6379}
```

Copy `.env.example` to `.env` and fill in your values:
```bash
cp .env.example .env
```

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/notifications` | Create a new notification |
| `GET` | `/api/notifications/ping` | Health check |

---

## Future Improvements

- [ ] Integrate Twilio for real SMS sending
- [ ] Integrate SendGrid for real email sending
- [ ] Add Firebase Cloud Messaging for push notifications
- [ ] Add API authentication (JWT)
- [ ] Add rate limiting per recipient

---

## Author

**Dolly Kaur** — [github.com/dollykaur](https://github.com/dollykaur)
