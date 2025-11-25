# Integration av Notification Service i Huvudprojektet

Om du vill köra notification service tillsammans med huvudprojektet (zoplanner-api), lägg till följande i huvudprojektets `docker-compose.yml`:

## I zoplanner-api/webapi/docker-compose.yml

Lägg till denna service-definition:

```yaml
  # Notification Service
  notification-service:
    build:
      context: ../../../zoplanner-notificationservice  # Justera sökvägen efter din struktur
      dockerfile: Dockerfile
    container_name: zoplanner-notification-service
    networks:
      - zoplanner
    ports:
      - "8080:8080"
    environment:
      - AWS_REGION=${AWS_REGION}
      - AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
      - AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
      - SES_FROM_EMAIL=${SES_FROM_EMAIL}
    restart: unless-stopped
    depends_on:
      - app
```

## Uppdatera .env i huvudprojektet

Lägg till dessa variabler i `.env` filen:

```env
# Notification Service - AWS SES Configuration
AWS_REGION=eu-north-1
AWS_ACCESS_KEY_ID=your_access_key_here
AWS_SECRET_ACCESS_KEY=your_secret_key_here
SES_FROM_EMAIL=noreply@example.com
```

## Kommunikation mellan services

När notification service körs i samma Docker-nätverk kan andra services nå den via:

- **Från Spring Boot (app)**: `http://notification-service:8081/api/notifications`
- **Från .NET service**: `http://notification-service:8081/api/notifications`
- **Från din dator**: `http://localhost:8081/api/notifications`

## Exempel: Anropa från Spring Boot

```java
@Service
public class NotificationClient {
    
    @Value("${notification.service.url:http://notification-service:8081}")
    private String notificationServiceUrl;
    
    private final RestTemplate restTemplate;
    
    public void sendNotification(NotificationDTO notification) {
        String url = notificationServiceUrl + "/api/notifications/send";
        restTemplate.postForObject(url, notification, String.class);
    }
}
```

## Exempel: Anropa från .NET

```csharp
public class NotificationService
{
    private readonly HttpClient _httpClient;
    private readonly string _notificationServiceUrl;
    
    public NotificationService(HttpClient httpClient, IConfiguration configuration)
    {
        _httpClient = httpClient;
        _notificationServiceUrl = configuration["NotificationService:BaseUrl"] 
            ?? "http://notification-service:8081";
    }
    
    public async Task SendNotificationAsync(NotificationDto notification)
    {
        var response = await _httpClient.PostAsJsonAsync(
            $"{_notificationServiceUrl}/api/notifications/send", 
            notification
        );
        response.EnsureSuccessStatusCode();
    }
}
```

## Uppdaterad Nätverksarkitektur

```
┌──────────────────────────────────────────────────────────────────────┐
│              Docker Network: zoplanner (bridge)                      │
│                                                                      │
│  ┌──────────┐  ┌───────────┐  ┌──────────┐  ┌────────────────────┐ │
│  │PostgreSQL│  │Spring Boot│  │.NET API  │  │Notification Service│ │
│  │          │◄─┤           │◄─┤          │◄─┤                    │ │
│  │Port: 5432│  │Port: 8080 │  │Port: 5027│  │Port: 8081          │ │
│  └──────────┘  └───────────┘  └──────────┘  └────────────────────┘ │
│       ▲              ▲              ▲                  ▲             │
└───────┼──────────────┼──────────────┼──────────────────┼─────────────┘
        │              │              │                  │
   localhost:5432  localhost:8080  localhost:5027  localhost:8081
```

## Kommandoreferens med Notification Service

```bash
# Första körningen (från zoplanner-api/webapi)
mvn clean package -DskipTests && docker-compose up -d --build

# Starta alla services
docker-compose up -d

# Stoppa alla services
docker-compose down

# Rebuilda endast notification service
docker-compose up -d --build notification-service

# Se loggar för notification service
docker-compose logs -f notification-service

# Testa notification service
curl http://localhost:8080/actuator/health
```

## Troubleshooting

### Notification service kan inte nås

```bash
# Kontrollera att containern körs
docker ps | grep notification

# Kontrollera loggar
docker logs zoplanner-notification-service

# Kontrollera nätverket
docker network inspect zoplanner
```

### AWS SES credentials fungerar inte

```bash
# Verifiera environment variables
docker exec zoplanner-notification-service printenv | grep AWS

# Testa AWS-konfigurationen från Spring Boot loggar
docker logs zoplanner-notification-service | grep AWS
```

