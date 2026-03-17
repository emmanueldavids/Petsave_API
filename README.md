# PetSave API 🐾

A comprehensive backend REST API for the **PetSave** platform — featuring a complete Community Hub with social interactions, pet adoption system, donation processing, and secure authentication.

---

## 🌟 Features

### **Community Hub Social Features**
- ✅ **Posts Management** → Create, edit, delete posts with image uploads
- ✅ **Interactive Comments** → Nested comments with likes and replies
- ✅ **Like System** → Like/unlike posts and individual comments
- ✅ **Private Chat** → Real-time messaging with file/image support
- ✅ **User Profiles** → Rich user information and activity tracking
- ✅ **File Upload** → Secure image and file uploads with validation

### **Core Platform Features**
- ✅ **User Authentication** → JWT-based secure login/registration
- ✅ **Email Verification** → OTP-based email verification
- ✅ **Password Reset** → Secure password recovery
- ✅ **Pet Management** → Complete pet adoption workflow
- ✅ **Donation System** → Paystack payment integration
- ✅ **Blog System** → News and content management
- ✅ **Contact System** → Shelter communication

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
- **Paystack API** → Payment processing
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
│   ├── AuthController.java    # Authentication endpoints
│   ├── PostController.java    # Community posts API
│   ├── ChatController.java    # Private messaging API
│   ├── DonationController.java # Donation processing
│   ├── PetController.java     # Pet management
│   ├── AdoptionController.java # Adoption workflow
│   ├── BlogController.java    # Blog management
│   └── FileUploadController.java # File upload API
├── Entity/                    # JPA Database Entities
│   ├── User.java             # User entity with community features
│   ├── Post.java             # Social posts with likes/comments
│   ├── Comment.java          # Nested comments with replies
│   ├── PostLike.java         # Post likes tracking
│   ├── CommentLike.java      # Comment likes tracking
│   ├── Chat.java             # Private chat management
│   ├── Message.java          # Chat messages with files
│   ├── Donation.java         # Donation records
│   ├── Pet.java              # Pet information
│   └── Adoption.java         # Adoption requests
├── Repository/                # Spring Data JPA Repositories
│   ├── UserRepository.java   # User data access
│   ├── PostRepository.java   # Post queries and search
│   ├── CommentRepository.java # Comment management
│   ├── ChatRepository.java   # Chat and message queries
│   └── ...                  # Other repositories
├── Service/                   # Business Logic Layer
│   ├── PostService.java      # Post management and interactions
│   ├── ChatService.java      # Chat and messaging logic
│   ├── EmailService.java     # Email notifications
│   ├── DonationService.java  # Payment processing
│   └── ...                  # Other services
├── dto/                       # Data Transfer Objects
│   ├── PostRequest.java      # Post creation/update
│   ├── PostResponse.java     # Post data with interactions
│   ├── MessageRequest.java   # Chat message creation
│   ├── CommentRequest.java   # Comment creation
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
| POST   | `/api/auth/verify`              | Verify email with OTP      |
| POST   | `/api/auth/login`               | Login and get tokens       |
| POST   | `/api/auth/refresh`             | Refresh access token       |
| POST   | `/api/auth/reset-password/request` | Request password reset  |
| POST   | `/api/auth/reset-password/confirm` | Confirm password reset  |
| GET    | `/api/auth/logout`              | Manual logout (client-side)|
| GET    | `/api/auth/users`               | Get all users              |
| GET    | `/api/auth/users/{id}`          | Get user by ID             |

---

## 📝 Community Hub API

### **Posts Management**
| Method | Endpoint                        | Description                |
|--------|----------------------------------|----------------------------|
| GET    | `/api/posts`                     | Get all posts (paginated)  |
| POST   | `/api/posts`                     | Create new post            |
| GET    | `/api/posts/{id}`                | Get specific post          |
| PUT    | `/api/posts/{id}`                | Update post (owner only)   |
| DELETE | `/api/posts/{id}`                | Delete post (owner only)   |
| GET    | `/api/posts/search`              | Search posts               |
| GET    | `/api/posts/user/{userId}`      | Get user posts             |

