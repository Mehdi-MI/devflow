# DevFlow Deployment Guide

## 1. Overview

DevFlow is deployed using a combination of Docker, Jenkins, Azure Container Registry, Azure App Service, PostgreSQL Flexible Server, and Terraform.

The deployment workflow is designed to separate:

* Application build
* Automated testing
* Multi-architecture container image creation
* Container image publishing
* Cloud infrastructure provisioning
* Application deployment
* Health verification

No credentials or secrets are stored directly in the deployment documentation.

---

## 2. Deployment Architecture

The deployment workflow follows this sequence:

```text
Developer
    │
    ▼
GitHub Repository
    │
    ▼
Jenkins
    │
    ├── Checkout
    ├── Gradle Build
    ├── Tests
    └── Docker Buildx
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

Terraform provisions the Azure infrastructure required by the cloud deployment.

---

## 3. Prerequisites

The deployment environment requires the following tools:

* Git
* Java 21
* Docker
* Docker Compose
* Docker Buildx
* Gradle Wrapper
* Jenkins
* Terraform
* Azure CLI

The project uses the Gradle Wrapper included in the repository, so a separate Gradle installation is not required for application builds.

---

## 4. Local Deployment

### 4.1 Start the Local Environment

The local environment is defined in:

```text
docker-compose.yml
```

It contains two services:

* PostgreSQL 17
* DevFlow backend

The PostgreSQL service uses a persistent Docker volume:

```text
postgres_data
```

The backend is exposed on port:

```text
8081
```

PostgreSQL is exposed on:

```text
5432
```

The backend depends on the PostgreSQL health check before starting.

Start the environment with:

```bash
docker compose up -d
```

Check the running containers:

```bash
docker compose ps
```

View backend logs:

```bash
docker compose logs -f backend
```

View PostgreSQL logs:

```bash
docker compose logs -f postgres
```

---

## 5. Local Configuration

The Docker Compose configuration uses environment variables for database credentials.

The supported variables are:

```text
POSTGRES_USER
POSTGRES_PASSWORD
```

The backend receives its database configuration through:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

The application therefore does not require database credentials to be hard-coded into the application image.

For local development, environment variables can be provided through the shell or an appropriate local environment configuration.

Sensitive configuration should not be committed to Git.

---

## 6. Building the Backend

The backend is a Java 21 Spring Boot application.

The Gradle project is located at:

```text
backend/
```

Build the application with:

```bash
cd backend
./gradlew clean build
```

Run the tests with:

```bash
./gradlew test
```

The generated Spring Boot application JAR is produced under:

```text
backend/build/libs/
```

---

## 7. Docker Image

The backend uses a multi-stage Dockerfile:

```text
backend/Dockerfile
```

### Build Stage

The build stage uses:

```text
eclipse-temurin:21-jdk
```

It:

1. Copies the Gradle configuration.
2. Downloads dependencies.
3. Copies the application source.
4. Builds the Spring Boot application.

### Runtime Stage

The runtime stage uses:

```text
eclipse-temurin:21-jre
```

Only the generated application JAR is copied into the runtime image.

This keeps build dependencies out of the runtime image.

The application container exposes:

```text
8081
```

Build the image locally with:

```bash
cd backend
docker build -t devflow-backend:latest .
```

---

## 8. Jenkins CI/CD Pipeline

Jenkins automates the application build, testing, and container publishing workflow.

The main pipeline is defined in:

```text
Jenkinsfile
```

The pipeline contains the following stages:

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

---

## 9. Checkout Stage

Jenkins checks out the source code from the configured Git repository:

```text
checkout scm
```

This ensures that the pipeline operates on the source revision associated with the Jenkins build.

---

## 10. Build Stage

The build stage enters the backend directory and executes:

```bash
./gradlew clean build -x test
```

Tests are intentionally executed in the dedicated Test stage.

This separates application compilation/package generation from the explicit test stage.

---

## 11. Test Stage

The Test stage executes:

```bash
./gradlew test
```

The project contains unit, controller, and integration-oriented tests.

The pipeline continues to the container build stage only when the test stage succeeds.

---

## 12. Multi-Architecture Image Build

Docker Buildx is used to build the backend image for multiple architectures.

The configured platforms are:

```text
linux/amd64
linux/arm64
```

The Jenkins pipeline uses the configured Buildx builder:

```text
multiarch
```

The resulting image is pushed directly to Azure Container Registry.

The pipeline creates two image tags:

```text
<registry>/devflow-backend:<BUILD_NUMBER>
<registry>/devflow-backend:latest
```

The build number provides a unique image version for each Jenkins execution, while `latest` represents the current published image.

---

## 13. Registry Authentication

Jenkins authenticates with Azure Container Registry using a Jenkins-managed credential.

The pipeline references the Jenkins credential by its configured credential identifier rather than storing credentials directly in the Jenkinsfile.

Authentication is performed using Docker's password-stdin mechanism:

```bash
echo "$ACR_PASSWORD" | docker login "$REGISTRY" \
    --username "$ACR_USERNAME" \
    --password-stdin
