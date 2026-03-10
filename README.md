# Notification Service

A Spring Boot-based notification service (Java) for sending and managing notifications.
This README was created for the user story: **"As a developer, I want well-documented code in a README so that others can build upon it in the future."**

## Overview
- **Language/Framework:** Java, Spring Boot
- **Build Tool:** Maven
- **Package Root:** `com.zoplanner.notification`
- **Architecture:** Standalone microservice (event-driven)
- **Modules (based on existing files):**
    - `controller`: REST controllers (e.g., `NotificationController`)
    - `repository`: Persistence/DB repositories (e.g., `NotificationRepository`)
    - `dto`: Data Transfer Objects (e.g., `NotificationDTO`)
    - `model`: Domain classes (e.g., `Notification`)
    - `service`: Business logic (e.g., `NotificationTemplate`)

> **Important:** This service is **standalone** and has no dependencies on other ZoPlanner repositories. It can run completely independently with its own database.

> **Note:** `NotificationController` is currently empty — add endpoints as needed (see template below).

## Getting Started (Local)
Requires **Java 21+** and **Maven 3.9+** installed.

```bash
# 1) Build and run tests
./mvnw clean verify   # or: mvn clean verify

# 2) Start the application
./mvnw spring-boot:run   # or: mvn spring-boot:run

# Application runs on: http://localhost:8082
```

### Run as JAR
```bash
./mvnw clean package
java -jar target/*.jar
```

## Running with Docker

### Quick Start with Docker (Standalone)

This service can run completely standalone with its own database:

```bash
# 1) Build JAR file (skip tests)
mvn clean package -DskipTests

# 2) Start containers with docker-compose
docker-compose up -d --build

# 3) Check status
docker-compose ps

# 4) View logs
docker-compose logs -f
```

Application now runs on: http://localhost:8082
PostgreSQL database runs on: localhost:5432

### Docker Commands

**Most common commands:**
- Start: `docker-compose up -d`
- Stop: `docker-compose down`
- Rebuild after code changes: `mvn clean package -DskipTests && docker-compose up -d --build`
- View logs: `docker-compose logs -f notification-service`
- View database logs: `docker-compose logs -f zoplanner-database`
- Clean everything: `docker-compose down -v` (removes containers and volumes)

### Docker Files

- **Dockerfile** - Container definition for notification service
- **docker-compose.yml** - Standalone setup (notification service + database)
- **database/Dockerfile** - PostgreSQL database container
- **.dockerignore** - Excludes unnecessary files from Docker build
- **.env** - Environment variables

## Configuration (Environment Variables/Properties)
Add/modify `application.properties` as needed.

### Local (application.properties)
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

### Docker (with .env file)
Uses the same `application.properties` but reads values from `.env` file:
```bash
# Database
HOST=zoplanner-database
PORT=5432
POSTGRES_USER=postgres
POSTGRES_PASSWORD=test123

# AWS credentials
AWS_ACCESS_KEY_ID=xxxxxxxx
AWS_SECRET_ACCESS_KEY=xxxxxxxx
AWS_REGION=eu-north-1

# SES
AWS_SES_FROM_EMAIL=noreply@zoplanner.com
AWS_SES_FROM_NAME=ZoPlanner Notifications

# SQS
SQS_QUEUE_URL=https://sqs.eu-north-1.amazonaws.com/123456789012/zoplanner-notifications
```

