# DevFlow — Multi-Architecture CI/CD Platform

DevFlow is a DevOps platform that automates the build, containerization, and deployment workflow for a Spring Boot application.

The project combines **Jenkins**, **Docker Buildx**, **Azure Container Registry (ACR)**, **Azure DevOps Pipelines**, **Azure App Service**, **PostgreSQL**, and **Terraform** to create a reproducible CI/CD workflow supporting **AMD64 and ARM64** container images.

The goal is to reduce repetitive manual DevOps operations and provide a consistent workflow from source code to cloud deployment.

---

## Architecture

```text
                         ┌─────────────────┐
                         │     GitHub      │
                         │  Source Control │
                         └────────┬────────┘
                                  │
                                  │ Push
                                  ▼
                         ┌─────────────────┐
                         │     Jenkins     │
                         │     Pipeline    │
                         └────────┬────────┘
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
                         │ Azure DevOps    │
                         │    Pipeline     │
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
                         │    + PostgreSQL │
                         └─────────────────┘
```

The platform follows this general workflow:

```text
Developer
   │
   ▼
GitHub
   │
   ▼
Jenkins
   │
   ├── Build
   ├── Test
   └── Docker Buildx
          │
          ├── AMD64
          └── ARM64
                │
                ▼
        Azure Container Registry
                │
                ▼
        Azure DevOps Pipeline
                │
                ▼
        Azure App Service
```

---

## Features

* Spring Boot REST API for build-task management
* PostgreSQL persistence
* Dockerized backend
* Multi-stage Docker image
* Multi-architecture container builds
* AMD64 and ARM64 support
* Docker Buildx
* Jenkins CI pipeline
* Azure DevOps deployment pipeline
* Azure Container Registry integration
* Azure App Service deployment
* Terraform Infrastructure-as-Code
* Automated backend tests
* Actuator health endpoint
* Reproducible infrastructure configuration

---

## Technology Stack

| Area               | Technology               |
| ------------------ | ------------------------ |
| Backend            | Java 21                  |
| Framework          | Spring Boot              |
| Build System       | Gradle                   |
| Database           | PostgreSQL 17            |
| Containerization   | Docker                   |
| Multi-architecture | Docker Buildx            |
| CI                 | Jenkins                  |
| CD                 | Azure DevOps Pipelines   |
| Registry           | Azure Container Registry |
| Cloud Runtime      | Azure App Service        |
| Infrastructure     | Terraform                |
| Source Control     | Git / GitHub             |
| API Monitoring     | Spring Boot Actuator     |

---

## Repository Structure

```text
devflow/
├── architecture.drawio
├── azure/
│   ├── pipelines/
│   └── templates/
├── backend/
│   ├── build.gradle.kts
│   ├── Dockerfile
│   ├── gradlew
│   └── settings.gradle.kts
├── docs/
│   ├── api-design.md
│   ├── architecture.md
│   ├── deployment.md
│   └── requirements.md
├── infrastructure/
│   ├── bicep/
│   └── terraform/
├── jenkins/
│   ├── agents/
│   ├── pipelines/
│   └── Jenkinsfile
├── scripts/
│   ├── build.sh
│   ├── clean.sh
│   ├── run.sh
│   └── stop.sh
├── docker-compose.yml
├── Jenkinsfile
├── LICENSE
└── README.md
```

---

## Backend

The backend is a Spring Boot application using Java 21 and Gradle.

The application provides REST endpoints for managing build tasks and persists task data in PostgreSQL.

The backend also exposes Spring Boot Actuator endpoints for application health monitoring.

### Run locally

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Then start the backend:

```bash
cd backend
./gradlew bootRun
```

The application runs on:

```text
http://localhost:8081
```

Health check:

```bash
curl http://localhost:8081/actuator/health
```

---

## Testing

Run the complete backend test suite with:

```bash
cd backend
./gradlew clean test
```

The project includes automated tests for the backend and database-backed test configuration.

A successful test execution should finish with:

```text
BUILD SUCCESSFUL
```

---

## Docker

The backend uses a multi-stage Docker build.

The build stage contains the tools required to compile the Spring Boot application, while the runtime stage contains only what is required to execute the resulting application.

Build the image locally:

```bash
docker build -t devflow-backend ./backend
```

Run the container:

```bash
docker run --rm -p 8081:8081 devflow-backend
```

---

## Multi-Architecture Builds

DevFlow uses Docker Buildx to produce container images for:

```text
linux/amd64
linux/arm64
```

Example:

```bash
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t devflow-backend:multiarch \
  ./backend
```

When publishing an image to a registry, use `--push`:

```bash
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t <registry>/devflow-backend:<tag> \
  --push \
  ./backend
```

The resulting image tag can contain manifests for both architectures, allowing the appropriate image variant to be selected automatically by the target platform.

---

## Jenkins CI

Jenkins is used to automate the continuous-integration workflow.

The pipeline performs the main build workflow:

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
Docker Buildx
   │
   ▼
Multi-Architecture Image
   │
   ▼
Azure Container Registry
```

The repository contains Jenkins pipeline configuration under:

```text
Jenkinsfile
jenkins/
```

The project also supports Jenkins agents for distributed build workloads.

---

## Azure Container Registry

Built container images are published to Azure Container Registry.

The registry provides a private location for storing the application's container images before deployment.

The CI/CD workflow can therefore be represented as:

```text
Jenkins
   │
   │ docker push
   ▼
Azure Container Registry
   │
   │ image
   ▼
Azure App Service
```

---

## Azure Deployment

Azure DevOps Pipelines are used as part of the deployment workflow.

The deployment process takes the container image stored in ACR and updates the Azure App Service deployment.

The intended workflow is:

```text
Source Code
    │
    ▼
Jenkins CI
    │
    ▼
Multi-Arch Image
    │
    ▼
Azure Container Registry
    │
    ▼
Azure DevOps Pipeline
    │
    ▼
Azure App Service
```

The application can then be verified using its Spring Boot health endpoint:

```text
/actuator/health
```

---

## Infrastructure as Code

Azure infrastructure is defined using Terraform.

Terraform configuration is located in:

```text
infrastructure/terraform/
```

The Terraform configuration defines the required Azure infrastructure and outputs useful resource information after deployment.

Typical Terraform workflow:

```bash
cd infrastructure/terraform

terraform init
terraform validate
terraform plan
terraform apply
```

To inspect the resulting infrastructure:

```bash
terraform output
```

Terraform state files and variable files are intentionally excluded from Git.

---

## API

The backend exposes REST endpoints for build-task management.

The API design is documented in:

```text
docs/api-design.md
```

The core concept is a `BuildTask`, representing a build/deployment workflow and its current state.

Typical task states include:

```text
PENDING
RUNNING
SUCCESS
FAILED
```

Example API request:

```bash
curl http://localhost:8081/api/build-tasks
```

The API can be used to create and monitor build tasks independently from the CI/CD infrastructure.

---

## Documentation

Additional technical documentation is available under:

```text
docs/
```

### Architecture

```text
docs/architecture.md
```

Describes the main DevFlow components and their relationships.

### API Design

```text
docs/api-design.md
```

Describes the REST API and build-task model.

### Deployment

```text
docs/deployment.md
```

Documents the deployment workflow and environment configuration.

### Requirements

```text
docs/requirements.md
```

Contains the project's functional and technical requirements.

---

## Scripts

Utility scripts are available under:

```text
scripts/
```

| Script     | Purpose                   |
| ---------- | ------------------------- |
| `build.sh` | Build the project         |
| `run.sh`   | Run the application       |
| `stop.sh`  | Stop running services     |
| `clean.sh` | Clean generated resources |

---

## Branch Strategy

The project uses a development branch workflow.

The primary development branch is:

```text
develop
```

Feature work can be developed independently and merged into the development branch after verification.

The repository uses Git for source control and GitHub as the remote repository.

---

## Security

Sensitive Terraform files are excluded from version control.

The repository ignores:

```text
*.tfstate
*.tfstate.*
*.tfvars
.env
.env.*
```

Credentials and environment-specific configuration should be supplied through secure environment variables, CI/CD credentials, or cloud identity mechanisms rather than committed to the repository.

---

## Project Status

### Completed

* [x] Spring Boot backend
* [x] PostgreSQL integration
* [x] REST API
* [x] Automated backend tests
* [x] Docker containerization
* [x] Multi-stage Docker build
* [x] Jenkins CI pipeline
* [x] Multi-architecture Docker Buildx workflow
* [x] Azure Container Registry integration
* [x] Azure App Service deployment
* [x] Azure DevOps pipeline
* [x] Terraform Infrastructure-as-Code
* [x] Repository security cleanup

### Final Phase

The project is currently in the final documentation and polish phase.

---

## Project Objective

DevFlow demonstrates how a modern DevOps workflow can combine application development, containerization, distributed CI, multi-architecture builds, cloud infrastructure, and automated deployment into a single reproducible platform.

The project is designed to reduce manual DevOps operations while providing a consistent path from source code to a deployed application.

---

## License

This project is licensed under the terms specified in [LICENSE](LICENSE).
