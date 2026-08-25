# DevFlow

**DevFlow** is a production-oriented Internal Developer Platform designed to automate the CI/CD lifecycle of a containerized Spring Boot application.

The platform combines **GitHub, Jenkins, Docker Buildx, Azure Container Registry, Azure DevOps Pipelines, Azure App Service, Terraform, PostgreSQL, Prometheus, and Grafana** into an end-to-end DevOps workflow.

The goal is to demonstrate a complete, reproducible CI/CD platform rather than a simple application deployment.

---

## Architecture

```text
                         ┌──────────────────┐
                         │     Developer    │
                         └────────┬─────────┘
                                  │
                                  │ git push
                                  ▼
                         ┌──────────────────┐
                         │      GitHub      │
                         │   devflow repo   │
                         └────────┬─────────┘
                                  │
                                  │ webhook / pipeline trigger
                                  ▼
                    ┌──────────────────────────┐
                    │         Jenkins          │
                    │      CI Controller       │
                    └────────────┬─────────────┘
                                 │
                                 │
                    ┌────────────▼─────────────┐
                    │      Docker Buildx       │
                    │     Multi-Architecture   │
                    │                          │
                    │   linux/amd64            │
                    │   linux/arm64            │
                    └────────────┬─────────────┘
                                 │
                                 │ docker push
                                 ▼
                    ┌──────────────────────────┐
                    │   Azure Container        │
                    │       Registry           │
                    │                          │
                    │ devflow-backend:latest   │
                    └────────────┬─────────────┘
                                 │
                                 │ deploy image
                                 ▼
                    ┌──────────────────────────┐
                    │    Azure App Service     │
                    │     Linux Container      │
                    │                          │
                    │     Spring Boot API      │
                    └────────────┬─────────────┘
                                 │
                         ┌───────┴────────┐
                         │                │
                         ▼                ▼
                ┌────────────────┐  ┌───────────────┐
                │   PostgreSQL   │  │  Prometheus   │
                │    Database    │  │    Metrics    │
                └────────────────┘  └───────┬───────┘
                                            │
                                            ▼
                                     ┌─────────────┐
                                     │   Grafana   │
                                     │ Monitoring  │
                                     └─────────────┘
```

---

# Features

* Spring Boot REST API for build-task management
* PostgreSQL persistence
* Layered backend architecture
* DTO, Mapper, Service and Repository patterns
* RESTful CRUD operations
* Centralized exception handling
* Bean validation
* Docker multi-stage image
* Multi-architecture Docker images
* Docker Buildx
* Jenkins CI pipeline
* Azure Container Registry
* Azure App Service deployment
* Azure DevOps deployment pipeline
* Terraform Infrastructure as Code
* Prometheus metrics
* Grafana monitoring
* Application logging
* Health and readiness checks
* GitHub integration
* Automated deployment verification
* Managed identity based Azure authentication
* ACR admin authentication disabled
* Actuator endpoint exposure hardened
* Terraform state and variable files excluded from Git
* Automated backend testing
* Container and database resilience validation
* Production-readiness validation

---

# Technology Stack

## Backend

| Technology           | Purpose               |
| -------------------- | --------------------- |
| Java 21              | Application runtime   |
| Spring Boot 4.1      | Backend framework     |
| Spring Data JPA      | Persistence           |
| Hibernate            | ORM                   |
| PostgreSQL 17        | Database              |
| Gradle               | Build system          |
| MapStruct            | DTO mapping           |
| Lombok               | Boilerplate reduction |
| Bean Validation      | Request validation    |
| Spring Boot Actuator | Health and metrics    |

## DevOps

| Technology               | Purpose                   |
| ------------------------ | ------------------------- |
| GitHub                   | Source control            |
| Jenkins                  | Continuous Integration    |
| Docker                   | Containerization          |
| Docker Buildx            | Multi-architecture builds |
| Azure Container Registry | Container image registry  |
| Azure App Service        | Production hosting        |
| Azure DevOps Pipelines   | Deployment automation     |
| Terraform                | Infrastructure as Code    |
| Docker Compose           | Local development         |

