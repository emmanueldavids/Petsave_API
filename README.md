# PetSave API 🐾

A comprehensive backend REST API for the **PetSave** platform — featuring a complete User Dashboard & Donation Flow system with pet adoption, community features, and secure authentication.

---

## 🌟 Features

### **💰 User Dashboard & Donation Flow**
- ✅ **Dashboard Statistics** → Real-time user overview with metrics
- ✅ **Donation History** → Complete donation records with receipt download
- ✅ **Social Sharing** → Multi-platform sharing (WhatsApp, Facebook, Twitter, Email)
- ✅ **Anonymous Donations** → Privacy toggle with impact visualization
- ✅ **Professional Receipts** → PDF generation with tax-deductible information
- ✅ **Post-Donation Flow** → Success popup, invoice modal, thank you page

### **🐾 Pet Adoption System**
- ✅ **Pet Management** → Complete CRUD with image uploads
- ✅ **Adoption Applications** → Full application workflow
- ✅ **Status Tracking** → Real-time application status updates
- ✅ **Admin Approval System** → Efficient application review and approval
- ✅ **Application Timeline** → Visual progress tracking

### **🔐 Authentication & Security**
- ✅ **JWT Authentication** → Secure token-based auth
- ✅ **Email Verification** → OTP-based verification
- ✅ **Password Reset** → Secure password recovery
- ✅ **Role-based Access** → User permission management
- ✅ **CORS Configuration** → Cross-origin security

---

## 🚀 Technology Stack

### **Backend Framework**
- **Java 17** → Modern Java features
- **Spring Boot 3.x** → Enterprise-grade framework
- **Spring Security** → JWT authentication and authorization
- **Spring Data JPA** → Database ORM
- **PostgreSQL** → Primary database
- **Maven** → Build and dependency management

### **Additional Technologies**
- **Lombok** → Code generation and boilerplate reduction
- **Jakarta Validation** → Input validation
- **Swagger/OpenAPI** → API documentation
- **Hibernate** → JPA implementation

---

## 📁 Project Structure

```
src/main/java/com/petsave/petsave/
├── Config/                    # Security & Application Configs
│   ├── SecurityConfig.java    # Spring Security configuration
│   └── FileUploadConfig.java  # File upload configuration
├── Controller/                # REST API Controllers
│   ├── AuthController.java    # Authentication & dashboard stats
│   ├── DonationController.java # Donation processing & receipts
│   ├── PetController.java     # Pet management
│   └── AdoptionController.java # Adoption workflow
├── Entity/                    # JPA Database Entities
│   ├── User.java             # User entity
│   ├── Pet.java              # Pet information
│   ├── Donation.java         # Donation records
│   └── Adoption.java         # Adoption requests
├── Repository/                # Spring Data JPA Repositories
│   ├── UserRepository.java   # User data access
│   ├── PetRepository.java   # Pet queries
│   ├── DonationRepository.java # Donation data
│   └── AdoptionRepository.java # Adoption management
├── Service/                   # Business Logic Layer
│   ├── AuthService.java     # Authentication logic
│   ├── DonationService.java  # Payment processing
│   ├── PetService.java      # Pet management
│   └── AdoptionService.java  # Adoption workflow
├── dto/                       # Data Transfer Objects
│   ├── DonationRequest.java  # Donation creation
│   ├── AdoptionRequest.java # Adoption requests
│   └── ...                  # Other DTOs
└── Utils/                     # Utility Classes
    ├── JwtUtil.java          # JWT token management
    └── ...                  # Other utilities
```

---

## 🔑 Authentication Endpoints

| Method | Endpoint                        | Description                |
|--------|----------------------------------|----------------------------|
| POST   | `/api/auth/register`            | Register a new user        |
| POST   | `/api/auth/login`               | Login and get tokens       |
| POST   | `/api/auth/refresh`             | Refresh access token       |
| POST   | `/api/auth/reset-password/request` | Request password reset  |
| POST   | `/api/auth/reset-password/confirm` | Confirm password reset  |
| GET    | `/api/auth/dashboard-stats`       | Get user dashboard stats  |
| GET    | `/api/auth/users`               | Get all users              |
| GET    | `/api/auth/users/{id}`          | Get user by ID             |

---

## � Donation API

| Method | Endpoint                 | Description                      |
|--------|--------------------------|--------------------------------|
| POST   | `/api/donations`         | Initialize donation (Paystack)   |
| GET    | `/api/donations`         | Get all donations                |
| GET    | `/api/donations/{id}`    | Get donation by ID               |
| POST   | `/api/donations/{id}/share` | Share donation impact            |
| GET    | `/api/donations/{id}/receipt` | Download donation receipt          |

