# DevFlow

**DevFlow** is a production-oriented Internal Developer Platform that automates the CI/CD lifecycle of a containerized Spring Boot application.

It combines **GitHub, Jenkins, Docker Buildx, Azure Container Registry, Azure DevOps Pipelines, Azure App Service, Terraform, PostgreSQL, Prometheus, and Grafana** into a single, reproducible DevOps workflow — from `git push` to a running, monitored service in production.

The goal of the project is to demonstrate a complete CI/CD platform, not just a standalone application.

---

## Table of Contents

* [Overview](#overview)
* [Architecture](#architecture)
* [Features](#features)
* [Technology Stack](#technology-stack)
* [Quick Start](#quick-start)
* [Getting Started](#getting-started)

  * [Prerequisites](#prerequisites)
  * [Clone the Repository](#clone-the-repository)
  * [Option A — Run Everything with Docker Compose](#option-a--run-everything-with-docker-compose)
  * [Option B — Run the Backend Manually](#option-b--run-the-backend-manually)
  * [Run Tests](#run-tests)
  * [Build the Application](#build-the-application)
* [REST API](#rest-api)
* [Backend Architecture](#backend-architecture)
* [Project Structure](#project-structure)
* [Environment Configuration](#environment-configuration)
* [Docker](#docker)
* [Jenkins CI Pipeline](#jenkins-ci-pipeline)
* [Infrastructure as Code (Terraform)](#infrastructure-as-code-terraform)
* [Azure Deployment](#azure-deployment)

  * [Azure Container Registry](#azure-container-registry)
  * [Azure App Service](#azure-app-service)
  * [Azure DevOps Deployment](#azure-devops-deployment)
* [Monitoring](#monitoring)
* [Security Hardening](#security-hardening)
* [Troubleshooting](#troubleshooting)
* [Useful Commands](#useful-commands)
* [Git Branch Strategy](#git-branch-strategy)
* [Contributing](#contributing)
* [Future Improvements](#future-improvements)
* [Project Validation Report](#project-validation-report)
* [Author](#author)
* [License](#license)

---

## Overview

DevFlow is split into two parts that work together:

1. **Application** — a Spring Boot REST API (`backend/`) for managing "build tasks", backed by PostgreSQL.
2. **Platform** — everything around the application: Docker images, a Jenkins CI pipeline, Terraform-managed Azure infrastructure, an Azure DevOps deployment pipeline, and Prometheus/Grafana monitoring.

You can run just the application locally in a couple of minutes (see [Quick Start](#quick-start)), or explore the full platform end to end.

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

## Features

* Spring Boot REST API for build-task management
* PostgreSQL persistence
* Layered backend architecture (Controller → DTO → Mapper → Service → Repository)
* RESTful CRUD operations
* Centralized exception handling
* Bean validation
* Multi-stage, multi-architecture Docker image (via Docker Buildx)
* Jenkins CI pipeline with automated tests
* Azure Container Registry + Azure App Service deployment
* Azure DevOps deployment pipeline with post-deploy health checks
* Terraform Infrastructure as Code
* Prometheus metrics and Grafana dashboards
* Health and readiness checks (Spring Boot Actuator)
* Managed identity based Azure authentication (no ACR admin credentials)
* Hardened Actuator endpoint exposure
* Terraform state and variable files excluded from Git

---

## Technology Stack

### Backend

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

### DevOps

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

### Monitoring

| Technology           | Purpose                        |
| -------------------- | ------------------------------ |
| Prometheus           | Metrics collection             |
| Grafana              | Monitoring and visualization   |
| Spring Boot Actuator | Application metrics and health |

---

## Quick Start

The fastest way to see DevFlow running locally is Docker Compose — it starts the backend, PostgreSQL, Prometheus, and Grafana together.

```bash
git clone https://github.com/Mehdi-MI/devflow.git
cd devflow
git checkout develop

docker compose up -d
```

Then check that everything is healthy:

```bash
# Backend health
curl http://localhost:8081/actuator/health

# Build-task API
curl http://localhost:8081/api/build-tasks
```

| Service     | URL                                                     |
| ----------- | ------------------------------------------------------- |
| Backend API | http://localhost:8081/api/build-tasks                   |
| Health      | http://localhost:8081/actuator/health                   |
| Prometheus  | http://localhost:9090                                   |
| Grafana     | http://localhost:3000 (default login `admin` / `admin`) |

Stop everything with `docker compose down`. For more detail — running without Docker, running tests, configuring the database, etc. — see [Getting Started](#getting-started) below.

---

## Getting Started

### Prerequisites

Only the first four are required to run the application locally. The rest are only needed if you want to work with the infrastructure/CI parts of the project.

| Tool                                 | Required for                                             |
| ------------------------------------ | -------------------------------------------------------- |
| Git                                  | Cloning the repository                                   |
| Java 21 (JDK)                        | Building/running the backend directly                    |
| Docker & Docker Compose              | Running the app and its dependencies in containers       |
| Gradle Wrapper (`gradlew`, included) | Building the backend — no separate Gradle install needed |
| Azure CLI                            | Interacting with Azure resources (optional)              |
| Terraform                            | Managing the Azure infrastructure (optional)             |

### Clone the Repository

```bash
git clone https://github.com/Mehdi-MI/devflow.git
cd devflow
git checkout develop
```

> If you have an SSH key configured on GitHub, you can use `git@github.com:Mehdi-MI/devflow.git` instead.

There are two ways to run the backend locally: with Docker Compose (recommended, no local Java/PostgreSQL setup needed), or manually with Gradle and your own PostgreSQL instance.

### Option A — Run Everything with Docker Compose

```bash
docker compose up -d
```

This starts the backend, PostgreSQL, Prometheus, and Grafana. Check status:

```bash
docker compose ps
```

Stop everything:

```bash
docker compose down
```

If you only want the database (for example, to run the backend from your IDE):

```bash
docker compose up -d postgres
```

### Option B — Run the Backend Manually

1. Start PostgreSQL (via Docker Compose, or your own local instance) so it's reachable at `localhost:5432` with database `devflow`.
2. Run the backend:

```bash
cd backend
./gradlew bootRun
```

The application starts on:

```text
http://localhost:8081
```

### Run Tests

```bash
cd backend
./gradlew clean test
```

Expected result:

```text
BUILD SUCCESSFUL
```

### Build the Application

```bash
cd backend
./gradlew clean build
```

Expected result:

```text
BUILD SUCCESSFUL
```

The resulting JAR is produced under `backend/build/libs/`.

---

## REST API

The application exposes the following build-task endpoints:

| Method | Endpoint                | Description         |
| ------ | ----------------------- | ------------------- |
| POST   | `/api/build-tasks`      | Create a build task |
| GET    | `/api/build-tasks`      | List build tasks    |
| GET    | `/api/build-tasks/{id}` | Get a build task    |
| PUT    | `/api/build-tasks/{id}` | Update a build task |
| DELETE | `/api/build-tasks/{id}` | Delete a build task |

Local example:

```bash
curl http://localhost:8081/api/build-tasks
```

Production endpoint:

```bash
curl https://azure-cicd-mehdi.azurewebsites.net/api/build-tasks
```

### Build Task Model

A build task contains:

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

## Backend Architecture

The backend follows a layered architecture:

```text
Controller → DTO → Mapper → Service → Repository → PostgreSQL
```

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

## Project Structure

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
│   ├── pipelines/
│   │   └── azure-pipelines.yml
│   └── templates/
│
├── docker/
│   ├── development/
│   └── production/
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
├── scripts/
│   ├── build.sh
│   ├── clean.sh
│   ├── run.sh
│   └── stop.sh
│
├── Jenkinsfile
├── docker-compose.yml
├── README.md
└── LICENSE
```

---

## Environment Configuration

The backend reads its database configuration from environment variables, with local-friendly defaults so it also runs out of the box without any extra setup.

| Variable            | Used by                   | Default                                    |
| ------------------- | ------------------------- | ------------------------------------------ |
| `DB_URL`            | backend                   | `jdbc:postgresql://localhost:5432/devflow` |
| `DB_USERNAME`       | backend                   | `postgres`                                 |
| `DB_PASSWORD`       | backend                   | `postgres`                                 |
| `POSTGRES_USER`     | docker-compose (postgres) | `postgres`                                 |
| `POSTGRES_PASSWORD` | docker-compose (postgres) | `postgres`                                 |

`application.yaml` example:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/devflow}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
```

For production, these values are supplied externally (Azure App Service configuration) rather than committed to the repository.

---

## Docker

The backend uses a multi-stage Docker build (build stage on `eclipse-temurin:21-jdk`, runtime on `eclipse-temurin:21-jre`).

Build locally:

```bash
cd backend
docker build -t devflow-backend:local .
```

Run:

```bash
docker run -p 8081:8081 devflow-backend:local
```

### Docker Compose

Validate the Compose configuration:

```bash
docker compose config -q
```

Start the complete local platform (backend, PostgreSQL, Prometheus, Grafana):

```bash
docker compose up -d
```

Check running services:

```bash
docker compose ps
```

Stop the environment:

```bash
docker compose down
```

---

## Jenkins CI Pipeline

Jenkins provides the continuous integration layer. The pipeline:

```text
Checkout → Gradle Build → Automated Tests → Docker Buildx → Multi-Architecture Image → Azure Container Registry → Image Verification
```

It builds `linux/amd64` and `linux/arm64` images using Docker Buildx and pushes to:

```text
mehdihsb.azurecr.io/devflow-backend
```

tagged as `:<BUILD_NUMBER>` and `:latest`. The pipeline then verifies the published image with:

```bash
docker buildx imagetools inspect
```

---

## Infrastructure as Code (Terraform)

Azure infrastructure is managed with Terraform, in `infrastructure/terraform/`:

```text
main.tf
variables.tf
outputs.tf
versions.tf
.gitignore
```

```bash
cd infrastructure/terraform

terraform fmt -check
terraform validate
terraform plan
```

Terraform state and variable files are intentionally excluded from Git:

```text
terraform.tfstate
terraform.tfstate.backup
terraform.tfvars
```

This keeps sensitive infrastructure state and configuration out of the repository.

---

## Azure Deployment

### Azure Container Registry

```text
Registry:   mehdihsb.azurecr.io
Repository: devflow-backend
```

Check available images:

```bash
az acr repository show-tags \
  --name mehdihsb \
  --repository devflow-backend \
  --orderby time_desc \
  --output table
```

### Azure App Service

```text
App:            azure-cicd-mehdi
Resource group: rg-cicd-spain
URL:             https://azure-cicd-mehdi.azurewebsites.net
```

### Azure DevOps Deployment

Azure Pipelines deploys the latest image from ACR to Azure App Service:

```text
Jenkins (build + test) → Docker Buildx → Azure Container Registry → Azure DevOps Pipeline (AzureWebAppContainer) → Azure App Service
```

After deployment, the pipeline runs a health check:

```bash
curl https://azure-cicd-mehdi.azurewebsites.net/actuator/health
```

and only considers the deployment successful once the app returns:

```json
{ "status": "UP" }
```

---

## Monitoring

DevFlow exposes Prometheus metrics through Spring Boot Actuator.

```text
Local:      /actuator/prometheus
Production: https://azure-cicd-mehdi.azurewebsites.net/actuator/prometheus
```

Example metrics: `application_ready_time_seconds`, `hikaricp_connections`, `http_server_requests`, `jvm_memory`, `jvm_threads`, `process_cpu`, `system_cpu`, and more.

Grafana is included for visualization, provisioned with a Prometheus datasource and a **DevFlow Backend Status** dashboard.

Health endpoint:

```text
Local:      /actuator/health
Production: https://azure-cicd-mehdi.azurewebsites.net/actuator/health
```

Expected response:

```json
{
  "groups": ["liveness", "readiness"],
  "status": "UP"
}
```

---

## Security Hardening

* **Actuator exposure** — only `health` and `prometheus` are exposed (`info` and `metrics` are not):

  ```yaml
  management:
    endpoints:
      web:
        exposure:
          include: health,prometheus
  ```
* **ACR authentication** — ACR admin authentication is disabled; deployment uses Azure identity-based authentication (least privilege) instead.
* **Grafana authentication** — Grafana's admin account is used for provisioning and dashboard verification, not exposed publicly by default.
* **Terraform secrets** — `*.tfstate`, `*.tfstate.*`, and `*.tfvars` are gitignored. The repository does not track any `.env`, secret, credential, or password files.

---

## Troubleshooting

| Symptom                                                               | Likely cause / fix                                                                                                                                   |
| --------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- |
| `Port 8081/5432/9090/3000 already in use`                             | Another process is using the port. Stop it, or change the host-side port mapping in `docker-compose.yml` (e.g. `"8082:8081"`).                       |
| `./gradlew: Permission denied`                                        | Make the wrapper executable: `chmod +x gradlew`.                                                                                                     |
| Backend can't connect to PostgreSQL                                   | Make sure `postgres` is running and healthy first (`docker compose ps`), or that `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` point to a reachable database. |
| `docker compose up` fails on `depends_on: condition: service_healthy` | Your Docker/Compose version may be too old. Update Docker Desktop or the Compose plugin.                                                             |
| Docker Buildx errors in the Jenkins pipeline                          | The Buildx builder instance may need to be recreated: `docker buildx create --use`.                                                                  |
| Changes to `application.yaml` aren't picked up                        | Restart the backend (`./gradlew bootRun` or `docker compose restart backend`) — it isn't hot-reloaded by default.                                    |

If you hit something not listed here, please open an issue with the command you ran and the full output.

---

## Useful Commands

### Git

```bash
git status
git log --oneline -10
git pull origin develop
git push origin develop
```

### Backend

```bash
cd backend

./gradlew clean test
./gradlew clean build
./gradlew bootRun
```

### Docker

```bash
docker compose config -q
docker compose up -d
docker compose ps
docker compose down
```

### Terraform

```bash
cd infrastructure/terraform

terraform fmt -check
terraform validate
terraform plan
```

### Azure

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

### Production Health & API

```bash
curl https://azure-cicd-mehdi.azurewebsites.net/actuator/health
curl https://azure-cicd-mehdi.azurewebsites.net/api/build-tasks
```

---

## Git Branch Strategy

`develop` is the active development branch. The repository is maintained through regular Git commits and kept synchronized with GitHub.

---

## Contributing

Contributions and suggestions are welcome:

1. Fork the repository and create a feature branch from `develop`.
2. Make your changes, following the existing layered backend structure (Controller → DTO → Mapper → Service → Repository).
3. Run the backend test suite before opening a pull request:

   ```bash
   cd backend
   ./gradlew clean test
   ```
4. Open a pull request against `develop` with a clear description of the change.

For larger changes (new infrastructure, new pipeline stages), please open an issue first to discuss the approach.

---

## Future Improvements

Potential future improvements (optional, not required for the current release):

* API authentication and authorization (Spring Security)
* Azure Key Vault integration
* Container vulnerability scanning
* Automated dependency scanning
* SonarQube or SonarCloud integration
* Automated semantic versioning
* Pull-request quality gates
* Blue/green deployments and rollback automation
* Azure Application Insights integration
* Automated Grafana alerting
* Kubernetes / AKS deployment
* Horizontal application scaling
* Database backup automation

---

## Project Validation Report

This section is a historical record from the project's final end-to-end validation pass — kept for transparency and as a portfolio reference. It is **not** required reading to use the project; see [Quick Start](#quick-start) instead.

<details>
<summary>Expand full validation report</summary>

### Repository

```text
Git status: clean
Branch: develop
Remote: synchronized
```

### Backend

```text
Gradle tests: PASS
```

### Terraform

```text
terraform validate: PASS
terraform fmt -check: PASS
```

### Docker

```text
docker compose config -q: PASS
All local platform services: RUNNING
PostgreSQL: HEALTHY
```

### Resilience

```text
Backend container restart: PASS
Backend health after restart: HTTP 200
PostgreSQL restart: PASS
PostgreSQL health after restart: HEALTHY
Backend recovery after database restart: HTTP 200
Database API verification: HTTP 200
```

### Monitoring

```text
Prometheus backend target: UP
Prometheus datasource: CONFIGURED
Grafana dashboard: AVAILABLE
Grafana health: HTTP 200
Grafana database: OK
```

### Azure

```text
App Service: Running
Health endpoint: HTTP 200
Prometheus endpoint: HTTP 200
API endpoint: HTTP 200
```

### Security

```text
Actuator info: not exposed
Actuator metrics: not exposed
Terraform state: not tracked
Terraform variables: not tracked
Secret-named files: not tracked
ACR admin authentication: disabled
```

### CI/CD

```text
Jenkins build: PASS
Jenkins tests: PASS
Multi-architecture image: PUBLISHED
Azure Container Registry: VERIFIED
Azure App Service: RUNNING
Deployment health check: PASS
```

### Current Release State

The project completed its final end-to-end validation and portfolio-readiness review.

Latest commit at that time:

```text
a71cd64 Improve global API error handling
```

Repository status at final validation:

```text
develop...origin/develop
working tree clean
```

The project successfully passed: backend tests, Terraform formatting and configuration validation, Docker Compose validation, Git repository/diff/security audit, Jenkins CI validation, automated PostgreSQL integration-test environment, automatic Jenkins Docker Buildx builder recovery, multi-architecture Docker builds and publishing (`linux/amd64`, `linux/arm64`), Azure Container Registry and App Service validation, production health validation, Prometheus/Grafana validation, Actuator security validation, and backend/PostgreSQL recovery validation.

</details>

### Project Objectives Achieved

DevFlow demonstrates the implementation of a complete DevOps platform covering:

```text
Application Development + Containerization + Continuous Integration
+ Multi-Architecture Builds + Container Registry + Infrastructure as Code
+ Continuous Deployment + Cloud Hosting + Monitoring
+ Security Hardening + Resilience Testing + Automated Validation
```

The project goes beyond a simple Spring Boot application, demonstrating practical integration of software engineering, containerization, CI/CD, cloud infrastructure, observability, security practices, and resilience validation.

---

## Author

**Mehdi Hasbellaoui**

* GitHub: https://github.com/Mehdi-MI
* Repository: https://github.com/Mehdi-MI/devflow

---

## License

This project is licensed under the terms specified in [`LICENSE`](./LICENSE).
