# Notification Service

En Spring Boot-baserad notifikationstjänst (Java) för att skicka och hantera notiser.
Denna README är skapad för user storyn: **"Som utvecklare vill jag ha väldokumenterad kod i en README så att andra kan bygga vidare i framtiden."**

## Översikt
- **Språk/ramverk:** Java, Spring Boot
- **Bygg:** Maven
- **Paket-rotnamn:** `com.zoplanner.notification`
- **Arkitektur:** Fristående mikroservice (event-driven)
- **Moduler (utifrån befintliga filer):**
    - `controller`: REST-kontrollers (t.ex. `NotificationController`)
    - `repository`: Persistens/DB-repositories (t.ex. `NotificationRepository`)
    - `dto`: Data Transfer Objects (t.ex. `NotificationDTO`)
    - `model`: Domänklass(er) (t.ex. `Notification`)
    - `service`: Tjänstelogik (t.ex. `NotificationTemplate`)

> **Viktigt:** Denna tjänst är **fristående** och har inga beroenden till andra ZoPlanner-repositories. Den kan köras helt självständigt med sin egen databas.

> **Notera:** `NotificationController` är i nuläget tom — fyll på med endpoints enligt behov (se mall nedan).

## Kom igång (lokalt)
Förutsätter att du har **Java 21+** och **Maven 3.9+** installerat.

```bash
# 1) Bygg och kör tester
./mvnw clean verify   # eller: mvn clean verify

# 2) Starta applikationen
./mvnw spring-boot:run   # eller: mvn spring-boot:run

# Applikationen körs på: http://localhost:8082
```

### Kör som JAR
```bash
./mvnw clean package
java -jar target/*.jar
```

## Kör med Docker

### Snabbstart med Docker (Standalone)

Denna tjänst kan köras helt fristående med sin egen databas:

```bash
# 1) Bygg JAR-filen (skippa tester)
mvn clean package -DskipTests

# 2) Starta container med docker-compose
docker-compose up -d --build

# 3) Kontrollera status
docker-compose ps

# 4) Se loggar
docker-compose logs -f
```

Applikationen körs nu på: http://localhost:8082
PostgreSQL databas körs på: localhost:5432

### Docker-kommandon

**Vanligaste kommandona:**
- Starta: `docker-compose up -d`
- Stoppa: `docker-compose down`
- Rebuilda efter kodändringar: `mvn clean package -DskipTests && docker-compose up -d --build`
- Visa loggar: `docker-compose logs -f notification-service`
- Visa databas-loggar: `docker-compose logs -f zoplanner-database`
- Rensa allt: `docker-compose down -v` (tar bort containers och volymer)

### Docker-filer

- **Dockerfile** - Container-definition för notification service
- **docker-compose.yml** - Standalone setup (notification service + databas)
- **database/Dockerfile** - PostgreSQL databas-container
- **.dockerignore** - Exkluderar onödiga filer från Docker-bygget
- **.env** - Miljövariabler (skapa denna från .env.example om den inte finns)

## Konfiguration (miljövariabler/properties)
Lägg till/ändra `application.properties` enligt ert behov.

### Lokalt (application.properties)
```properties
server.port=8082
spring.application.name=notification-service

# AWS (non-secret config only)
aws.region=eu-north-1

# SES
aws.ses.from.email=${AWS_SES_FROM_EMAIL:noreply@zoplanner.com}
aws.ses.from.name=${AWS_SES_FROM_NAME:ZoPlanner Notifications}

# SQS
aws.sqs.queue-url=${AWS_SQS_QUEUE_URL}
```

### Docker (med .env fil)
Använder samma `application.properties` men läser värden från `.env` fil:
```bash
cp .env.example .env
# Redigera .env med dina AWS-credentials
# AWS credentials (DEV ONLY)
AWS_ACCESS_KEY_ID=xxxxxxxx
AWS_SECRET_ACCESS_KEY=xxxxxxxx
AWS_REGION=eu-north-1

# SES
AWS_SES_FROM_EMAIL=noreply@zoplanner.com
AWS_SES_FROM_NAME=ZoPlanner Notifications

# SQS
AWS_SQS_QUEUE_URL=https://sqs.eu-north-1.amazonaws.com/123456789012/zoplanner-notifications
```

## Projektstruktur (exempel)
```
src/
 └── main/
     ├── java/com/zoplanner/notification/
     │   ├── NotificationServiceApplication.java
     │   ├── config/
     │   │   └── AwsConfig.java
     │   ├── controller/
     │   │   └── NotificationController.java
     │   ├── dto/
     │   │   └── NotificationDTO.java
     │   ├── model/         # eller entity/
     │   │   └── Notification.java
     │   ├── repository/
     │   │   └── NotificationRepository.java
     │   └── template/
     │       └── NotificationTemplate.java
     └── resources/
         ├── application.yml
         └── db/migration/   # Flyway-migreringar (om ni använder)
```

## Exempel på REST-endpoints (mall)
> Lägg i `NotificationController` (anpassa efter behov)
```java
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService service;

  @PostMapping
  public ResponseEntity<NotificationDTO> create(@RequestBody @Valid NotificationDTO dto) {
    var created = service.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping(params = "userId")
  public Page<NotificationDTO> listByUser(@RequestParam String userId,
                                          @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    return service.listByUser(userId, pageable);
  }

  @PatchMapping("/{id}/read")
  public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
    service.markAsRead(id);
    return ResponseEntity.noContent().build();
  }
}
```

## Kodstil & konventioner
- Paketstruktur enligt ovan.
- DTOs endast för in/ut ur kontroller.
- Services för affärslogik; Repositories för DB.
- Exception-hantering med `@ControllerAdvice` (lägg till vid behov).
- Loggning med korrelations-ID (`requestId`, `eventId`) där det är relevant.

## Arbetsflöde (issue → branch → PR → merge)
1. Skapa **Issue** i GitHub för user storyn.
2. Skapa branch från `main`: `feature/readme-dokumentation-#<issueNr>`
3. Uppdatera/skriv `README.md` (denna fil).
4. Commit & push (se kommandon nedan).
5. Skapa **Pull Request** och länka issue (`Closes #<issueNr>`).
6. Få review, säkerställ grön CI, **Squash & merge**.

### Git-kommandon
```bash
# Skapa branch (ersätt <issueNr>)
git checkout -b feature/readme-dokumentation-#<issueNr>

# Lägg till README och committa
git add README.md
git commit -m "docs(readme): lägg till/uppdatera projekt-README (#<issueNr>)"

# Pusha branchen
git push -u origin feature/readme-dokumentation-#<issueNr>
```

## PR-checklista (Definition of Done för denna user story)
- [ ] README beskriver **hur man kör** lokalt (kommandon).
- [ ] README beskriver **projektstruktur** (paket, viktiga klasser).
- [ ] README visar **exempel-endpoints** eller länkar till OpenAPI.
- [ ] README listar **konfiguration** (port, DB, profiler) eller hänvisar.
- [ ] Stavning/format OK; rubriker och kodblock fungerar i GitHub.
- [ ] PR-texten länkar issue: `Closes #<issueNr>`.

## Licens

