# Hospital OPD Management Mini-Module — Version 1.0 (v1.0.0)

[![Release](https://img.shields.io/badge/Release-Version%201.0-blue.svg)](#)
[![Status](https://img.shields.io/badge/Core%20Tasks-Completed%20%E2%9C%85-brightgreen.svg)](#)
[![Tests](https://img.shields.io/badge/Tests-24%2F24%20Passed%20(100%25)-success.svg)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green.svg)](#)
[![Angular](https://img.shields.io/badge/Angular-19-red.svg)](#)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)](#)

> **Core Tasks Given — 100% Completed & Demo-Ready**  
> A full-stack Outpatient Department (OPD) clinical mini-module engineered with **Spring Boot 3 + JPA (Hibernate)**, **MySQL 8.0**, and **Angular 19**.

---

## 🎯 Core Task Deliverables Matrix

| Scope Item | Requirement Details | Implementation Status | Screen / API Mapping |
| :--- | :--- | :---: | :--- |
| **1. Patient Registration** | • Add a patient: Name, Gender, Age, Phone number<br>• List patients directory<br>• Search by name or phone | ✅ **COMPLETE** | Screen 1 (`/patients`)<br>`POST /api/patients`<br>`GET /api/patients?query=` |
| **2. Appointment Booking** | • Book appointment: Patient + Date/time + Doctor<br>• List appointments for today<br>• Full queue view | ✅ **COMPLETE** | Screen 2 (`/appointments`)<br>`POST /api/appointments`<br>`GET /api/appointments/today` |
| **3. Consultation Summary** | • Enter vitals (BP, Heart Rate, Temperature)<br>• Enter clinical notes/diagnosis<br>• Mark consultation complete<br>• View completed consultations for patient | ✅ **COMPLETE** | Screen 3 (`/consultations`)<br>`POST /api/consultations`<br>`GET /api/consultations/patient/{id}` |

---

## 🏗️ Architecture & Technology Stack

- **Backend**: Java 23, Spring Boot 3.4, Spring Data JPA, Hibernate ORM, Jakarta Bean Validation
- **Database**: MySQL 8.0 (Docker container `opd-mysql`, port `3306`)
- **Frontend**: Angular 19, TypeScript, Reactive Forms, Component-scoped CSS
- **Testing**: JUnit 5, Mockito, MockMvc (24 test cases, 100% pass rate)
- **Version Control**: Git / GitHub (`main` branch + release tags)

---

## 📁 Project Structure

```
hospital/
├── backend/
│   ├── src/main/java/com/hospital/opd/
│   │   ├── config/           # CORS security & automated DataInitializer seeder
│   │   ├── controller/       # REST API endpoints (/api/patients, /api/appointments, /api/consultations)
│   │   ├── dto/              # Request / Response DTOs with validation rules
│   │   ├── entity/           # JPA Entities (Patient, Appointment, Consultation)
│   │   ├── exception/        # Global Exception Handler (RFC 7807)
│   │   ├── repository/       # Spring Data Repositories with custom search/filter queries
│   │   └── service/          # Business logic interfaces & transactional implementations
│   ├── src/main/resources/   # MySQL (port 8081) and fallback dev profiles
│   ├── src/test/java/        # 24 unit & integration test suites
│   ├── pom.xml
│   └── mvnw.cmd
├── frontend/
│   ├── src/app/
│   │   ├── components/
│   │   │   ├── patient/      # Screen 1: Registration form & live search table
│   │   │   ├── appointment/  # Screen 2: Booking form & today's queue
│   │   │   ├── consultation/ # Screen 3: Vitals entry & patient history cards
│   │   │   └── navbar/       # Header navigation with live status indicator
│   │   ├── models/           # TypeScript interfaces & DTOs
│   │   └── services/         # Angular HTTP services
│   └── package.json
├── TEST_REPORT.md            # Comprehensive test execution & quality report
└── README.md
```

---

## 🚀 Quick Start Guide

### 1. Database (Docker MySQL)
```powershell
docker run -d --name opd-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=opd_db mysql:8.0
```

### 2. Backend (Spring Boot REST API)
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```
API runs on: **`http://localhost:8081`**

### 3. Frontend (Angular Single Page App)
```powershell
cd frontend
npm start
```
UI runs on: **`http://localhost:4200`**

---

## 🧪 Testing & Quality Assurance
Run all 24 backend automated tests:
```powershell
cd backend
.\mvnw.cmd test
```
See [TEST_REPORT.md](TEST_REPORT.md) for full test metrics, assertions, and verification logs.