## Monitoring

| Technology           | Purpose                        |
| -------------------- | ------------------------------ |
| Prometheus           | Metrics collection             |
| Grafana              | Monitoring and visualization   |
| Spring Boot Actuator | Application metrics and health |

---

# Backend Architecture

The backend follows a layered architecture:

```text
Controller
    │
    ▼
DTO
    │
    ▼
Mapper
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

The application contains:

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/mehdi/devflow/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── entity/
│   │   │       ├── enum/
│   │   │       ├── exception/
│   │   │       ├── mapper/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
├── Dockerfile
├── build.gradle.kts
└── gradlew
```

---

# REST API

The application exposes the following build-task endpoints.

| Method | Endpoint                | Description         |
| ------ | ----------------------- | ------------------- |
| POST   | `/api/build-tasks`      | Create a build task |
| GET    | `/api/build-tasks`      | List build tasks    |
| GET    | `/api/build-tasks/{id}` | Get a build task    |
| PUT    | `/api/build-tasks/{id}` | Update a build task |
| DELETE | `/api/build-tasks/{id}` | Delete a build task |

Example:

```bash
curl http://localhost:8081/api/build-tasks
```

Production endpoint:

```bash
curl https://azure-cicd-mehdi.azurewebsites.net/api/build-tasks
```

---

# Build Task Model

A build task contains information such as:

```text
id
name
repositoryUrl
branch
status
logs
createdAt
startedAt
completedAt
```

Supported statuses:

```text
PENDING
RUNNING
SUCCESS
FAILED
```

---

# Local Development

## Prerequisites

Install:

* Java 21
* Docker
* Docker Compose
* Git
* Gradle Wrapper
* Azure CLI for Azure operations
* Terraform for infrastructure operations

---

## Clone the Repository

```bash
git clone git@github.com:Mehdi-MI/devflow.git
cd devflow
```

---

# Start PostgreSQL

The project provides Docker Compose configuration for local development.

```bash
docker compose up -d postgres
```

Check the containers:

```bash
docker compose ps
```

---

# Run the Backend

```bash
cd backend
./gradlew bootRun
```

The application runs on:

```text
http://localhost:8081
```

---

# Run Tests

From the backend directory:

```bash
./gradlew clean test
```

Expected result:

```text
BUILD SUCCESSFUL
```

The backend test suite was successfully validated during the final project review.

---

# Build the Application

```bash
cd backend
./gradlew clean build
```

---

# Docker

The backend uses a multi-stage Docker build.

Build locally:

```bash
cd backend
docker build -t devflow-backend:local .
```

Run:

```bash
docker run \
  -p 8081:8081 \
  devflow-backend:local
```

---

# Docker Compose

Validate the Compose configuration:

```bash
docker compose config -q
```

Start the complete local platform:

```bash
docker compose up -d
```

The local stack includes:

```text
Spring Boot Backend
PostgreSQL
Prometheus
Grafana
```

Check the running services:

```bash
docker compose ps
```

Stop the environment:

```bash
docker compose down
```

---

# Jenkins CI Pipeline

Jenkins provides the continuous integration layer.

The pipeline performs:

```text
Checkout
   │
   ▼
Gradle Build
   │
   ▼
Automated Tests
   │
   ▼
Docker Buildx
   │
   ▼
Multi-Architecture Image
   │
   ▼
Azure Container Registry
   │
   ▼
Image Verification
```

The Jenkins pipeline builds:

```text
linux/amd64
linux/arm64
```

using Docker Buildx.

The resulting image is pushed to:

```text
mehdihsb.azurecr.io/devflow-backend
```

with:

```text
:<BUILD_NUMBER>
:latest
```

The pipeline also verifies the published image using:

```bash
docker buildx imagetools inspect
```

The Jenkins pipeline was successfully validated with Gradle build and test stages, automated PostgreSQL integration support, Docker Buildx builder recovery, and multi-architecture image publishing.

---

# Azure Container Registry

The project uses Azure Container Registry:

```text
mehdihsb.azurecr.io
```