## Project Structure
```
src/
 └── main/
     ├── java/com/zoplanner/notification/
     │   ├── NotificationServiceApplication.java
     │   ├── config/
     │   │   ├── AwsConfig.java
     │   │   ├── AwsSesConfig.java
     │   │   ├── AwsSqsConfig.java
     │   │   └── AwsSnsConfig.java
     │   ├── controller/
     │   │   ├── NotificationController.java
     │   │   ├── EmailController.java
     │   │   └── FileController.java
     │   ├── dto/
     │   │   ├── NotificationDTO.java
     │   │   ├── EmailRequest.java
     │   │   └── FileResponse.java
     │   ├── model/
     │   │   ├── Notification.java
     │   │   └── FileEntity.java
     │   ├── repository/
     │   │   ├── NotificationRepository.java
     │   │   └── FileRepository.java
     │   ├── service/
     │   │   ├── NotificationService.java
     │   │   ├── EmailService.java
     │   │   └── FileService.java
     │   └── exception/
     │       ├── GlobalExceptionHandler.java
     │       ├── FileNotFoundException.java
     │       └── FileStorageException.java
     └── resources/
         └── application.properties
```

## File Upload Feature (Consultant Images)

### Overview of Core Functions

**FileService - 4 main functions:**

- **uploadFile(MultipartFile file)**
  - Receives a file via MultipartFile
  - Extracts metadata (name, type, size) and binary data
  - Saves to database via FileRepository
  - Returns FileResponse with ID and URL

- **replaceFile(Integer id, MultipartFile file)**
  - Finds existing file by ID
  - Throws FileNotFoundException if ID doesn't exist
  - Updates all field data with new file
  - Returns updated FileResponse

- **deleteFile(Integer id)**
  - Checks that the file exists
  - Throws FileNotFoundException if ID doesn't exist
  - Removes the file from the database
  - Returns nothing (void)

- **getFile(Integer id)**
  - Retrieves file from database by ID
  - Throws FileNotFoundException if ID doesn't exist
  - Returns complete FileEntity with binary data
  - Used by controller to download files

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/files` | Upload a new file |
| GET | `/files/{id}` | Download a file |
| PUT | `/files/{id}` | Replace an existing file |
| DELETE | `/files/{id}` | Delete a file |

### Testing Guide for Postman

#### Test 1: Upload File
- **Method:** `POST`
- **URL:** `http://localhost:8082/files`
- **Body:** 
  - Select `form-data`
  - Key: `file` (change type to `File` via dropdown)
  - Value: Select a file from your computer
- **Click:** Send
- **Expected response:**
```json
{
  "id": 1,
  "url": "/files/1",
  "fileName": "your-file.pdf",
  "size": 12345
}
```

#### Test 2: Retrieve/Download File
- **Method:** `GET`
- **URL:** `http://localhost:8082/files/1`
- **Body:** (no body needed)
- **Click:** Send
- **Expected:** File downloads or displays in Postman

#### Test 3: Replace File
- **Method:** `PUT`
- **URL:** `http://localhost:8082/files/1`
- **Body:**
  - Select `form-data`
  - Key: `file` (type `File`)
  - Value: Select a new/different file
- **Click:** Send
- **Expected response:**
```json
{
  "id": 1,
  "url": "/files/1",
  "fileName": "updated-file.pdf",
  "size": 54321
}
```

#### Test 4: Delete File
- **Method:** `DELETE`
- **URL:** `http://localhost:8082/files/1`
- **Body:** (no body needed)
- **Click:** Send
- **Expected response:**
```json
{
  "status": "success",
  "message": "File with ID 1 has been successfully deleted",
  "deleteFieldId": "1"
}
```

#### Test 5: Error Handling - File Not Found
- **Method:** `GET` (or `PUT`/`DELETE`)
- **URL:** `http://localhost:8082/files/999`
- **Click:** Send
- **Expected:** HTTP 404 with error message
```json
{
  "timestamp": "2026-03-10T...",
  "status": 404,
  "error": "Not Found",
  "message": "File not found with id: 999"
}
```

**Tip:** Save the ID from the upload response to use in subsequent tests.

#### Verification Checklist
- ✅ File uploads and returns correct metadata
- ✅ Downloaded file matches uploaded file
- ✅ File replacement updates the content
- ✅ File deletion removes the record from database
- ✅ Operations on non-existent files return 404
- ✅ Check database to verify records are created/updated/deleted
- ✅ Check application logs for info/error messages
- ✅ Test with different file types (PDF, images, text)
- ✅ Verify that `createdAt` and `updatedAt` timestamps are set correctly

