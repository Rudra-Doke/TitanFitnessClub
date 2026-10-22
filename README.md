# Titan Fitness Club

**A gym management web application built with Java 21, Spring Boot, Thymeleaf, and MySQL.**

Titan Fitness Club provides a centralized platform for managing gym operations and member experiences through separate administrator and member dashboards.

## Features

- **Member management:** Registration, profile management, and account status controls
- **Membership management:** Plans, renewals, and membership changes
- **Attendance tracking:** Record and review member attendance
- **Payment management:** Payment records, paid-only revenue reporting, and optional Razorpay Checkout integration
- **Workout and diet plans:** Organize fitness plans for members
- **Trainer and equipment management:** Manage gym resources
- **Announcements and reports:** Support communication and administrative oversight
- **Security:** Role-based access control, protected form submissions, and secure session configuration

## Technology Stack

- Java 21
- Spring Boot and Spring Security
- Thymeleaf, HTML, and CSS
- Spring Data JPA and Hibernate
- MySQL
- Maven
- Docker and Render deployment support

## Live Demo

https://titanfitnessclub.onrender.com/

## Source Code

https://github.com/Rudra-Doke/TitanFitnessClub

## Running Locally

Use Java 21 and MySQL. Configure the database connection through environment variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `PORT` (optional; defaults to `8080`)

Run the test suite before deployment:

- Windows: `mvnw.cmd test`
- macOS/Linux: `./mvnw test`

See the sections below for deployment configuration, payment setup, security behavior, and verification details.
