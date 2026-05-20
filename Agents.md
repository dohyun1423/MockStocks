# MockStock Coding Rules

## General Rules

- Use Spring Boot conventions
- Keep layered architecture
- Use DTOs for request/response
- Avoid returning Entity directly
- Use Service layer for business logic
- Use Global Exception Handler

---

# Security Rules

- Use JWT authentication
- Do not use session authentication
- Keep Stateless architecture
- Use BCryptPasswordEncoder

---

# API Rules

- Use RESTful API style
- JSON request/response
- Consistent error response format

Example:

{
"message": "에러 메시지"
}

---

# Package Structure

global
- config
- exception
- response
- security

domain
- user
- stock
- order
- portfolio

web
- controller

---

# Frontend Rules

- Use Thymeleaf
- CSS in static/css
- JavaScript inline or separate js file
- Use fetch API for backend communication

---

# Coding Style

- Clear naming
- Avoid overly large controllers
- Prefer constructor injection
- Use final when possible
- Keep methods focused

---

# Current Authentication Flow

Login:
- email/password validation
- JWT generation
- localStorage token storage

Request:
- Authorization: Bearer token
- JwtAuthenticationFilter validation

---

# Current Stack

- Java 21
- Spring Boot
- Spring Security
- JWT
- JPA
- MySQL
- Thymeleaf
- Gradle

---

# Current Development Focus

Currently focusing on:
1. Authentication
2. UI connection
3. JWT flow
4. Stock domain design
5. Portfolio system

---

# Important

This project is intended to simulate
a real trading platform architecture,
not just a simple CRUD application.