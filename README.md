# PetSave API 🐾

A backend REST API for the **PetSave** platform — helping users discover blogs, make donations, and authenticate securely.

---

## 📦 Features

- ✅ User Registration, Login & JWT Authentication
- 📧 Email Verification & Password Reset
- 💸 Donation Processing with Paystack Integration
- 📄 Swagger Documentation
- 🔐 Secure Endpoints with Role-Based Access (optional)

---

## 🚀 Technologies Used

- Java 17
- Spring Boot 3.x
- Spring Security (JWT)
- Spring Data JPA + PostgreSQL
- Lombok
- Paystack API
- Swagger / OpenAPI

---

## 🔧 Project Structure

```
src/main/java/com/petsave/petsave/
├── Config/                # Security & App Configs
├── Controller/            # REST Controllers
├── Entity/                # JPA Entities (User, Donation, etc.)
├── Repository/            # Spring Data Repositories
├── Service/               # Business Logic
├── Utils/                 # Helper classes (e.g. JWT)
└── dto/                   # Data Transfer Objects
```

---

## 🔑 Authentication Endpoints

| Method | Endpoint                        | Description                |
|--------|----------------------------------|----------------------------|
| POST   | `/api/auth/register`            | Register a new user        |
| POST   | `/api/auth/verify`              | Verify email with OTP      |
| POST   | `/api/auth/login`               | Login and get tokens       |
| POST   | `/api/auth/refresh`             | Refresh access token       |
| POST   | `/api/auth/reset-password/request` | Request password reset  |
| POST   | `/api/auth/reset-password/confirm` | Confirm password reset  |
| GET    | `/api/auth/logout`              | Manual logout (client-side)|
| GET    | `/api/auth/users`               | Get all users              |
| GET    | `/api/auth/users/{id}`          | Get user by ID             |

---

## 💳 Donation Endpoints

| Method | Endpoint                 | Description                      |
|--------|--------------------------|----------------------------------|
| POST   | `/api/donations/pay`     | Initialize a donation (Paystack) |
| GET    | `/api/donations`         | Get all donations                |
| GET    | `/api/donations/{id}`    | Get donation by ID               |

---

## 🧪 Swagger Documentation

You can test and explore the API via Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

---

## ⚙️ Environment Variables

Create an `.env` or set in `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/petsave
spring.datasource.username=your_db_user
spring.datasource.password=your_db_pass

paystack.secret.key=sk_test_your_key
spring.mail.username=youremail@example.com
spring.mail.password=yourpassword
```

---

## ▶️ Running the App

```bash
./mvnw spring-boot:run
# or
mvn spring-boot:run
```


---

## 📫 Contact

Feel free to reach out for suggestions, questions, or contributions.

Made with ❤️ for animal lovers.