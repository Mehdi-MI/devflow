# DevFlow Architecture

## 1. Overview

DevFlow is a containerized DevOps platform built around a Spring Boot backend and an automated multi-architecture CI workflow.

The platform combines:

* Spring Boot
* PostgreSQL
* Docker
* Docker Buildx
* Jenkins
* Azure Container Registry
* Azure App Service
* Terraform

The architecture is designed to provide a reproducible workflow from source code to a containerized cloud deployment.

---

## 2. High-Level Architecture

```text
                         ┌─────────────────┐
                         │     GitHub      │
                         │  Source Control │
                         └────────┬────────┘
                                  │
                                  │ checkout
                                  ▼
                         ┌─────────────────┐
                         │     Jenkins     │
                         │       CI        │
                         └────────┬────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    │                           │
                    ▼                           ▼
              Gradle Build                  Gradle Tests
                    │                           │
                    └─────────────┬─────────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │ Docker Buildx   │
                         │  Multi-Arch     │
                         └────────┬────────┘
                                  │
                         ┌────────┴────────┐
                         │                 │
                         ▼                 ▼
                     linux/amd64       linux/arm64
                         │                 │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │ Azure Container │
                         │    Registry     │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │ Azure App       │
                         │     Service     │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │ Spring Boot API │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │   PostgreSQL    │
                         └─────────────────┘
```

---

## 3. Application Layer

The backend is implemented using Java 21 and Spring Boot.

The application is structured into several layers:

```text
Controller
    │
    ▼
Service
    │
    ▼
Repository
    │
    ▼
PostgreSQL
```

### Controller

`BuildTaskController` exposes the REST API for build-task operations.

### Service

`BuildTaskService` and its implementation contain the application's business logic.

### Repository

`BuildTaskRepository` provides persistence operations using Spring Data JPA.

### Entity

`BuildTask` represents a build task stored in PostgreSQL.

### DTOs

The API uses dedicated request and response DTOs:

* `CreateBuildTaskRequest`
* `UpdateBuildTaskRequest`
* `BuildTaskResponse`
* `ErrorResponse`

### Error Handling

`GlobalExceptionHandler` provides centralized handling for application exceptions, including resources that cannot be found.

---

## 4. Database

DevFlow uses PostgreSQL 17.

For local development, PostgreSQL runs as a Docker Compose service:

```text
PostgreSQL
    │
    └── Database: devflow
```

The backend connects to PostgreSQL using environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Local Docker Compose uses the PostgreSQL service name as the database host:

```text
jdbc:postgresql://postgres:5432/devflow
```

The backend is configured with:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

---

## 5. Container Architecture

The backend uses a two-stage Dockerfile.

### Build stage

The first stage uses:

```text
eclipse-temurin:21-jdk
```

It:

1. Copies the Gradle configuration.
2. Downloads dependencies.
3. Copies the application source.
4. Builds the Spring Boot JAR.

### Runtime stage

The second stage uses:

```text
eclipse-temurin:21-jre
```

Only the generated application JAR is copied into the runtime image.

This separates build dependencies from runtime dependencies and produces a smaller runtime image.

The application listens on:

```text
8081
```

---

## 6. Local Container Architecture

Docker Compose provides the local environment:

```text
┌──────────────────────┐
│ Docker Compose       │
│                      │
│  ┌────────────────┐  │
│  │ Backend        │  │
│  │ Spring Boot    │  │
│  │ :8081          │  │
│  └───────┬────────┘  │
│          │           │
│          ▼           │
│  ┌────────────────┐  │
│  │ PostgreSQL 17  │  │
│  │ :5432          │  │
│  └────────────────┘  │
│                      │
└──────────────────────┘
```

The backend waits for PostgreSQL to become healthy before starting.

---

## 7. CI Architecture

Jenkins provides the continuous-integration workflow.

The implemented pipeline contains the following stages:

```text
Checkout
   │
   ▼
Build
   │
   ▼
Test
   │
   ▼
Build Multi-Arch Image
   │
   ▼
Verify Multi-Arch Image
```

