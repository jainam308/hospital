# Hospital OPD Management Mini-Module

A lightweight, robust Outpatient Department (OPD) mini-module covering:
1. **Patient Registration & Search**: Register demographics (Name, Gender, Age, Phone) and search by name or phone.
2. **Appointment Scheduling**: Book appointments with assigned doctors, filter today's appointment queue.
3. **Doctor Consultation & Vitals**: Record vitals (BP, Heart Rate, Temperature), add clinical notes, mark consultations completed, and review patient medical history.

---

## Tech Stack
- **Backend**: Spring Boot 3.4, Java 21/23, Spring Data JPA, Hibernate, Jakarta Validation
- **Database**: MySQL 8.0+ (with fallback embedded database support)
- **Frontend**: Angular 19, TypeScript, Reactive Forms, Responsive CSS
- **Version Control**: Git / GitHub

---

## Project Structure
```
hospital/
├── backend/                  # Spring Boot application
│   ├── src/main/java/com/hospital/opd/
│   │   ├── controller/       # REST API endpoints
│   │   ├── dto/              # Request / Response DTOs
│   │   ├── entity/           # JPA Entities (Patient, Appointment, Consultation)
│   │   ├── exception/        # Global Exception Handler
│   │   ├── repository/       # Spring Data Repositories
│   │   └── service/          # Business logic interfaces & implementations
│   ├── src/main/resources/   # application.properties & profiles
│   ├── pom.xml
│   └── mvnw.cmd
├── frontend/                 # Angular SPA
│   ├── src/app/
│   │   ├── components/       # Patient, Appointment, Consultation screens
│   │   ├── models/           # TypeScript interfaces & DTOs
│   │   └── services/         # API HTTP services
│   └── package.json
└── README.md
```

---

## Setup & Running Instructions

### 1. Database (MySQL)
Ensure MySQL is running on `localhost:3306` with database `opd_db`:
```sql
CREATE DATABASE IF NOT EXISTS opd_db;
```
Or run with Docker:
```bash
docker run -d --name opd-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=opd_db mysql:8.0
```

### 2. Backend (Spring Boot)
From `backend/` directory:
```bash
.\mvnw.cmd spring-boot:run
```
Backend will be available on `http://localhost:8080`.

### 3. Frontend (Angular)
From `frontend/` directory:
```bash
npm install
npm start
```
Frontend will be available on `http://localhost:4200`.
