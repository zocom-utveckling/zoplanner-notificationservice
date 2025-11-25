# Konfigurationsförklaring - Notification Service

## Nuvarande Setup (Förenklad)

Vi använder **endast `application.properties`** för all konfiguration - både lokalt och i Docker.

## 🔧 Hur det fungerar

### application.properties
```properties
spring.application.name=notification-service
server.port=8080

# AWS SES Configuration
aws.region=${AWS_REGION:eu-north-1}
aws.accessKeyId=${AWS_ACCESS_KEY_ID:}
aws.secretAccessKey=${AWS_SECRET_ACCESS_KEY:}
ses.from.email=${SES_FROM_EMAIL:noreply@example.com}
```

### Syntaxförklaring: `${VAR:default}`

- `${AWS_REGION:eu-north-1}` betyder:
  - Om environment variable `AWS_REGION` finns → använd den
  - Annars → använd default-värdet `eu-north-1`

- `${AWS_ACCESS_KEY_ID:}` betyder:
  - Om environment variable `AWS_ACCESS_KEY_ID` finns → använd den
  - Annars → använd tomt värde `""`

## Lokalt (utan Docker)

Kör direkt:
```bash
mvn spring-boot:run
```

**Resultat:**
- Använder default-värden från `application.properties`
- Port: 8080
- AWS region: eu-north-1 (default)
- SES från-email: noreply@example.com (default)

## Docker

Kör med docker-compose:
```bash
docker-compose up -d
```

**Resultat:**
- Läser värden från `.env` filen
- Om `.env` inte har en variabel → använd default från `application.properties`

### .env exempel:
```env
AWS_REGION=eu-north-1
AWS_ACCESS_KEY_ID=din_access_key
AWS_SECRET_ACCESS_KEY=din_secret_key
SES_FROM_EMAIL=noreply@din-domain.com
```

## Fördelar med denna setup:

1. **En fil att underhålla** - bara `application.properties`
2. **Fungerar både lokalt och i Docker** - ingen profil behövs
3. **Säkra defaults** - applikationen startar även utan `.env`
4. **Flexibelt** - kan overrida med environment variables när det behövs

## Så här läser Spring Boot konfigurationen:

```
1. application.properties laddas
   ↓
2. Spring Boot letar efter environment variables
   ↓
3. Om variabel finns → använd den
   ↓
4. Om variabel INTE finns → använd default-värdet efter kolon (:)
   ↓
5. Slutgiltig konfiguration
```

## Environment Variables i olika miljöer:

### Lokalt (direkt i terminal):
```bash
# Windows PowerShell
$env:AWS_REGION="us-east-1"
mvn spring-boot:run

# Windows CMD
set AWS_REGION=us-east-1
mvn spring-boot:run
```

### Docker (via .env fil):
```bash
# Skapa .env fil
cp .env.example .env

# Redigera .env
# AWS_REGION=us-east-1

# Starta Docker
docker-compose up -d
```

### Docker (direkt i docker-compose.yml):
```yaml
services:
  notification-service:
    environment:
      - AWS_REGION=us-east-1
      - AWS_ACCESS_KEY_ID=xyz123
```

## Felsökning

### Se vilka värden som används:
```bash
# I Docker
docker exec zoplanner-notification-service printenv | grep AWS

# Lokalt - lägg till i application.properties:
logging.level.root=DEBUG
```

### Verifiera konfiguration vid start:
Lägg till i din `NotificationServiceApplication.java`:
```java
@PostConstruct
public void logConfig() {
    log.info("AWS Region: {}", awsRegion);
    log.info("SES From Email: {}", sesFromEmail);
}
```

## ⚠IDE Varningar

Du kan se varningar i IntelliJ typ:
```
Cannot resolve configuration property 'aws.region'
```

Detta är **normalt** och betyder bara att IDE:n inte hittar en `@ConfigurationProperties` class. 
Varningarna påverkar inte körningen av applikationen.

För att ta bort varningarna (valfritt), skapa en konfigurationsklass:
```java
@Configuration
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {
    private String region;
    private String accessKeyId;
    private String secretAccessKey;
    // getters and setters
}
```

## Sammanfattning

- ✅ **En konfigurationsfil** (`application.properties`)
- ✅ **Default-värden** för lokal utveckling
- ✅ **Environment variables** för Docker/produktion
- ✅ **Inget behov av profiler** (docker, prod, etc.)
- ✅ **Enklare att underhålla**

