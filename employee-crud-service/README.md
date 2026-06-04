# Employee CRUD Microservice

A RESTful microservice for Employee CRUD operations built with Spring Boot 3.4.1 and Java 21.

## Tech Stack

- **Java**: 21 (LTS)
- **Spring Boot**: 3.4.1
- **Database**: H2 (in-memory)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, MockMvc
- **Coverage**: JaCoCo (>90%)

## Employee Object

| Field            | Type    | Description          | Constraints                |
|------------------|---------|----------------------|----------------------------|
| `employeeId`     | Long    | Auto-generated ID    | Primary Key                |
| `employeeName`   | String  | Employee's full name | Required, non-blank        |
| `employeeSalary` | Double  | Employee's salary    | Required, must be positive |
| `employeeAge`    | Integer | Employee's age       | Required, minimum 18       |

## API Endpoints

| Method | Endpoint                   | Description              |
|--------|----------------------------|--------------------------|
| POST   | `/api/employees`           | Create a new employee    |
| GET    | `/api/employees/{id}`      | Get employee by ID       |
| GET    | `/api/employees`           | Get all employees        |
| PUT    | `/api/employees/{id}`      | Update employee by ID    |
| DELETE | `/api/employees/{id}`      | Delete employee by ID    |

## Build & Run

```bash
cd employee-crud-service
mvn clean install
mvn spring-boot:run
```

The service starts on `http://localhost:8080`.

## Run Tests

```bash
mvn test
```

## View Coverage Report

```bash
mvn test
# Report available at: target/site/jacoco/index.html
```

## Sample Payloads for Testing

### 1. Create Employee (POST /api/employees)

**Request:**
```json
{
    "employeeName": "John Doe",
    "employeeSalary": 75000.00,
    "employeeAge": 30
}
```

**Response (201 Created):**
```json
{
    "employeeId": 1,
    "employeeName": "John Doe",
    "employeeSalary": 75000.00,
    "employeeAge": 30
}
```

**cURL:**
```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"employeeName": "John Doe", "employeeSalary": 75000.00, "employeeAge": 30}'
```

### 2. Get Employee by ID (GET /api/employees/1)

**Response (200 OK):**
```json
{
    "employeeId": 1,
    "employeeName": "John Doe",
    "employeeSalary": 75000.00,
    "employeeAge": 30
}
```

**cURL:**
```bash
curl http://localhost:8080/api/employees/1
```

### 3. Get All Employees (GET /api/employees)

**Response (200 OK):**
```json
[
    {
        "employeeId": 1,
        "employeeName": "John Doe",
        "employeeSalary": 75000.00,
        "employeeAge": 30
    },
    {
        "employeeId": 2,
        "employeeName": "Jane Smith",
        "employeeSalary": 85000.00,
        "employeeAge": 28
    }
]
```

**cURL:**
```bash
curl http://localhost:8080/api/employees
```

### 4. Update Employee (PUT /api/employees/1)

**Request:**
```json
{
    "employeeName": "John Doe Updated",
    "employeeSalary": 85000.00,
    "employeeAge": 31
}
```

**Response (200 OK):**
```json
{
    "employeeId": 1,
    "employeeName": "John Doe Updated",
    "employeeSalary": 85000.00,
    "employeeAge": 31
}
```

**cURL:**
```bash
curl -X PUT http://localhost:8080/api/employees/1 \
  -H "Content-Type: application/json" \
  -d '{"employeeName": "John Doe Updated", "employeeSalary": 85000.00, "employeeAge": 31}'
```

### 5. Delete Employee (DELETE /api/employees/1)

**Response: 204 No Content**

**cURL:**
```bash
curl -X DELETE http://localhost:8080/api/employees/1
```

### Error Responses

**Employee Not Found (404):**
```json
{
    "timestamp": "2024-01-15T10:30:00",
    "status": 404,
    "error": "Not Found",
    "message": "Employee not found with id: 99"
}
```

**Validation Error (400):**
```json
{
    "timestamp": "2024-01-15T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "errors": {
        "employeeName": "Employee name is required",
        "employeeSalary": "Salary must be positive",
        "employeeAge": "Age must be at least 18"
    }
}
```
