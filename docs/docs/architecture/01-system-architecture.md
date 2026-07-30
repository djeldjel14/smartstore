# SmartStore System Architecture

## Overview

This document defines the overall architecture of the SmartStore system.

Its purpose is to establish the technical foundation before implementation begins. All development decisions should follow the architecture described in this document.

**Note:** This is a revised architecture designed specifically for a single developer building a learning-focused MVP for one physical grocery store. The architecture prioritizes simplicity, maintainability, and practical software engineering experience over premature complexity.

---

# Revision Notes

| Area | Original Decision | Revised Decision | Reason |
|------|-------------------|------------------|--------|
| Architecture Style | Modular Monolith | **Layered Architecture** | Simpler and easier to understand for a single developer and a small MVP. |
| Security | Spring Security from day one | **Authentication postponed until core features are complete** | Focus on business logic before infrastructure concerns. |
| Schema Migration | Flyway | **Hibernate ddl-auto=update** | The database schema will evolve frequently during development. Flyway will be introduced after stabilization. |
| MVP Modules | Dashboard, Settings | **Deferred** | Not essential for the first usable version. |
| Database | PostgreSQL | **PostgreSQL (Local Instance)** | Better reflects real-world enterprise development while remaining fully offline. |

---

# Project Information

| Property | Value |
|----------|-------|
| Project Name | SmartStore |
| Project Type | Store Management System (POS + Inventory) |
| Platform | Web Application |
| Deployment | Local Network (LAN) |
| Architecture | Layered Architecture |
| Development Approach | Backend-First, Feature-by-Feature |
| Database | PostgreSQL |

---

# Project Goal

SmartStore is a web-based management system designed for a single grocery store.

The MVP aims to provide a reliable and easy-to-use system for managing products, inventory, and sales while simultaneously serving as a practical software engineering learning project.

Future versions may support multiple cashier terminals, additional business modules, and more advanced business requirements without requiring a complete redesign.

---

# Target Users

Current MVP users:

- Store Owner
- Cashier

Future versions may support multiple users with different permission levels.

---

# Architecture Style

## Selected Architecture

**Layered Architecture**

```
Presentation
      ↓
Controller
      ↓
Service
      ↓
Repository
      ↓
Database
```

### Why Layered Architecture?

SmartStore MVP is intentionally small.

Current project characteristics:

- One developer
- Learning-oriented
- Four to five core business modules
- One local PostgreSQL database

A Layered Architecture keeps responsibilities separated while remaining easy to understand, debug, and maintain.

If the project grows significantly in the future, it can evolve into a Modular Monolith without redesigning the business logic.

---

# High-Level Architecture

```text
+----------------------+
|      Web Browser     |
+----------+-----------+
           |
           v
+----------------------+
| Thymeleaf + Bootstrap|
+----------+-----------+
           |
           v
+----------------------+
| Spring MVC Controller|
+----------+-----------+
           |
           v
+----------------------+
|    Service Layer     |
+----------+-----------+
           |
           v
+----------------------+
| Repository Layer     |
+----------+-----------+
           |
           v
+----------------------+
| PostgreSQL Database  |
+----------------------+
```

Everything runs locally on the store computer without requiring internet access.

---

# Technology Stack

## Frontend

- HTML5
- CSS3
- Bootstrap 5
- Thymeleaf
- JavaScript (minimal)

## Backend

- Java 21
- Spring Boot
- Spring MVC
- Spring Data JPA
- Hibernate

## Database

- PostgreSQL (Local Installation)

## Build Tool

- Maven

## Version Control

- Git
- GitHub

---

# Development Philosophy

SmartStore follows an incremental **Feature-by-Feature** development process.

Every feature is fully completed before starting the next one.

Development workflow:

1. Requirements Analysis
2. Database Design
3. Entity
4. Repository
5. Service
6. Controller
7. User Interface
8. Testing
9. Git Commit

The project should remain functional after completing each feature.

---

# Layered Architecture

## Presentation Layer

Responsibilities:

- User Interface
- Form Validation
- Response Rendering

Technology:

- Thymeleaf
- Bootstrap
- JavaScript

---

## Controller Layer

Responsibilities:

- Receive HTTP requests
- Validate input
- Call business services
- Return responses

Technology:

- Spring MVC

---

## Service Layer

Responsibilities:

- Business logic
- Business rules
- Validation
- Transactions

---

## Repository Layer

Responsibilities:

- CRUD operations
- Database queries
- Data persistence

Technology:

- Spring Data JPA

---

## Database Layer

Responsibilities:

- Data storage
- Constraints
- Relationships
- Indexes

Technology:

- PostgreSQL

---

# Project Structure

```text
com.smartstore
│
├── config
├── controller
├── entity
├── repository
├── service
└── SmartStoreApplication
```

Additional packages such as:

- dto
- mapper
- exception
- validation
- util
- security

will only be introduced when the project actually requires them.

This follows the **YAGNI** principle.

---

# Functional Modules

## MVP (Phase 1)

- Categories
- Products
- Inventory
- Sales / POS
- Basic Reports
- Authentication *(implemented as the final step of Phase 1)*

---

## Phase 2

- Suppliers
- Purchases
- Expenses
- Backup & Restore

---

## Future Versions

- Customers
- Employees
- Dashboard
- Settings
- Notifications
- Multiple Cashiers
- Multiple Stores
- Multiple Warehouses
- REST API
- Mobile Application
- Barcode Scanner
- Receipt Printer

---

# Security

## Phase 1

During the initial development phase, authentication is intentionally postponed to simplify development and reduce debugging complexity while learning the framework.

Before the MVP is considered complete, a basic authentication system will be implemented using:

- Username
- Password
- BCrypt Password Hashing
- Session-Based Authentication

---

## Future Security Improvements

When SmartStore supports multiple users:

- Spring Security
- Role-Based Access Control (RBAC)
- CSRF Protection
- Secure Session Management

---

# Database Strategy

Database Engine:

- PostgreSQL

ORM:

- Hibernate

Persistence:

- Spring Data JPA

Current Schema Management:

```
spring.jpa.hibernate.ddl-auto=update
```

Future Schema Management:

- Flyway

Flyway will be introduced only after the database schema becomes stable.

---

# Design Principles

The project follows the following software engineering principles:

- SOLID
- DRY (Don't Repeat Yourself)
- KISS (Keep It Simple)
- YAGNI (You Aren't Gonna Need It)
- Separation of Concerns
- High Cohesion
- Low Coupling

---

# Scalability

The architecture allows future expansion without major redesign.

Possible future improvements include:

- Second cashier terminal on the same LAN
- REST API
- Mobile application
- Barcode scanner support
- Receipt printer support

More advanced requirements such as multiple stores and multiple warehouses may require additional architectural changes.

---

# Architectural Decisions

| Decision | Selected |
|----------|----------|
| Application Type | Web Application |
| Deployment | Local Network |
| Architecture | Layered Architecture |
| Backend | Spring Boot |
| Frontend | Thymeleaf + Bootstrap |
| Database | PostgreSQL |
| ORM | Hibernate |
| Schema Management | Hibernate `ddl-auto=update` (Flyway later) |
| Build Tool | Maven |
| Security | Authentication implemented at the end of Phase 1 |
| Version Control | Git + GitHub |

---

# Status

**Status:** Approved

This document defines the official architecture of SmartStore MVP and serves as the primary technical reference for implementation.

The architecture intentionally favors simplicity, maintainability, and progressive learning while remaining flexible enough for future expansion.