# Hospital OPD Management System — Version 2.0 (v2.0.0)

[![Release](https://img.shields.io/badge/Release-Version%202.0%20(Advanced)-blue.svg)](#)
[![Status](https://img.shields.io/badge/Features%201%2C2%2C3%2C4-Completed%20%E2%9C%85-brightgreen.svg)](#)
[![Razorpay](https://img.shields.io/badge/Payments-Razorpay%20Integrated%20%F0%9F%92%B3-blueviolet.svg)](#)
[![Tests](https://img.shields.io/badge/Tests-32%2F32%20Passed%20(100%25)-success.svg)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green.svg)](#)
[![Angular](https://img.shields.io/badge/Angular-19-red.svg)](#)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)](#)

> **Enterprise Hospital OPD Management System with Razorpay Payment Integration, Digital E-Prescriptions, Doctor Slot Conflict Detection, and Patient Medical Profiles.**  
> Built with **Spring Boot 3 + JPA (Hibernate)**, **MySQL 8.0**, **Angular 19**, and the official **Razorpay Java SDK**.

---

## 🎯 Deliverables & Features Matrix

| Module | Feature Details | Implementation Status | Screen / API Mapping |
| :--- | :--- | :---: | :--- |
| **Feature 1: Digital E-Prescription (Rx)** | • Itemized prescriptions: Medicine name, Dosage, Frequency, Duration, Instructions<br>• One-to-many persistence linked to Consultation<br>• Digital Prescription Slip rendering with print preview | ✅ **COMPLETE** | `/consultations`<br>`POST /api/consultations`<br>`GET /api/consultations/patient/{id}` |
| **Feature 2: OPD Billing & Razorpay Integration** | • Auto-generated invoice on consultation completion<br>• Breakdown: Doctor Consultation Fee + 18% GST = Grand Total<br>• Razorpay Order creation API (`/create-order/{id}`)<br>• Razorpay Signature verification (HMAC SHA-256) & settlement<br>• Reception Counter Cash payment settlement<br>• Printable Tax Invoice with hospital branding & stamp | ✅ **COMPLETE** | `/billing`<br>`GET /api/billing`<br>`POST /api/billing/create-order/{id}`<br>`POST /api/billing/verify-payment`<br>`POST /api/billing/cash-payment/{id}` |
| **Feature 3: Doctor & Department Management** | • Department roster (Cardiology, Orthopedics, General Medicine, Pediatrics)<br>• Doctor entities with consultation fees and room numbers<br>• Real-time Doctor Slot Conflict Detection (±30 min window check) | ✅ **COMPLETE** | `/appointments`<br>`GET /api/doctors`<br>`GET /api/departments`<br>`GET /api/doctors/check-conflict` |
| **Feature 4: Patient Medical Profile** | • Blood Group classification (A+, B+, O+, AB+, etc.)<br>• High-priority Drug Allergies recorded & highlighted in red badges<br>• Chronic medical conditions tracking<br>• Doctor Consultation Warning Banner to prevent adverse drug events | ✅ **COMPLETE** | `/patients` & `/consultations`<br>`POST /api/patients`<br>`GET /api/patients` |

---

## 🏗️ Architecture & Technology Stack

- **Backend**: Java 23, Spring Boot 3.4, Spring Data JPA, Hibernate ORM, Jakarta Bean Validation
- **Payment Gateway**: Razorpay Java SDK (`com.razorpay:razorpay-java:1.4.3`) with mock fallback for sandbox testing
- **Database**: MySQL 8.0 (Docker container `opd-mysql`, port `3306`)
- **Frontend**: Angular 19, TypeScript, Reactive Forms, Component-scoped CSS, Razorpay Checkout JS
- **Testing**: JUnit 5, Mockito, MockMvc (32 unit & integration tests, 100% pass rate)
- **Version Control**: Git / GitHub (`main` branch, atomic commits, tagged releases)

---

## 📁 Project Structure

```
hospital/
├── backend/
│   ├── src/main/java/com/hospital/opd/
│   │   ├── config/           # CORS security & automated DataInitializer seeder (v2.0)
│   │   ├── controller/       # REST API endpoints (Patient, Appointment, Consultation, Doctor, Billing)
│   │   ├── dto/              # Request / Response DTOs (Prescription, RazorpayOrder, PaymentVerification, Bill)
│   │   ├── entity/           # JPA Entities (Patient, Doctor, Department, Appointment, Consultation, PrescriptionItem, Bill)
│   │   ├── exception/        # Global Exception Handler (RFC 7807)
│   │   ├── repository/       # Repositories with custom queries (conflict check, today's queue, bills)
│   │   └── service/          # Business logic (Patient, Appointment, Consultation, Doctor, Billing)
│   ├── src/main/resources/   # MySQL (port 8081) and Razorpay properties
│   ├── src/test/java/        # 32 unit & integration test suites
│   ├── pom.xml
│   └── mvnw.cmd
├── frontend/
│   ├── src/app/
│   │   ├── components/
│   │   │   ├── patient/      # Medical Profile, Drug Allergies alert, directory
│   │   │   ├── appointment/  # Doctor selection, fee preview, real-time slot conflict warning
│   │   │   ├── consultation/ # Vitals entry, Drug Allergy Alert Banner, E-Prescription builder (Rx)
│   │   │   ├── billing/      # OPD Billing dashboard, Razorpay Checkout modal, Cash settlement, Printable Tax Invoice
│   │   │   └── navbar/       # Header navigation with live status indicator
│   │   ├── models/           # TypeScript interfaces (Patient, Doctor, Consultation, Prescription, Bill)
│   │   └── services/         # Angular HTTP services (Patient, Appointment, Consultation, Doctor, Billing)
│   └── package.json
├── TEST_REPORT.md            # Comprehensive test execution & quality report (32/32 passing)
└── README.md
```

---

## 💳 Razorpay Payment Gateway Workflow

```
[Doctor Consultation Completed]
               │
               ▼
[Bill Auto-Generated in Database (Status: PENDING)]
               │
               ├────────────────────────────────────────┐
               ▼                                        ▼
   [Option A: Pay via Razorpay]             [Option B: Pay via Cash]
               │                                        │
    1. POST /create-order/{billId}              1. Reception Cash Confirmation
    2. Opens Razorpay Checkout Modal            2. POST /cash-payment/{billId}
    3. Customer completes transaction                   │
    4. POST /verify-payment (HMAC SHA-256)              │
               │                                        │
               └───────────────────┬────────────────────┘
                                   ▼
                [Bill Updated to PAID in Database]
                                   │
                                   ▼
             [Printable Hospital Tax Invoice Generated]
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

## 🧪 Automated Test Verification
Run all 32 automated tests covering services, conflict logic, payment verification, and controllers:
```powershell
cd backend
.\mvnw.cmd test
```
**Results:** `Tests run: 32, Failures: 0, Errors: 0, Skipped: 0` (100% Pass Rate).  
See [TEST_REPORT.md](TEST_REPORT.md) for full metrics and execution logs.
