# DevFlow Requirements

## 1. Introduction

DevFlow is a containerized DevOps platform designed to automate the build, testing, containerization, and deployment workflow of a Spring Boot application.

The project combines application development, automated CI, multi-architecture container builds, cloud infrastructure, container registry management, and cloud deployment into a reproducible workflow.

The system is designed to support both local development and deployment to Microsoft Azure.

---

## 2. Project Objectives

The main objectives of DevFlow are:

* Provide a REST API for managing build tasks.
* Persist build-task information in PostgreSQL.
* Containerize the Spring Boot application.
* Automate application builds and tests using Jenkins.
* Build container images for multiple CPU architectures.
* Publish container images to Azure Container Registry.
* Deploy the application to Azure App Service.
* Provision Azure infrastructure using Terraform.
* Externalize environment-specific configuration.
* Provide application health monitoring.
* Maintain a reproducible DevOps workflow.

---

## 3. Functional Requirements

### FR-01 — Build Task Management

The system shall provide a REST API for managing build tasks.

The API shall support:

* Creating a build task.
* Listing all build tasks.
* Retrieving a build task by identifier.
* Updating a build task.
* Deleting a build task.

The base API path is:

```text
/api/build-tasks
```

---

### FR-02 — Build Task Creation

The API shall allow clients to create a build task containing:

* Task name.
* Repository URL.
* Branch.

The following fields are required:

```text
name
repositoryUrl
branch
```

Validation shall reject requests where required fields are missing or blank.

New tasks shall initially use the:

```text
PENDING
```

status.

---

### FR-03 — Build Task Retrieval

The API shall allow clients to retrieve:

* All existing build tasks.
* A specific build task by UUID.

If the requested task does not exist, the API shall return an appropriate `404 Not Found` response.

---

### FR-04 — Build Task Update

The API shall allow an existing build task to be updated.

Supported update fields include:

* Name.
* Repository URL.
* Branch.
* Build status.
* Build logs.

---

### FR-05 — Build Task Deletion

The API shall allow a build task to be deleted by UUID.

A successful deletion shall return:

```text
204 No Content
```

Attempting to delete a task that does not exist shall return:

```text
404 Not Found
```

---

### FR-06 — Build Status

Build tasks shall maintain a status representing their current state.

The supported status model includes:

```text
PENDING
RUNNING
SUCCESS
FAILED
```

The status shall be persisted with the build task.

---

### FR-07 — Build Metadata

A build task shall maintain relevant execution metadata, including:

* Creation timestamp.
* Start timestamp.
* Completion timestamp.
* Build logs.

---

### FR-08 — REST API Error Handling

The application shall provide centralized REST API error handling.

The API shall provide structured error responses containing information such as:

* Timestamp.
* HTTP status.
* Error description.
* Error message.

The application shall handle at least:

* Resource-not-found errors.
* Request validation errors.
* Unexpected application errors.

---

## 4. Backend Requirements

### BR-01 — Java Runtime

The backend shall use Java 21.

---

### BR-02 — Spring Boot

The backend shall be implemented using Spring Boot.

---

### BR-03 — Gradle

The project shall use Gradle for:

* Dependency management.
* Application compilation.
* Testing.
* Spring Boot packaging.

The repository shall include the Gradle Wrapper.

---

### BR-04 — Layered Architecture

The backend shall separate application responsibilities into appropriate layers.

The implementation shall include:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

The project shall maintain separate components for:

* Controllers.
* Services.
* Repositories.
* Entities.
* DTOs.
* Mappers.
* Exception handling.

---

## 5. Database Requirements

### DB-01 — PostgreSQL

The application shall use PostgreSQL as its relational database.

The configured PostgreSQL version for the project is:

```text
17
```

---

### DB-02 — Persistence

Build tasks shall be persisted in PostgreSQL.

The main persistent entity shall be:

```text
BuildTask
```

---

### DB-03 — Database Configuration

Database configuration shall be supplied through environment-based configuration.

The application shall support:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Database credentials shall not be hard-coded into the application source code.

---

### DB-04 — Local Database

Local development shall support PostgreSQL through Docker Compose.

The local database shall use a persistent Docker volume so that database data can survive container restarts.

---

## 6. API Requirements

### API-01 — Create Task

```text
POST /api/build-tasks
```

The endpoint shall create a new build task.

Expected successful response:

```text
201 Created
```

---

### API-02 — List Tasks

```text
GET /api/build-tasks
```

The endpoint shall return the available build tasks.

Expected successful response:

```text
200 OK
```

---

### API-03 — Get Task

```text
GET /api/build-tasks/{id}
```

The endpoint shall return the requested build task.

Expected successful response:

```text
200 OK
```

---

### API-04 — Update Task

```text
PUT /api/build-tasks/{id}
```

The endpoint shall update the requested build task.

Expected successful response:

```text
200 OK
```

---

### API-05 — Delete Task