---

## � Pet & Adoption API

| Method | Endpoint                 | Description                |
|--------|--------------------------|----------------------------|
| GET    | `/api/pets`               | Get available pets         |
| GET    | `/api/pets/{id}`          | Get pet details            |
| POST   | `/api/pets`               | Create new pet            |
| PUT    | `/api/pets/{id}`          | Update pet information      |
| DELETE | `/api/pets/{id}`          | Delete pet                |
| POST   | `/api/adoptions`           | Submit adoption request    |
| GET    | `/api/adoptions`           | Get adoption status        |
| PUT    | `/api/adoptions/{id}/status` | Update application status |

---

## 🛡️ Security Features

### **Authentication & Authorization**
- **JWT Tokens** → Secure stateless authentication
- **Role-based Access** → User permission management
- **Password Hashing** → BCrypt encryption
- **Token Refresh** → Automatic token renewal
- **CORS Configuration** → Cross-origin security

### **Input Validation**
- **Jakarta Validation** → Request validation
- **File Upload Security** → Type and size validation
- **SQL Injection Prevention** → JPA parameter binding
- **XSS Protection** → Input sanitization

---

## ⚙️ Environment Variables

Create an `.env` file or set in `application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/petsaveDB
spring.datasource.username=your_db_user
spring.datasource.password=your_db_pass
spring.jpa.hibernate.ddl-auto=update

# JWT Configuration
jwt_secret_key=your-super-secret-jwt-key
jwt_expiration=86400000

# File Upload Configuration
file.upload.dir=uploads
file.upload.max-size=10485760
```

---

## 🧪 API Documentation

### **Swagger UI**
Interactive API documentation available at:
```
http://localhost:8080/swagger-ui/index.html
```

### **OpenAPI Spec**
Raw API specification at:
```
http://localhost:8080/v3/api-docs
```

---

## ▶️ Running the Application

### **Prerequisites**
- Java 17+
- PostgreSQL database
- Maven 3.6+

### **Installation & Setup**
```bash
# Clone repository
git clone https://github.com/emmanueldavids/Petsave_API.git
cd Petsave_API

# Set up database (create petsave database)
createdb petsave
```

### **Default Configuration**
- **Server Port** → 8080
- **Database** → PostgreSQL on localhost:5432
- **File Uploads** → ./uploads directory
- **JWT Expiration** → 24 hours

---

## 📈 Performance Features

### **Database Optimization**
- **Indexes** → Optimized query performance
- **Lazy Loading** → Efficient data fetching
- **Pagination** → Large dataset handling
- **Connection Pooling** → Database connection management

### **Caching Strategy**
- **Entity Caching** → Second-level cache ready
- **Query Cache** → Frequently accessed data
- **Static Resources** → File serving optimization

---

## 🔧 Development

### **Code Quality**
- **Lombok** → Reduced boilerplate code
- **Spring Boot DevTools** → Hot reloading
- **Maven** → Dependency management
- **Git Hooks** → Pre-commit validation

### **Testing**
- **Unit Tests** → Service layer testing
- **Integration Tests** → API endpoint testing
- **Repository Tests** → Database layer testing

---

## 🚀 Deployment

### **Production Configuration**
```bash
# Build for production
./mvnw clean package

# Run with production profile
java -jar target/petsave-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### **Docker Support**
```dockerfile
# Build Docker image
docker build -t petsave-api .

# Run container
docker run -p 8080:8080 petsave-api
```

---

## 📊 Monitoring & Logging

### **Logging Configuration**
- **Log Levels** → Configurable logging levels
- **Request Logging** → API request/response tracking
- **Error Logging** → Comprehensive error tracking
- **Performance Metrics** → Response time monitoring

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 🎯 Current Status

### **✅ Completed Features**
- **User Dashboard** → Complete with real-time statistics
- **Donation System** → Full payment processing with receipts
- **Adoption System** → Complete application workflow
- **Social Sharing** → Multi-platform sharing integration
- **Authentication** → Secure JWT-based system
- **File Upload** → Secure image and file handling
- **Admin Approval** → Efficient application management

### **� Production Ready**
- **Frontend Integration** → Complete User Dashboard & Donation Flow
- **API Documentation** → Comprehensive Swagger documentation
- **Security** → Enterprise-grade authentication
- **Performance** → Optimized for production use
- **Error Handling** → Comprehensive error management

---

## 🎉 Built with ❤️ for PetSave Animal Shelter Community 🐾** 🐾