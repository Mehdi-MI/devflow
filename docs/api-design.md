# DevFlow API Design

## 1. Overview

DevFlow exposes a REST API for managing build tasks.

The API is implemented using Spring Boot and provides CRUD operations for build tasks.

The main API resource is:

```text
/api/build-tasks
```

The API uses JSON for request and response bodies.

---

## 2. API Resource

The primary resource is a `BuildTask`.

A build task represents a build operation associated with a source-code repository and branch.

The resource contains:

* `id`
* `name`
* `repositoryUrl`
* `branch`
* `status`
* `createdAt`
* `startedAt`
* `completedAt`
* `logs`

---

## 3. Base URL

For local development, the backend runs on port `8081`.

The API base path is:

```text
http://localhost:8081/api/build-tasks
```

---

## 4. Endpoints

The API provides the following endpoints:

| Method | Endpoint                | Description                   | Success          |
| ------ | ----------------------- | ----------------------------- | ---------------- |
| POST   | `/api/build-tasks`      | Create a build task           | `201 Created`    |
| GET    | `/api/build-tasks`      | Retrieve all build tasks      | `200 OK`         |
| GET    | `/api/build-tasks/{id}` | Retrieve a build task by UUID | `200 OK`         |
| PUT    | `/api/build-tasks/{id}` | Update a build task           | `200 OK`         |
| DELETE | `/api/build-tasks/{id}` | Delete a build task           | `204 No Content` |

---

## 5. Create Build Task

### Request

```http
POST /api/build-tasks
Content-Type: application/json
```

The request body is represented by `CreateBuildTaskRequest`.

```json
{
  "name": "Build DevFlow",
  "repositoryUrl": "https://github.com/example/devflow.git",
  "branch": "develop"
}
```

### Request Fields

| Field           | Type   | Required | Description            |
| --------------- | ------ | -------- | ---------------------- |
| `name`          | String | Yes      | Name of the build task |
| `repositoryUrl` | String | Yes      | Repository URL         |
| `branch`        | String | Yes      | Git branch             |

The fields are validated using `@NotBlank`.

Validation messages are:

* `Task name is required`
* `Repository URL is required`
* `Branch is required`

### Successful Response

The endpoint returns:

```text
201 Created
```

Example:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Build DevFlow",
  "repositoryUrl": "https://github.com/example/devflow.git",
  "branch": "develop",
  "status": "PENDING",
  "createdAt": "2026-08-09T10:00:00",
  "startedAt": null,
  "completedAt": null,
  "logs": null
}
```

A newly created task defaults to the `PENDING` status when no status is explicitly assigned.

---

## 6. Get All Build Tasks

### Request

```http
GET /api/build-tasks
```

### Successful Response

The endpoint returns:

```text
200 OK
```

The response is a JSON array.

Example:

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Build DevFlow",
    "repositoryUrl": "https://github.com/example/devflow.git",
    "branch": "develop",
    "status": "PENDING",
    "createdAt": "2026-08-09T10:00:00",
    "startedAt": null,
    "completedAt": null,
    "logs": null
  }
]
```

If no build tasks exist, the API returns an empty array:

```json
[]
```

---

## 7. Get Build Task by ID

### Request

```http
GET /api/build-tasks/{id}
```

The `id` is a UUID.

Example:

```http
GET /api/build-tasks/550e8400-e29b-41d4-a716-446655440000
```

### Successful Response

The endpoint returns:

```text
200 OK
```