### **Interactions**
| Method | Endpoint                        | Description                |
|--------|----------------------------------|----------------------------|
| POST   | `/api/posts/{id}/like`          | Like post                  |
| DELETE | `/api/posts/{id}/like`          | Unlike post                |
| POST   | `/api/posts/{id}/comments`      | Create comment             |
| GET    | `/api/posts/{id}/comments`      | Get post comments          |
| POST   | `/api/posts/comments/{id}/like` | Like comment              |
| DELETE | `/api/posts/comments/{id}/like` | Unlike comment            |

---

## 💬 Chat & Messaging API

| Method | Endpoint                        | Description                |
|--------|----------------------------------|----------------------------|
| GET    | `/api/chats/{userId}`            | Get/create chat            |
| GET    | `/api/chats`                     | Get user chats             |
| POST   | `/api/chats/messages`            | Send message               |
| GET    | `/api/chats/{id}/messages`       | Get chat messages          |
| POST   | `/api/chats/{id}/read`           | Mark chat as read          |
| GET    | `/api/chats/messages/unread/count` | Get unread count        |
| POST   | `/api/chats/messages/{id}/read` | Mark message as read      |

---

## 📁 File Upload API

| Method | Endpoint                        | Description                |
|--------|----------------------------------|----------------------------|
| POST   | `/api/upload/image`              | Upload post image          |
| POST   | `/api/upload/file`               | Upload chat file           |
| GET    | `/api/upload/files/{filename}`   | Serve uploaded file        |

---

## 🐾 Pet & Adoption API

| Method | Endpoint                        | Description                |
|--------|----------------------------------|----------------------------|
| GET    | `/api/pets`                      | Get available pets         |
| GET    | `/api/pets/{id}`                 | Get pet details            |
| POST   | `/api/adoptions`                 | Submit adoption request    |
| GET    | `/api/adoptions`                 | Get adoption status        |

---

## 💳 Donation API

| Method | Endpoint                 | Description                      |
|--------|--------------------------|----------------------------------|
| POST   | `/api/donations`         | Initialize donation (Paystack)   |
| GET    | `/api/donations`         | Get all donations                |
| GET    | `/api/donations/{id}`    | Get donation by ID               |
| POST   | `/api/donations/webhook` | Paystack webhook handler        |

---

## 📊 Database Schema

### **Community Tables**
- **posts** → Social posts with metadata
- **comments** → Nested comments with parent-child relationships
- **post_likes** → Post like tracking
- **comment_likes** → Comment like tracking
- **chats** → Private chat sessions
- **messages** → Chat messages with file attachments
- **post_tags** → Post tag relationships

### **Core Tables**
- **users** → User profiles with community features
- **pets** → Pet information and availability
- **adoptions** → Adoption requests and status
- **donations** → Payment records and transactions
- **blogs** → Blog posts and content

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
spring.datasource.url=jdbc:postgresql://localhost:5432/petsave
spring.datasource.username=your_db_user
spring.datasource.password=your_db_pass
spring.jpa.hibernate.ddl-auto=update

# Paystack Payment Integration
paystack.secret.key=sk_test_your_key
paystack.public.key=pk_test_your_key

# Email Configuration
spring.mail.username=your-email@example.com
spring.mail.password=your_email_password

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
# Clone the repository
git clone https://github.com/emmanueldavids/Petsave_API.git
cd Petsave_API

# Set up database (create petsave database)
createdb petsave

# Configure environment variables
# Copy and modify application.properties.example

# Run the application
./mvnw spring-boot:run
# or
mvn spring-boot:run
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
- **Community Hub** → Complete social platform with posts, comments, likes
- **Private Chat** → Real-time messaging with file support
- **Photo Upload** → Secure image upload with validation
- **Post Editing** → Edit own posts with permissions
- **User Profiles** → Rich user information and interactions
- **Authentication** → JWT-based secure auth system
- **Pet Adoption** → Complete adoption workflow
- **Donation System** → Paystack payment integration
- **File Management** → Secure upload and serving

### **🔄 In Development**
- **Real-time Notifications** → WebSocket support
- **Advanced Search** → Full-text search capabilities
- **Analytics Dashboard** → User engagement metrics
- **Push Notifications** → Mobile app integration

---

**Built with ❤️ for PetSave Animal Shelter Community** 🐾