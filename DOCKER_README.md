# Docker Commands - ZoPlanner Notification Service

## Project Structure

```
zoplanner-notificationservice/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── src/
```

## Quick Reference

| Action                    | Command                                                             |
| ------------------------- | ------------------------------------------------------------------- |
| First run                 | `mvn clean package -DskipTests && docker-compose up -d --build`     |
| Start service             | `docker-compose up -d`                                              |
| Stop service              | `docker-compose down`                                               |
| Restart service           | `docker-compose restart`                                            |
| Code changes              | `mvn clean package -DskipTests && docker-compose up -d --build notification-service` |
| View logs                 | `docker-compose logs -f`                                            |
| Check status              | `docker-compose ps`                                                 |

## Commands with Explanations

### Start Container - First Time vs Regular

```bash
# First run (build Spring Boot JAR first!, this command skips tests)
mvn clean package -DskipTests
docker-compose up -d --build

# Regular start
docker-compose up -d
```

### Stop Container

```bash
# Stop (removes container)
docker-compose down

# Stop without removing container
docker-compose stop
```

### Individual Container Operations

```bash
# Start notification service
docker-compose up -d notification-service

# Stop notification service
docker-compose stop notification-service

# Restart notification service
docker-compose restart notification-service
```

## Code Changes

### Spring Boot code changed

```bash
# Build JAR first, then rebuild container
mvn clean package -DskipTests
docker-compose up -d --build notification-service
```

Spring Boot Dockerfile copies pre-built JAR from `target/`.

## Monitoring

```bash
# Check status
docker ps
docker-compose ps

# View logs
docker-compose logs -f
docker-compose logs -f notification-service
docker-compose logs --tail=100 notification-service

# Access container shell
docker exec -it zoplanner-notification-service sh
```

## Testing

```bash
# Notification Service API
curl http://localhost:8081/actuator/health
curl http://localhost:8081/api/notifications
```

## Troubleshooting

```bash
# Check logs for errors
docker-compose logs notification-service

# Check environment variables
docker exec zoplanner-notification-service printenv

# Full clean restart
docker-compose down
mvn clean package -DskipTests
docker-compose up -d --build
```

## Network Architecture

```
┌─────────────────────────────────────────┐
│   Docker Network: zoplanner (bridge)    │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │   Notification Service           │  │
│  │   Port: 8081                     │  │
│  │   Container:                     │  │
│  │   zoplanner-notification-service │  │
│  └──────────────────────────────────┘  │
│                  ▲                      │
└──────────────────┼──────────────────────┘
                   │
            localhost:8081
```

**External access (via localhost):**
- Notification Service: `localhost:8081`

## Integration with Main ZoPlanner Services

This notification service can be integrated with the main ZoPlanner docker-compose.yml by:

1. Adding it as a service in the main docker-compose.yml
2. Sharing the same `zoplanner` network
3. Setting dependencies with `depends_on`

Example integration in main docker-compose.yml:

```yaml
services:
  # ... existing services (database, app, dotnet-service, frontend) ...
  
  notification-service:
    build:
      context: ../zoplanner-notificationservice  # Adjust path as needed
      dockerfile: Dockerfile
    container_name: zoplanner-notification-service
    networks:
      - zoplanner
    ports:
      - "8080:8080"
    env_file:
      - .env
    restart: unless-stopped
    depends_on:
      - app
```

Then the main app can call the notification service internally at:
`http://zoplanner-notification-service:8080/api/notifications`