Repository:

```text
devflow-backend
```

Available images include versioned build tags and:

```text
latest
```

Check available images:

```bash
az acr repository show-tags \
  --name mehdihsb \
  --repository devflow-backend \
  --orderby time_desc \
  --output table
```

The final validation confirmed that the ACR repository contains published multi-architecture image tags.

---

# Azure App Service

The production application is deployed to Azure App Service.

Application:

```text
azure-cicd-mehdi
```

Resource group:

```text
rg-cicd-spain
```

Production URL:

```text
https://azure-cicd-mehdi.azurewebsites.net
```

The App Service was verified as:

```text
Running
```

The production health endpoint successfully returned HTTP 200 with status `UP`.

---

# Azure DevOps Deployment

Azure Pipelines is responsible for deploying the image from ACR to Azure App Service.

Deployment flow:

```text
Jenkins
   │
   │ build + test
   ▼
Docker Buildx
   │
   │ push
   ▼
Azure Container Registry
   │
   │ latest image
   ▼
Azure DevOps Pipeline
   │
   │ AzureWebAppContainer
   ▼
Azure App Service
```

The deployment pipeline performs a post-deployment health check:

```bash
curl \
  https://azure-cicd-mehdi.azurewebsites.net/actuator/health
```

The deployment is considered successful only when the application returns:

```json
{
  "status": "UP"
}
```

The end-to-end Azure deployment was validated successfully.

---

# Infrastructure as Code

Azure infrastructure is managed with Terraform.

Terraform files are located in:

```text
infrastructure/terraform/
```

Main files:

```text
main.tf
variables.tf
outputs.tf
versions.tf
.gitignore
```

Validate Terraform formatting:

```bash
terraform fmt -check
```

Validate the configuration:

```bash
terraform validate
```

The final validation returned:

```text
Success! The configuration is valid.
```

Terraform state and variable files are intentionally excluded from Git:

```text
terraform.tfstate
terraform.tfstate.backup
terraform.tfvars
```

This prevents sensitive infrastructure state and configuration from being committed to the repository.

---

# Monitoring

DevFlow exposes Prometheus metrics through Spring Boot Actuator.

Prometheus endpoint:

```text
/actuator/prometheus
```

Production:

```text
https://azure-cicd-mehdi.azurewebsites.net/actuator/prometheus
```

The endpoint was validated successfully with HTTP 200.

The local Prometheus stack successfully verified the backend target:

```text
job: devflow-backend
instance: backend:8081
up: 1
```

Example metrics include:

```text
application_ready_time_seconds
application_started_time_seconds
disk_free_bytes
disk_total_bytes
executor_active_threads
hikaricp_connections
http_server_requests
jdbc_connections
jvm_memory
jvm_threads
process_cpu
system_cpu
```

Grafana is included for visualization.

The final local monitoring validation confirmed:

```text
Prometheus datasource: configured
Datasource URL: http://prometheus:9090
Grafana dashboard: DevFlow Backend Status
Grafana database: healthy
```

---

# Health Monitoring

The production health endpoint is:

```text
/actuator/health
```

Production:

```text
https://azure-cicd-mehdi.azurewebsites.net/actuator/health
```

Expected response:

```json
{
  "groups": [
    "liveness",
    "readiness"
  ],
  "status": "UP"
}
```

The final validation returned:

```text
HTTP_STATUS:200
```

with application status:

```text
UP
```

---

# Resilience Validation

The local platform was tested for application and database recovery.

## Backend Container Recovery

The backend container was restarted during validation.

Results:

```text
Backend before restart: HTTP 200
Backend after restart: HTTP 200
Application status after recovery: UP
Database API access after recovery: HTTP 200
```

## PostgreSQL Recovery

PostgreSQL was restarted while the backend remained running.

Results:

```text
PostgreSQL after restart: healthy
Backend health after database recovery: HTTP 200
Application status: UP
Database API access after recovery: HTTP 200
```

This validation demonstrated successful recovery of the application and database services in the Docker Compose environment.

---

# Security Hardening

Security was explicitly reviewed during the final project phases.