```text
DELETE /api/build-tasks/{id}
```

The endpoint shall delete the requested build task.

Expected successful response:

```text
204 No Content
```

---

### API-06 — Health Endpoint

The application shall expose a Spring Boot Actuator health endpoint:

```text
/actuator/health
```

The endpoint shall provide a way to verify application availability.

---

## 7. Containerization Requirements

### CON-01 — Docker

The backend shall be packaged as a Docker container.

---

### CON-02 — Multi-Stage Build

The backend Dockerfile shall use separate build and runtime stages.

The build stage shall use a Java 21 JDK image.

The runtime stage shall use a Java 21 JRE image.

Only the generated application artifact shall be copied into the runtime image.

---

### CON-03 — Application Port

The containerized Spring Boot application shall expose:

```text
8081
```

---

### CON-04 — Docker Compose

Docker Compose shall provide a local environment containing:

* Backend service.
* PostgreSQL service.

The backend shall depend on the PostgreSQL health check before starting.

---

## 8. CI Requirements

### CI-01 — Jenkins

Jenkins shall automate the continuous-integration workflow.

The Jenkins pipeline shall include:

```text
Checkout
    ↓
Build
    ↓
Test
    ↓
Multi-Architecture Image Build
    ↓
Image Verification
```

---

### CI-02 — Automated Build

The Jenkins pipeline shall build the backend using the Gradle Wrapper.

The build shall be performed from the backend project directory.

---

### CI-03 — Automated Testing

The Jenkins pipeline shall execute the backend test suite.

The container image build shall only proceed when the required preceding stages succeed.

---

### CI-04 — Multi-Architecture Images

The CI pipeline shall build container images for:

```text
linux/amd64
linux/arm64
```

Docker Buildx shall be used for the multi-architecture build.

---

### CI-05 — Image Versioning

Published images shall support a build-specific tag based on the Jenkins build number.

The pipeline shall also publish the:

```text
latest
```

tag.

---

### CI-06 — Image Verification

The pipeline shall verify the published multi-architecture image using Docker Buildx image inspection.

---

## 9. Container Registry Requirements

### REG-01 — Azure Container Registry

The project shall use Azure Container Registry for storing published application images.

---

### REG-02 — Image Publishing

The CI pipeline shall publish the DevFlow backend image to the configured Azure Container Registry.

---

### REG-03 — Registry Credentials

Registry authentication credentials shall not be hard-coded in the source repository.

Jenkins-managed credentials shall be used for CI registry authentication.

---

## 10. Azure Requirements

### AZ-01 — Azure Resource Group

The cloud deployment shall use an Azure Resource Group to contain the DevFlow infrastructure.

---

### AZ-02 — Azure App Service

The backend shall be deployable as a Linux container on Azure App Service.

The App Service shall be configured to expose the application on port:

```text
8081
```

---

### AZ-03 — PostgreSQL Flexible Server

The cloud deployment shall use Azure Database for PostgreSQL Flexible Server.

The configured PostgreSQL major version shall be:

```text
17
```

---

### AZ-04 — Container Registry Integration

Azure App Service shall retrieve the application container image from Azure Container Registry.

---

### AZ-05 — Managed Identity

The App Service shall use a system-assigned managed identity for container registry authentication.

Registry authentication shall therefore avoid embedding registry credentials directly into the application configuration.

---

### AZ-06 — Database Configuration

Cloud database connection information shall be supplied to the application through environment-based application settings.

Sensitive database credentials shall be supplied through secure configuration mechanisms.

---

## 11. Infrastructure-as-Code Requirements

### IAC-01 — Terraform

Azure infrastructure shall be managed using Terraform.

---

### IAC-02 — Required Infrastructure

The Terraform configuration shall support provisioning of:

* Azure Resource Group.
* Azure Container Registry.
* Linux App Service Plan.
* Linux Web App.
* PostgreSQL Flexible Server.

---

### IAC-03 — Configuration Separation

Infrastructure configuration shall be separated from application source code.

Terraform configuration shall be stored under:

```text
infrastructure/terraform/
```

---

### IAC-04 — Sensitive Variables

Sensitive infrastructure values shall be represented using Terraform sensitive variables.

Examples include:

```text
subscription_id
tenant_id
db_password
```

The actual values shall never be committed to the repository.

---

### IAC-05 — Terraform State Protection

Terraform state files shall not be committed to source control.

The repository shall ignore Terraform state and variable files.

---

## 12. Testing Requirements

### TEST-01 — Automated Tests

The backend shall include automated tests.

The current test suite shall cover:

* Controller behavior.
* Service behavior.
* Application context.
* Database-backed integration behavior.

---

### TEST-02 — Controller Testing

REST controller behavior shall be tested using Spring's web testing facilities.

Tests shall verify successful responses and relevant error conditions.

---

### TEST-03 — Service Testing

Business logic shall be tested independently from the REST layer.

---

### TEST-04 — Integration Testing