Example:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Build DevFlow",
  "repositoryUrl": "https://github.com/example/devflow.git",
  "branch": "develop",
  "status": "PENDING",
  "createdAt": "2026-08-09T10:00:00",
  "startedAt": null,
  "completedAt": null,
  "logs": null
}
```

### Not Found Response

If the build task does not exist, the API returns:

```text
404 Not Found
```

Example:

```json
{
  "timestamp": "2026-08-09T10:05:00",
  "status": 404,
  "error": "Not Found",
  "message": "Build task not found: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

## 8. Update Build Task

### Request

```http
PUT /api/build-tasks/{id}
Content-Type: application/json
```

The request body is represented by `UpdateBuildTaskRequest`.

All fields in the update request are optional.

Example:

```json
{
  "name": "Updated DevFlow Build",
  "repositoryUrl": "https://github.com/example/devflow.git",
  "branch": "develop",
  "status": "RUNNING",
  "logs": "Build started successfully."
}
```

### Request Fields

| Field           | Type        | Required | Description            |
| --------------- | ----------- | -------- | ---------------------- |
| `name`          | String      | No       | Updated task name      |
| `repositoryUrl` | String      | No       | Updated repository URL |
| `branch`        | String      | No       | Updated branch         |
| `status`        | BuildStatus | No       | Updated build status   |
| `logs`          | String      | No       | Build logs             |

### Successful Response

The endpoint returns:

```text
200 OK
```

Example:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Updated DevFlow Build",
  "repositoryUrl": "https://github.com/example/devflow.git",
  "branch": "develop",
  "status": "RUNNING",
  "createdAt": "2026-08-09T10:00:00",
  "startedAt": null,
  "completedAt": null,
  "logs": "Build started successfully."
}
```

---

## 9. Delete Build Task

### Request

```http
DELETE /api/build-tasks/{id}
```

Example:

```http
DELETE /api/build-tasks/550e8400-e29b-41d4-a716-446655440000
```

### Successful Response

The endpoint returns:

```text
204 No Content
```

The response contains no body.

### Not Found Response

If the build task does not exist, the API returns:

```text
404 Not Found
```

with the standard error response structure.

---

## 10. Build Task Response

The API uses `BuildTaskResponse` for successful responses.

| Field           | Type          | Description                |
| --------------- | ------------- | -------------------------- |
| `id`            | UUID          | Unique identifier          |
| `name`          | String        | Build task name            |
| `repositoryUrl` | String        | Source repository URL      |
| `branch`        | String        | Git branch                 |
| `status`        | BuildStatus   | Current build status       |
| `createdAt`     | LocalDateTime | Task creation timestamp    |
| `startedAt`     | LocalDateTime | Build start timestamp      |
| `completedAt`   | LocalDateTime | Build completion timestamp |
| `logs`          | String        | Build execution logs       |

---

## 11. Build Status

The build task status is represented by the `BuildStatus` enum.

The implementation uses at least the following statuses:

```text
PENDING
RUNNING
```

The API serializes the status as a string in JSON responses.

For example:

```json
{
  "status": "RUNNING"
}
```

Additional status values should be documented here when they are added to the `BuildStatus` enum.

---

## 12. Error Handling

DevFlow uses a centralized `GlobalExceptionHandler`.

The API returns a consistent `ErrorResponse` structure for handled errors.

The structure is:

```json
{
  "timestamp": "2026-08-09T10:05:00",
  "status": 404,
  "error": "Not Found",
  "message": "Build task not found"
}
```

### Error Response Fields

| Field       | Type          | Description                  |
| ----------- | ------------- | ---------------------------- |
| `timestamp` | LocalDateTime | Time when the error occurred |
| `status`    | Integer       | HTTP status code             |
| `error`     | String        | HTTP status description      |
| `message`   | String        | Detailed error message       |

---

## 13. HTTP Error Responses

### 400 Bad Request

Validation errors return:

```text
400 Bad Request
```

For example, creating a task without a name results in:

```json
{
  "timestamp": "2026-08-09T10:05:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Task name is required"
}
```

The controller tests verify validation behavior for missing required fields.

### 404 Not Found

When a requested build task does not exist:

```text
404 Not Found
```

Example:

```json
{
  "timestamp": "2026-08-09T10:05:00",
  "status": 404,
  "error": "Not Found",
  "message": "Build task not found: <id>"
}
```

### 500 Internal Server Error

Unexpected exceptions are handled by the generic exception handler and return:

```text
500 Internal Server Error
```

Example:

```json
{
  "timestamp": "2026-08-09T10:05:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

The exact `message` value is generated from the underlying exception.

---

## 14. Validation

The create endpoint uses Jakarta Bean Validation.

The following fields are mandatory:

```text
name
repositoryUrl
branch
```

The controller uses:

```java
@Valid @RequestBody CreateBuildTaskRequest request
```

This causes invalid requests to be handled by `GlobalExceptionHandler`.

The update request does not define validation annotations, allowing partial updates.

---

## 15. Data Model

The persistence entity is `BuildTask`.

The database table is:

```text
build_tasks
```

The entity contains:

```text
BuildTask
├── id
├── name
├── repositoryUrl
├── branch
├── status
├── createdAt
├── startedAt
├── completedAt
└── logs
```

The ID is generated as a UUID.

The following database columns are non-null:

* `name`
* `repository_url`
* `branch`
* `status`
* `created_at`

The `logs` field is stored as PostgreSQL text data.

---

## 16. Timestamp Handling

When a build task is created, the entity's `@PrePersist` callback initializes:

```text
createdAt
```

If no status has been provided, the entity initializes the status to:

```text
PENDING
```

The `startedAt` and `completedAt` fields are optional and represent build lifecycle timestamps.

---

## 17. API Testing

The REST controller is tested using Spring MVC test support.

The controller test class is:

```text
BuildTaskControllerTest
```

The tests cover:

* Creating a build task
* Retrieving all build tasks
* Retrieving an empty task list
* Retrieving a build task by ID
* Handling a missing build task
* Updating a build task
* Deleting a build task
* Handling deletion of a missing task
* Validating required create fields

The tests verify HTTP status codes, JSON response fields, service interactions, and error responses.

---

## 18. Example API Workflow

A typical build-task workflow is:

```text
Client
   │
   │ POST /api/build-tasks
   ▼
BuildTaskController
   │
   ▼
BuildTaskService
   │
   ▼
BuildTaskRepository
   │
   ▼
PostgreSQL
```

After creation, the client can retrieve or update the task:

```text
POST   /api/build-tasks
   │
   ▼
GET    /api/build-tasks/{id}
   │
   ▼
PUT    /api/build-tasks/{id}
   │
   ▼
DELETE /api/build-tasks/{id}
```

The API therefore provides the CRUD foundation required to manage build tasks within DevFlow.

---

## 19. API Design Principles

The API follows several design principles:

### RESTful Resource Structure

Build tasks are represented as a resource under:

```text
/api/build-tasks
```

### HTTP Methods

Standard HTTP methods are used according to the operation:

* `POST` for creation
* `GET` for retrieval
* `PUT` for updates
* `DELETE` for removal

### HTTP Status Codes

The API uses meaningful HTTP status codes:

* `200 OK`
* `201 Created`
* `204 No Content`
* `400 Bad Request`
* `404 Not Found`
* `500 Internal Server Error`

### DTO Separation

Request and response DTOs are separated from the persistence entity.

This prevents the API contract from being directly coupled to the database entity.

### Centralized Error Handling

Application errors are converted into a consistent `ErrorResponse` format through `GlobalExceptionHandler`.

### Validation

Required fields are validated at the API boundary before the service layer is called.
