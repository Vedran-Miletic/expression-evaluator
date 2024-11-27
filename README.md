
# Expression Evaluator

A Spring Boot application for storing, validating, and evaluating logical expressions. Designed with a clean code approach and service-oriented architecture to ensure scalability and maintainability.

---

## Features

- **Validation**: Ensures expressions have valid syntax, balanced parentheses, and correct logical operators.
- **Storage**: Expressions are stored in an **H2 in-memory database** for fast access during runtime.
- **Dynamic Evaluation**: Supports evaluation of expressions with runtime-provided variables using the JEXL (Java Expression Language) engine.
- **API**: RESTful endpoints for managing expressions (CRUD) and evaluating them.

---

## Prerequisites

- **Java 19** or later
- **Maven** for dependency management
- Any REST client (e.g., Postman, curl) for testing the API

---

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/leapwise/expression-evaluator.git
cd expression-evaluator
```

### 2. Build the Project

Use Maven to build and package the application:
```bash
mvn clean install
```

### 3. Run the Application

Start the application using the following command:
```bash
mvn spring-boot:run
```

### 4. Access the Application

- **API Documentation**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## API Endpoints

### Save an Expression
**POST** `/expression`  
Save a logical expression.

**Request Body**:
```json
{
  "name": "ExampleExpression",
  "expression": "(x > 5 && y < 10) || z == 20"
}
```

**Response**:
- `200 OK`: Returns the UUID of the saved expression.

### Get All Expressions
**GET** `/all-expressions`  
Retrieve all stored expressions.

**Response**:
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "ExampleExpression",
    "expression": "(x > 5 && y < 10) || z == 20"
  }
]
```

### Evaluate an Expression
**POST** `/evaluate`  
Evaluate a saved expression with runtime variables.

**Request Body**:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "variables": {
    "x": 6,
    "y": 5,
    "z": 20
  }
}
```

**Response**:
- `200 OK`: Returns the result of the evaluation (`true` or `false`).

---

## Technologies Used

- **Spring Boot 3.4.0**: Application framework
- **H2 Database**: In-memory database for testing
- **JEXL 3.4.0**: Expression evaluation
- **JUnit 5**: Unit testing
- **Mockito**: Mocking for tests
- **Springdoc OpenAPI**: API documentation

---

## Running Tests

Run all tests with:
```bash
mvn test
```

---

## Future Improvements

- Add support for custom error messages in API responses.
- Enhance validation to support nested logical expressions.
- Expand to support other databases (e.g., PostgreSQL, MySQL) for production.

---

## Author

This project was created as part of a technical assessment. If you have any questions or feedback, feel free to reach out.