### Database Schema

Files are stored in the `files` table:
- `id` - Auto-generated primary key
- `file_name` - Original filename
- `content_type` - MIME type (e.g., image/jpeg)
- `size` - File size in bytes
- `data` - Binary file data (BYTEA)
- `created_at` - Upload timestamp
- `updated_at` - Last update timestamp

### Configuration

Maximum file size: **10MB** (configurable in `application.properties`)

```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.enabled=true
```

## REST Endpoint Examples (Template)
> Add to `NotificationController` (adapt as needed)
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

## Code Style & Conventions
- Package structure as shown above.
- DTOs only for input/output from controllers.
- Services for business logic; Repositories for DB.
- Exception handling with `@ControllerAdvice` (add as needed).
- Logging with correlation IDs (`requestId`, `eventId`) where relevant.

## Workflow (issue → branch → PR → merge)
1. Create **Issue** in GitHub for the user story.
2. Create branch from `main`: `feature/readme-documentation-#<issueNr>`
3. Update/write `README.md` (this file).
4. Commit & push (see commands below).
5. Create **Pull Request** and link issue (`Closes #<issueNr>`).
6. Get review, ensure green CI, **Squash & merge**.

### Git Commands
```bash
# Create branch (replace <issueNr>)
git checkout -b feature/readme-documentation-#<issueNr>

# Add README and commit
git add README.md
git commit -m "docs(readme): add/update project README (#<issueNr>)"

# Push branch
git push -u origin feature/readme-documentation-#<issueNr>
```

## PR Checklist (Definition of Done for this user story)
- [ ] README describes **how to run** locally (commands).
- [ ] README describes **project structure** (packages, key classes).
- [ ] README shows **example endpoints** or links to OpenAPI.
- [ ] README lists **configuration** (port, DB, profiles) or references it.
- [ ] Spelling/formatting OK; headers and code blocks work on GitHub.
- [ ] PR text links issue: `Closes #<issueNr>`.

## Testing

### Unit Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=FileServiceTest
```

All 7 file service tests should pass:
- File upload
- File replacement
- File deletion
- File retrieval
- Error handling

### Integration Tests

See `POSTMAN_TESTING_GUIDE.md` for comprehensive API testing with Postman.

## Documentation

- **POSTMAN_TESTING_GUIDE.md** - Complete testing guide for file upload with Postman
- **SETUP_GUIDE.md** - IntelliJ IDEA and development environment setup instructions
- **ZoPlanner-File-Upload.postman_collection.json** - Import-ready Postman collection

## Technology Stack

- **Java 21**
- **Spring Boot 3.5.7**
- **PostgreSQL 16**
- **Maven**
- **Docker & Docker Compose**
- **AWS SDK** (SES, SQS, SNS)

## .NET Integration

This API is designed to work with the ZoPlanner .NET service for consultant image uploads.

**In your .NET appsettings.json:**
```json
{
  "SpringApi": {
    "BaseUrl": "http://localhost:8082"
  }
}
```

**Example .NET usage:**
```csharp
// Upload consultant image
await _fileService.UploadAsync(stream, fileName, contentType);

// This calls POST http://localhost:8082/files
```

The .NET service successfully integrates with this Spring Boot API for handling consultant image uploads.

## Troubleshooting

### Service won't start

```bash
# Check logs
docker-compose logs notification-service

# Restart
docker-compose restart notification-service
```

### Database connection failed

```bash
# Verify database is running
docker ps

# Check database health
docker exec zoplanner-database psql -U postgres -c "SELECT 1"
```

### File upload fails

```bash
# Check uploaded files in database
docker exec zoplanner-database psql -U postgres -d zoplanner -c "SELECT id, file_name, size FROM files;"

# View application logs
docker logs notification-service --tail 50
```

## License

Internal ZoPlanner project