```

The actual username and password are intentionally not documented or stored in this file.

After the pipeline completes, Jenkins logs out of the registry:

```bash
docker logout ${REGISTRY}
```

---

## 14. Multi-Architecture Verification

After publishing the image, Jenkins verifies the image manifest using:

```bash
docker buildx imagetools inspect \
    ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
```

This confirms that the published image contains the expected multi-platform configuration.

The verification stage provides an additional check that the registry contains the intended architecture variants.

---

## 15. Azure Infrastructure

The Azure infrastructure is defined using Terraform.

The Terraform configuration is located at:

```text
infrastructure/terraform/
```

The main infrastructure components are:

```text
Azure Resource Group
        │
        ├── Azure Container Registry
        │
        ├── Linux App Service Plan
        │
        ├── Linux Web App
        │
        └── PostgreSQL Flexible Server
```

The default deployment region configured by the project is:

```text
spaincentral
```

---

## 16. Terraform Configuration

The Terraform configuration consists of:

```text
main.tf
variables.tf
outputs.tf
versions.tf
```

Terraform requires version:

```text
>= 1.9.0
```

The AzureRM provider uses the configured 4.x provider series.

Initialize Terraform with:

```bash
cd infrastructure/terraform
terraform init
```

Validate the configuration:

```bash
terraform validate
```

Review the planned infrastructure changes:

```bash
terraform plan
```

Apply the infrastructure:

```bash
terraform apply
```

Terraform prompts for confirmation before applying changes unless an appropriate automated workflow is used.

---

## 17. Sensitive Terraform Variables

Terraform requires sensitive configuration for Azure and PostgreSQL resources.

The project defines sensitive variables for values such as:

```text
subscription_id
tenant_id
db_password
```

These values must be supplied securely.

They should not be placed directly into documentation, source code, or committed configuration files.

Terraform variables containing secrets should be supplied through an appropriate secure mechanism, such as environment variables or a protected local variable file that is excluded from version control.

The repository `.gitignore` excludes:

```text
*.tfstate
*.tfstate.*
*.tfvars
*.tfvars.json
```

---

## 18. Terraform State

Terraform state contains infrastructure information and may contain sensitive data.

The project therefore excludes Terraform state files from Git:

```text
terraform.tfstate
terraform.tfstate.*
```

Terraform state should never be committed to the public repository.

For a production deployment, remote state storage with appropriate access control should be considered.

---

## 19. Azure Container Registry

Azure Container Registry stores the published DevFlow backend image.

The Jenkins pipeline publishes:

```text
devflow-backend:<BUILD_NUMBER>
devflow-backend:latest
```

The registry is configured as the image source for the Azure App Service deployment.

The registry authentication credentials are managed outside the application source code.

---

## 20. Azure App Service

The backend is deployed as a Linux container on Azure App Service.

The App Service is configured to listen on:

```text
8081
```

The deployment uses the container image published to Azure Container Registry.

The App Service uses a system-assigned managed identity.

Terraform enables managed-identity-based authentication for the container registry:

```text
container_registry_use_managed_identity = true
```

This allows the App Service to retrieve the container image without requiring registry credentials to be embedded in the application configuration.

---

## 21. PostgreSQL Flexible Server

The cloud deployment uses Azure Database for PostgreSQL Flexible Server.

The configured PostgreSQL major version is:

```text
17
```

The application connects to the cloud database using environment-based configuration.

The database password is provided through a sensitive Terraform variable and is not stored in this documentation.

---

## 22. Application Configuration in Azure

The Azure App Service receives database configuration through application settings.

The relevant configuration concepts are:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

The application container therefore uses the same configuration model across environments.

The actual password and other sensitive values must remain outside source-controlled documentation.

---

## 23. Application Health Check

Spring Boot Actuator provides the application health endpoint:

```text
/actuator/health
```

For a local deployment:

```bash
curl http://localhost:8081/actuator/health
```

After deployment, the same endpoint can be accessed through the application's configured Azure hostname.

A successful health response indicates that the Spring Boot application is running and that the health endpoint is available.

---

## 24. Deployment Verification

After deployment, verify the following components in order.

### 1. Azure Infrastructure

Confirm that Terraform successfully created or updated the required resources.

### 2. Container Registry

Confirm that the expected image tags were published.

### 3. Multi-Architecture Manifest

Verify the image using:

```bash
docker buildx imagetools inspect <registry>/devflow-backend:<tag>
```

The expected platforms are:

```text
linux/amd64
linux/arm64
```

### 4. App Service

Confirm that the App Service is running the expected container image.

### 5. Application Health

Check:

```text
/actuator/health
```

### 6. API

Verify the REST API endpoints after the application becomes available.

---

## 25. Deployment Rollback

Because Jenkins publishes images using the Jenkins build number, previous image versions can be identified using their build-specific tags.

For example:

```text
<registry>/devflow-backend:<BUILD_NUMBER>
```

A previous known-good image can therefore be selected for a rollback procedure.

The exact rollback mechanism depends on how the App Service deployment is being managed at the time of the rollback.

---

## 26. Stopping the Local Environment

To stop the local Docker Compose environment:

```bash
docker compose down
```

To stop the environment while preserving the PostgreSQL volume:

```bash
docker compose down
```

The named PostgreSQL volume remains available unless it is explicitly removed.

To remove the containers and associated volumes:

```bash
docker compose down -v
```

The `-v` option deletes the PostgreSQL Docker volume and therefore removes the local database data.

---

## 27. Security Considerations

The deployment process follows several security practices.

### Secrets Are Externalized

Passwords and credentials are provided through environment variables, Terraform sensitive variables, or Jenkins credentials.

### Credentials Are Not Hard-Coded

Registry credentials are not embedded directly in the Jenkins pipeline.

### Terraform Secrets Are Ignored

Terraform variable and state files are excluded from version control.

### Managed Identity

Azure App Service uses a system-assigned managed identity for container registry authentication.

### Password-Based Database Authentication

The PostgreSQL Flexible Server currently uses password authentication, with the administrator password supplied through a sensitive Terraform variable.

### HTTPS Database Connection

The Azure PostgreSQL connection is configured to require SSL/TLS through the database connection configuration.

### Minimal Runtime Image

The Dockerfile uses a separate JRE runtime stage so that build tooling is not included in the final application image.

---

## 28. Recommended Production Improvements

The current deployment provides a functional DevOps workflow. For a production environment, additional hardening could include:

* Remote Terraform state with access control
* Azure Key Vault for secret management
* Managed identity for additional Azure services
* Private networking for database and registry access
* Restricted App Service network access
* More restrictive IP rules
* Container image vulnerability scanning
* Dependency vulnerability scanning
* Automated deployment approvals
* Monitoring and alerting
* Centralized application logging
* Separate development, staging, and production environments

These improvements are recommendations and are not represented as currently implemented features unless explicitly configured elsewhere in the project.

---

## 29. End-to-End Deployment Workflow

The complete deployment process can be summarized as:

```text
1. Developer pushes code
          │
          ▼
2. Jenkins checks out repository
          │
          ▼
3. Gradle build
          │
          ▼
4. Automated tests
          │
          ▼
5. Docker Buildx
          │
          ├── linux/amd64
          └── linux/arm64
          │
          ▼
6. Push image to Azure Container Registry
          │
          ▼
7. Verify multi-architecture manifest
          │
          ▼
8. Azure App Service pulls container image
          │
          ▼
9. Spring Boot application starts
          │
          ▼
10. Application connects to PostgreSQL
          │
          ▼
11. Health endpoint verification
```

Terraform manages the Azure resources required by the cloud deployment.

---

## 30. Deployment Summary

DevFlow uses a reproducible containerized deployment workflow based on:

* Docker for application packaging
* Docker Compose for local development
* Jenkins for CI automation
* Gradle for application builds and tests
* Docker Buildx for multi-architecture images
* Azure Container Registry for image storage
* Azure App Service for application hosting
* Azure Database for PostgreSQL Flexible Server for cloud persistence
* Terraform for infrastructure provisioning
* Managed identity for App Service registry authentication
* Environment-based configuration for application secrets
