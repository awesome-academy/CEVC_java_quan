# 🧩 Member Management System

A **Spring Boot-based web application** for managing members, courses, and events — featuring **Admin UI (SSR with
Thymeleaf + Tailwind)** and **Client RESTful APIs**.

---

## 🚀 Tech Stack

| Layer                | Technology                                  |
|:---------------------|:--------------------------------------------|
| **Backend**          | Spring Boot (3.x), Maven                    |
| **Database**         | MySQL, JPA (Hibernate), Flyway              |
| **Frontend (Admin)** | Thymeleaf, TailwindCSS                      |
| **Client API**       | RESTful JSON endpoints                      |
| **Utilities**        | Lombok, Validation, ModelMapper             |
| **Build & Run**      | Maven Wrapper, .env for environment configs |

---

## 🧱 Project Structure

```
member-management-system/
├── src/
│ ├── main/
│ │ ├── java/com/example/membermanagementsystem/
│ │ │ ├── config/
│ │ │ ├── controller/
│ │ │ ├── dto/
│ │ │ ├── entity/
│ │ │ ├── repository/
│ │ │ ├── service/
│ │ │ └── MemberManagementSystemApplication.java
│ │ └── resources/
│ │ ├── static/ # Tailwind assets
│ │ ├── templates/ # Thymeleaf views (admin)
│ │ ├── application.yml
│ │ └── db/migration/ # Flyway scripts
│ └── test/
│ └── java/... # Unit tests
├── .env # Environment variables (not committed)
├── .gitignore
├── pom.xml
└── README.md
```

---

## ⚙️ Setup Instructions

### 1️⃣ Prerequisites

- **Java 17+**
- **Maven 3.9+**
- **MySQL 8+**
- **Node.js + npm** (for Tailwind build)

### 2️⃣ Environment Configuration

Create a `.env` file in the project root (not committed to Git):

```
# .env (local)
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/member_management_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=root
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=true
SERVER_PORT=8088
```

### 3️⃣ Build & Run

**Using Maven:**

```bash
# Clean & build
mvn clean package

# Run application
mvn spring-boot:run
```

**Or directly:**

```bash
java -jar target/member-management-system-0.0.1-SNAPSHOT.jar
```

Then visit:

```
http://localhost:8088/admin     → Admin UI
http://localhost:8088/api/...   → Client REST APIs
```

### 4️⃣ Database Migration (optional Flyway)

If you use Flyway for versioned schema management, add scripts under:
`src/main/resources/db/migration/` and they will be auto-applied on startup.
---

## 🧪 Health Check

Test API:

```bash
GET http://localhost:8088/health
```

Response:

```json
{
  "status": "UP"
}
```

---

## 🧰 Development Notes

- Use `application.yml` for defaults, `.env` for environment-specific overrides.
- Always commit after running:

```bash
mvn clean verify
```

- Tailwind build (if needed):

```bash
npm install
npm run build
```

---

## 🧑‍💻 Authors

Team: Member Management System (Spring Boot Project)

Maintainer: nguyen.tien.quan@sun-asterisk.com