## Actuator Exposure

The application exposes only:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
```

Therefore:

```text
/actuator/health       → exposed
/actuator/prometheus   → exposed
/actuator/info         → not exposed
/actuator/metrics      → not exposed
```

The production security validation confirmed:

```text
/actuator/health      → HTTP 200
/actuator/prometheus  → HTTP 200
/actuator/info        → HTTP 500
/actuator/metrics     → HTTP 500
```

The latter two are not registered as exposed Actuator endpoints.

---

## ACR Authentication

ACR administrative authentication was disabled.

The deployment uses Azure identity-based authentication rather than relying on the ACR administrator account.

A dedicated Jenkins identity is used for ACR operations.

This follows the principle of least privilege.

---

## Grafana Authentication

Grafana administrative access was reviewed during final monitoring validation.

The configured Grafana instance successfully authenticated the administrator account and the API verification returned the expected administrative user information.

The monitoring stack was then validated through:

```text
Grafana health: HTTP 200
Database: OK
Prometheus datasource: configured
DevFlow Backend Status dashboard: available
```

---

## Terraform Secrets

Terraform state and variable files are ignored:

```text
*.tfstate
*.tfstate.*
*.tfvars
```

A Git history audit confirmed that Terraform state and variable files were never tracked.

The repository also contains no tracked `.env`, secret, credential, password, or Terraform state files.

---

# Environment Configuration

Database configuration uses environment variables rather than requiring credentials to be hardcoded into the application.

Example:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/devflow}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
```

For production deployments, environment-specific values are supplied externally.

---

# CI/CD Workflow

The complete DevFlow workflow is:

```text
1. Developer pushes code
          │
          ▼
2. GitHub repository
          │
          ▼
3. Jenkins checkout
          │
          ▼
4. Gradle build
          │
          ▼
5. Automated tests
          │
          ▼
6. Docker Buildx
          │
          ├───────────────┐
          │               │
          ▼               ▼
      AMD64             ARM64
          │               │
          └───────┬───────┘
                  ▼
7. Multi-architecture image
                  │
                  ▼
8. Azure Container Registry
                  │
                  ▼
9. Azure DevOps Pipeline
                  │
                  ▼
10. Azure App Service
                  │
                  ▼
11. Production health check
                  │
                  ▼
12. DevFlow application running
```

---

# Validation

The final production-readiness validation covered:

## Repository

```text
Git status: clean
Branch: develop
Remote: synchronized
```

## Backend

```text
Gradle tests: PASS
```

## Terraform

```text
terraform validate: PASS
terraform fmt -check: PASS
```

## Docker

```text
docker compose config -q: PASS
All local platform services: RUNNING
PostgreSQL: HEALTHY
```

## Resilience

```text
Backend container restart: PASS
Backend health after restart: HTTP 200
PostgreSQL restart: PASS
PostgreSQL health after restart: HEALTHY
Backend recovery after database restart: HTTP 200
Database API verification: HTTP 200
```

## Monitoring

```text
Prometheus backend target: UP
Prometheus datasource: CONFIGURED
Grafana dashboard: AVAILABLE
Grafana health: HTTP 200
Grafana database: OK
```

## Azure

```text
App Service: Running
Health endpoint: HTTP 200
Prometheus endpoint: HTTP 200
API endpoint: HTTP 200
```

## Security

```text
Actuator info: not exposed
Actuator metrics: not exposed
Terraform state: not tracked
Terraform variables: not tracked
Secret-named files: not tracked
ACR admin authentication: disabled
```

## CI/CD

```text
Jenkins build: PASS
Jenkins tests: PASS
Multi-architecture image: PUBLISHED
Azure Container Registry: VERIFIED
Azure App Service: RUNNING
Deployment health check: PASS
```

---

# Project Structure

