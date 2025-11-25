# 🚀 Snabbstart - ZoPlanner Notification Service

## Vad har skapats?

✅ **Dockerfile** - Containeriserar Spring Boot-applikationen med Java 21
✅ **docker-compose.yml** - Enkel orkestrering för standalone-körning
✅ **application.properties** - Uppdaterad med port 8081 och AWS-konfiguration
✅ **application-docker.properties** - Docker-specifik profil
✅ **.env.example** - Mall för miljövariabler
✅ **.dockerignore** - Optimerar Docker-bygget
✅ **DOCKER_README.md** - Detaljerad Docker-dokumentation
✅ **INTEGRATION_README.md** - Guide för att integrera med huvudprojektet

## 📋 Checklista - Komma igång

### 1. Kopiera miljövariabler
```bash
cp .env.example .env
```

Redigera `.env` och fyll i dina AWS SES-credentials:
```env
AWS_REGION=eu-north-1
AWS_ACCESS_KEY_ID=din_access_key
AWS_SECRET_ACCESS_KEY=din_secret_key
SES_FROM_EMAIL=noreply@din-domän.com
```

### 2. Bygg och starta med Docker (Första gången)
```bash
# Bygg JAR-filen
mvn clean package -DskipTests

# Starta containern
docker-compose up -d --build
```

### 3. Verifiera att det fungerar
```bash
# Kontrollera status
docker-compose ps

# Se loggar
docker-compose logs -f

# Testa API:et
curl http://localhost:8081/actuator/health
```

## Vanliga arbetsflöden

### Uppdatera kod och rebuilda
```bash
mvn clean package -DskipTests
docker-compose up -d --build notification-service
```

### Starta/stoppa containern
```bash
# Starta
docker-compose up -d

# Stoppa
docker-compose down

# Restart
docker-compose restart
```

### Felsökning
```bash
# Visa loggar
docker-compose logs -f notification-service

# Komma in i containern
docker exec -it zoplanner-notification-service sh

# Kontrollera miljövariabler
docker exec zoplanner-notification-service printenv
```

## 🔌 Integration med huvudprojektet

### Standalone (detta projekt)
Notification service körs på: **http://localhost:8081**

### Tillsammans med huvudprojektet
Se [INTEGRATION_README.md](INTEGRATION_README.md) för:
- Hur man lägger till notification service i huvudprojektets docker-compose.yml
- Hur andra services kan anropa notification service
- Nätverksarkitektur och kommunikation mellan containers

## Projektstruktur

```
zoplanner-notificationservice/
├── Dockerfile                           # Container-definition
├── docker-compose.yml                   # Docker orkestrering
├── .env.example                         # Mall för miljövariabler
├── .env                                 # Dina faktiska miljövariabler (gitignored)
├── .dockerignore                        # Docker build-optimering
├── DOCKER_README.md                     # Detaljerad Docker-dokumentation
├── INTEGRATION_README.md                # Guide för huvudprojekt-integration
├── QUICKSTART.md                        # Denna fil
├── README.md                            # Uppdaterad huvuddokumentation
├── pom.xml                              # Maven-konfiguration
└── src/
    └── main/
        ├── java/
        │   └── com/zoplanner/notification/
        │       ├── NotificationServiceApplication.java
        │       ├── controller/
        │       ├── service/
        │       ├── repository/
        │       ├── model/
        │       ├── dto/
        │       └── config/
        └── resources/
            └── application.properties         # Huvudkonfiguration (fungerar lokalt & Docker)
```

## Nästa steg

1. Konfigurera AWS SES-credentials i `.env`
2. Testa notification service standalone med `docker-compose up -d --build`
3. Läs [INTEGRATION_README.md](INTEGRATION_README.md) för att integrera med huvudprojektet
4. Implementera dina notification endpoints i `NotificationController`
5. Testa att skicka mail via AWS SES

## Hjälp och dokumentation

- **Docker-kommandon**: Se [DOCKER_README.md](DOCKER_README.md)
- **Integration**: Se [INTEGRATION_README.md](INTEGRATION_README.md)
- **Allmän info**: Se [README.md](README.md)

## Docker Network

När notification service körs i Docker använder den nätverket `zoplanner`. Detta gör det möjligt för andra services att nå den via container-namnet `zoplanner-notification-service`.

**Extern access (från din dator):**
```
http://localhost:8080
```

**Intern access (från andra containers):**
```
http://zoplanner-notification-service:8080
```

---

**Lycka till med din notification service! 🎉**