The project shall include integration-oriented tests for persistence and application behavior.

---

### TEST-05 — Validation Testing

The API shall test invalid input scenarios, including missing required task information.

---

## 13. Security Requirements

### SEC-01 — No Credentials in Source Control

Passwords, access tokens, API keys, and other credentials shall not be committed to the repository.

---

### SEC-02 — Environment-Based Secrets

Sensitive application configuration shall be supplied through environment variables, CI/CD credential stores, or cloud-managed configuration.

---

### SEC-03 — Terraform Secret Protection

Terraform state and sensitive variable files shall be excluded from source control.

---

### SEC-04 — Jenkins Credential Management

CI registry credentials shall be stored using Jenkins credential management rather than directly in pipeline source code.

---

### SEC-05 — Managed Identity

Azure App Service shall use managed identity for Azure Container Registry authentication where configured by the infrastructure.

---

### SEC-06 — Secure Database Connection

The cloud PostgreSQL connection shall use an encrypted connection configuration.

---

## 14. Documentation Requirements

The project shall maintain technical documentation covering:

```text
docs/
├── requirements.md
├── architecture.md
├── api-design.md
└── deployment.md
```

The documentation shall describe the implemented architecture, API, deployment workflow, and project requirements without exposing secrets.

---

## 15. Repository Requirements

The repository shall maintain a clear separation between:

```text
backend/
docker/
jenkins/
infrastructure/
docs/
scripts/
```

Application source code, CI configuration, infrastructure definitions, and documentation shall remain logically separated.

---

## 16. Development Environment Requirements

The project shall support local development using:

* Java 21.
* Docker.
* Docker Compose.
* PostgreSQL.
* Gradle Wrapper.

The backend shall be runnable independently using the Gradle Wrapper.

---

## 17. Operational Requirements

The system shall provide mechanisms to verify application availability after deployment.

At minimum, deployment verification shall include:

1. Infrastructure verification.
2. Container image verification.
3. App Service verification.
4. Application health verification.
5. API verification.

---

## 18. Non-Functional Requirements

### NFR-01 — Reproducibility

Build and deployment processes should be reproducible across environments.

---

### NFR-02 — Portability

Container images shall support both:

```text
AMD64
ARM64
```

---

### NFR-03 — Maintainability

The project shall maintain separation of concerns between:

* Application logic.
* CI automation.
* Infrastructure.
* Deployment configuration.
* Documentation.

---

### NFR-04 — Configuration Management

Environment-specific configuration shall remain external to the application image.

---

### NFR-05 — Security

Sensitive information shall remain outside version-controlled documentation and source code.

---

## 19. Deployment Workflow Requirement

The implemented deployment workflow shall follow the general sequence:

```text
Source Code
    │
    ▼
Jenkins
    │
    ├── Build
    ├── Test
    └── Multi-Architecture Build
            │
            ├── linux/amd64
            └── linux/arm64
                    │
                    ▼
          Azure Container Registry
                    │
                    ▼
             Azure App Service
                    │
                    ▼
             Spring Boot API
                    │
                    ▼
          PostgreSQL Flexible Server
```

Terraform shall provision and manage the Azure infrastructure required by the cloud deployment.

---

## 20. Acceptance Criteria

The DevFlow project shall be considered functionally complete when:

* [x] The Spring Boot backend builds successfully.
* [x] The REST API provides build-task CRUD operations.
* [x] PostgreSQL persistence is functional.
* [x] Automated backend tests execute successfully.
* [x] The backend can be containerized.
* [x] The Docker image supports AMD64 and ARM64.
* [x] Jenkins automates build and test operations.
* [x] Jenkins publishes the multi-architecture image.
* [x] The published image can be verified.
* [x] Azure Container Registry stores the application image.
* [x] Azure App Service can run the containerized application.
* [x] Terraform defines the Azure infrastructure.
* [x] Application health can be verified through Actuator.
* [x] Sensitive credentials are excluded from source-controlled documentation and configuration.

---

## 21. Scope Boundaries

The following items are not considered mandatory implemented features unless explicitly configured elsewhere in the repository:

* Azure Key Vault.
* Private Azure networking.
* Container vulnerability scanning.
* Dependency vulnerability scanning.
* Automated production approval gates.
* Centralized logging infrastructure.
* Production monitoring and alerting.
* Separate staging and production environments.
* Remote Terraform state.

These may be added as future improvements but should not be represented as currently implemented features without corresponding project configuration.

---

## 22. Requirements Summary

DevFlow is intended to provide a complete and reproducible DevOps workflow combining:

```text
Spring Boot
     +
PostgreSQL
     +
Docker
     +
Docker Buildx
     +
Jenkins
     +
Azure Container Registry
     +
Azure App Service
     +
Terraform
```

The resulting platform provides a consistent path from source code through automated build and testing, multi-architecture container creation, registry publishing, infrastructure provisioning, and cloud application deployment.