```text
devflow/
│
├── architecture.drawio
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   ├── build.gradle.kts
│   ├── Dockerfile
│   ├── gradlew
│   └── settings.gradle.kts
│
├── azure/
│   └── pipelines/
│       └── azure-pipelines.yml
│
├── docker/
│   └── compose/
│
├── infrastructure/
│   ├── bicep/
│   └── terraform/
│       ├── main.tf
│       ├── variables.tf
│       ├── outputs.tf
│       ├── versions.tf
│       └── .gitignore
│
├── jenkins/
│   ├── agents/
│   ├── pipelines/
│   └── Jenkinsfile
│
├── monitoring/
│   ├── grafana/
│   └── prometheus/
│
├── Jenkinsfile
├── docker-compose.yml
├── README.md
└── LICENSE
```

---

# Useful Commands

## Git

```bash
git status
git log --oneline -10
git pull origin develop
git push origin develop
```

## Backend

```bash
cd backend

./gradlew clean test
./gradlew clean build
./gradlew bootRun
```

## Docker

```bash
docker compose config -q
docker compose up -d
docker compose ps
docker compose down
```

## Terraform

```bash
cd infrastructure/terraform

terraform fmt -check
terraform validate
terraform plan
```

## Azure

```bash
az login

az acr repository show-tags \
  --name mehdihsb \
  --repository devflow-backend \
  --orderby time_desc \
  --output table

az webapp show \
  --name azure-cicd-mehdi \
  --resource-group rg-cicd-spain \
  --query "{name:name,state:state,hostName:defaultHostName}"
```

## Production Health

```bash
curl https://azure-cicd-mehdi.azurewebsites.net/actuator/health
```

## Production API

```bash
curl https://azure-cicd-mehdi.azurewebsites.net/api/build-tasks
```

---

# Git Branch Strategy

The project uses:

```text
develop
```

as the active development branch.

The repository is maintained through Git commits and remote synchronization with GitHub.

---

# Current Release State

The project has completed its final end-to-end validation and portfolio readiness review.

Latest commit:

```text
a71cd64 Improve global API error handling
```

Repository status at final validation:

```text
develop...origin/develop
working tree clean
```

The project successfully passed:

* Backend tests
* Terraform formatting validation
* Terraform configuration validation
* Docker Compose validation
* Git repository validation
* Git diff validation
* Git security audit
* Jenkins CI validation
* Automated PostgreSQL integration-test environment
* Automatic Jenkins Docker Buildx builder recovery
* Multi-architecture Docker builds for `linux/amd64` and `linux/arm64`
* Multi-architecture Docker image publishing
* Azure Container Registry validation
* Azure App Service validation
* Production health validation
* Prometheus endpoint validation
* Prometheus target validation
* Grafana authentication and health validation
* Grafana datasource and dashboard validation
* Actuator security validation
* Backend container recovery validation
* PostgreSQL recovery validation
* End-to-end CI/CD validation

---

# Future Improvements

Potential future improvements include:

* API authentication and authorization
* Spring Security integration
* Azure Key Vault integration
* Container vulnerability scanning
* Automated dependency scanning
* SonarQube or SonarCloud integration
* Automated semantic versioning
* Pull-request quality gates
* Blue/green deployments
* Deployment rollback automation
* Azure Application Insights integration
* Automated Grafana alerting
* Kubernetes / AKS deployment
* Horizontal application scaling
* Database backup automation

These are optional extensions and are not required for the current release.

---

# Project Objectives Achieved

DevFlow demonstrates the implementation of a complete DevOps platform covering:

```text
Application Development
        +
Containerization
        +
Continuous Integration
        +
Multi-Architecture Builds
        +
Container Registry
        +
Infrastructure as Code
        +
Continuous Deployment
        +
Cloud Hosting
        +
Monitoring
        +
Security Hardening
        +
Resilience Testing
        +
Automated Validation
```

The project therefore goes beyond a simple Spring Boot application and demonstrates practical integration of software engineering, containerization, CI/CD, cloud infrastructure, observability, security practices, and resilience validation.

---

# Author

**Mehdi Hasbellaoui**

GitHub:

```text
https://github.com/Mehdi-MI
```

Repository:

```text
https://github.com/Mehdi-MI/devflow
```

---

# License

This project is licensed under the terms specified in `LICENSE`.