### Build

Jenkins executes:

```bash
./gradlew clean build -x test
```

from the `backend` directory.

### Test

Jenkins executes:

```bash
./gradlew test
```

### Multi-Architecture Build

Docker Buildx builds:

```text
linux/amd64
linux/arm64
```

The resulting image is pushed to Azure Container Registry.

The pipeline creates both:

```text
<registry>/devflow-backend:<BUILD_NUMBER>
<registry>/devflow-backend:latest
```

### Verification

Jenkins verifies the published image using:

```bash
docker buildx imagetools inspect
```

This verifies the published image manifest for the configured platforms.

---

## 8. Azure Architecture

Terraform provisions the main Azure infrastructure.

```text
┌──────────────────────────────────────────────┐
│ Azure Resource Group                         │
│                                              │
│  ┌──────────────────┐                        │
│  │ Azure Container  │                        │
│  │ Registry         │                        │
│  └────────┬─────────┘                        │
│           │                                  │
│           │ image                            │
│           ▼                                  │
│  ┌──────────────────┐                        │
│  │ Azure App        │                        │
│  │ Service           │                       │
│  └────────┬─────────┘                        │
│           │                                  │
│           │                                  │
│  ┌────────▼─────────┐                        │
│  │ PostgreSQL       │                        │
│  │ Flexible Server  │                        │
│  └──────────────────┘                        │
│                                              │
└──────────────────────────────────────────────┘
```

The Terraform configuration currently provisions:

* Azure Resource Group
* Azure Container Registry
* Linux App Service Plan
* Linux Web App
* PostgreSQL Flexible Server

The default Azure region is:

```text
spaincentral
```

---

## 9. Container Registry Authentication

The Azure App Service uses a system-assigned managed identity.

Terraform configures:

```text
container_registry_use_managed_identity = true
```

This allows the App Service to authenticate with the container registry using its managed identity rather than storing registry credentials directly in the application configuration.

---

## 10. Infrastructure as Code

Terraform manages the Azure infrastructure.

The configuration is located at:

```text
infrastructure/terraform/
```

Terraform defines:

```text
Resource Group
     │
     ├── Container Registry
     ├── App Service Plan
     ├── Linux Web App
     └── PostgreSQL Flexible Server
```

Important Terraform files include:

```text
main.tf
variables.tf
outputs.tf
versions.tf
```

Terraform state and variable files are excluded from version control.

---

## 11. Configuration

The application supports environment-based configuration.

The main database settings are:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

This allows the same application image to be used in different environments without rebuilding the image.

---

## 12. Health Monitoring

Spring Boot Actuator exposes:

```text
/actuator/health
/actuator/info
```

The health endpoint can be used to verify that the application is running correctly after deployment.

Example:

```bash
curl http://localhost:8081/actuator/health
```

---

## 13. End-to-End Workflow

The complete implemented workflow is:

```text
Developer
    │
    ▼
GitHub
    │
    ▼
Jenkins
    │
    ├── Checkout
    ├── Gradle Build
    ├── Tests
    └── Docker Buildx
           ├── AMD64
           └── ARM64
                 │
                 ▼
        Azure Container Registry
                 │
                 ▼
        Azure App Service
                 │
                 ▼
           Spring Boot
                 │
                 ▼
             PostgreSQL
```

Terraform provides the infrastructure required by the Azure portion of this architecture.

---

## 14. Design Principles

The architecture follows several DevOps principles.

### Automation

Build and test operations are automated through Jenkins.

### Reproducibility

Docker and Terraform provide reproducible application and infrastructure environments.

### Multi-Architecture Support

Docker Buildx allows the application image to support both AMD64 and ARM64.

### Separation of Concerns

Application code, CI configuration, deployment infrastructure, and documentation are maintained in separate areas of the repository.

### Configuration Through Environment Variables

Database configuration is externalized rather than hard-coded into the application image.

### Infrastructure as Code

Azure infrastructure is defined declaratively with Terraform rather than configured exclusively through the Azure Portal.
