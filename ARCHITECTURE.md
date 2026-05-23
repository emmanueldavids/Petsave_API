# PetSave API — System Architecture

## Overview

PetSave is a pet rescue and adoption platform backend built with Spring Boot 3.5. It handles pet listings, adoption applications, donation processing, user authentication, real-time notifications, blog management, and file uploads.

**Stack:**
- Runtime: Java 17, Spring Boot 3.5
- Database: PostgreSQL (via HikariCP connection pool)
- Authentication: JWT (access + refresh tokens)
- Payments: Paystack
- File storage: Cloudinary
- Email: Brevo (HTTPS API)
- Real-time: WebSocket
- Templates: Thymeleaf (email rendering)
- Docs: SpringDoc OpenAPI / Swagger UI
- Deployment: Docker on Railway

---

## High-Level Architecture

```
                        ┌─────────────────────────────────┐
                        │        Frontend (Vercel)         │
                        │  https://petsave-frontend.       │
                        │         vercel.app               │
                        └────────────┬────────────────────┘
                                     │ HTTPS / WebSocket
                        ┌────────────▼────────────────────┐
                        │      Railway (Docker Container)  │
                        │                                  │
                        │  ┌──────────────────────────┐   │
                        │  │   Spring Boot API         │   │
                        │  │   (port $PORT)            │   │
                        │  │                           │   │
                        │  │  Security Filter Chain    │   │
                        │  │  JWT Filter               │   │
                        │  │  REST Controllers (11)    │   │
                        │  │  Services (17)            │   │
                        │  │  Repositories (6)         │   │
                        │  └──────────┬───────────────┘   │
                        │             │                    │
                        └─────────────┼────────────────────┘
                                      │
               ┌──────────────────────┼──────────────────────┐
               │                      │                      │
    ┌──────────▼──────┐   ┌──────────▼──────┐  ┌───────────▼──────┐
    │   PostgreSQL     │   │   Cloudinary    │  │    Paystack      │
    │   (Railway)      │   │  (image store)  │  │  (payments)      │
    └─────────────────┘   └─────────────────┘  └──────────────────┘
               │
    ┌──────────▼──────┐
    │     Brevo       │
    │  (email API)    │
    └─────────────────┘
```

---

## Project Structure

```
src/main/java/com/petsave/petsave/
├── Config/           14 configuration classes
├── Controller/       11 REST controllers
├── Entity/           5 entities + 8 enums
├── Repository/       6 JPA repositories
├── Service/          17 service classes
├── dto/              30 request/response DTOs
├── Utils/            3 utility classes
└── PetsaveApplication.java
```

---

## Security Architecture

### Authentication Flow
```
Client → POST /api/auth/login
       → JwtUtil.generateAccessToken()  (15 min TTL)
       → JwtUtil.generateRefreshToken() (7 day TTL)
       → LoginResponse { accessToken, refreshToken, name, email, role }

Subsequent requests → Authorization: Bearer <accessToken>
       → JwtFilter.doFilterInternal()
       → JwtUtil.validateToken()
       → SecurityContextHolder.setAuthentication()
       → Controller method executes
```

### Token Refresh Flow
```
Client → POST /api/auth/refresh { email, refreshToken }
       → Validate refreshToken against DB
       → Check refreshTokenExpiry
       → Issue new accessToken + refreshToken
       → TokenResponse { accessToken, refreshToken }
```

### Public Endpoints (no JWT required)
```
/api/auth/**           Registration, login, OTP, password reset
/api/pets/**           All pet listings
/api/adoptions/**      All adoption listings
/api/donations/**      All donation listings
/api/blogs/**          Blog posts
/api/contact           Contact form
/api/upload/**         File uploads
/api/users/count       User count (public stat)
/api/users/all         User list (public)
/api/test/**           Health check, ping
/api/paystack/webhook  Paystack payment webhook
/ws                    WebSocket endpoint
/v3/api-docs/**        Swagger API docs
/swagger-ui/**         Swagger UI
```

---

## Data Model

### Entity Relationship Diagram

```
User (users)
 ├─── id (PK)
 ├─── name, email (unique), username (unique)
 ├─── password (BCrypt), role (USER/ADMIN)
 ├─── isVerified, verificationCode, verificationCodeExpiresAt
 ├─── otpType (EMAIL_VERIFICATION | PASSWORD_RESET)
 ├─── refreshToken, refreshTokenExpiry
 └─── createdAt, updatedAt
      │
      ├──< Adoption (adoptions)          [One-to-Many]
      └──< Donation (donations)          [One-to-Many]

Pet (pets)
 ├─── id (PK)
 ├─── name, breed, age, description, imageUrl
 ├─── type (DOG | CAT | BIRD | RABBIT | OTHER)
 ├─── status (RESCUED_DONATION | FOR_ADOPTION | PENDING_ADOPTION |
 │           ADOPTED | IN_TREATMENT | FOSTER_CARE | TEMPORARY_CARE |
 │           SPECIAL_NEEDS | SENIOR_PET | EMERGENCY_RESCUE)
 ├─── location, medicalHistory, specialNeeds, story
 ├─── adoptionFee, adoptionRequirements
 ├─── vaccinated, neutered, houseTrained, requiresExperienced
 ├─── temperament, goodWith, weight, color, microchipId
 ├─── available, uploadedBy, rescueDate, adoptionDate
 └─── viewCount, favoriteCount
      │
      ├──< Adoption                      [One-to-Many]
      └──< Donation                      [One-to-Many]

Adoption (adoptions)
 ├─── id (PK)
 ├─── petName, petBreed, petAge, petDescription, petImageUrl
 ├─── petType (DOG | CAT | BIRD | RABBIT | OTHER)
 ├─── status (PENDING | APPROVED | REJECTED | CANCELLED | COMPLETED)
 ├─── adopterName, adopterEmail, adopterPhone, adopterAddress
 ├─── applicationReason, applicationDate
 ├─── user_id (FK → users)
 ├─── pet_id  (FK → pets)
 └─── createdAt, updatedAt

Donation (donations)
 ├─── id (PK)
 ├─── donorName, amount, date
 ├─── email, gender (MALE | FEMALE | OTHER), country
 ├─── reference (Paystack transaction reference, indexed)
 ├─── paymentStatus (PENDING | COMPLETED | FAILED)
 ├─── user_id (FK → users, lazy)
 └─── pet_id  (FK → pets, lazy)

Blog (blogs)
 ├─── id (PK)
 ├─── title, content (TEXT), author
 ├─── imageUrl, imageType
 └─── createdAt

Notification (notifications)
 ├─── id (PK)
 ├─── userEmail, message
 ├─── type (DONATION_SUCCESS | DONATION_FAILED | ADOPTION_APPROVED |
 │         ADOPTION_REJECTED | APPLICATION_RECEIVED | APPLICATION_UPDATE |
 │         SYSTEM_ANNOUNCEMENT | CHAT_MESSAGE | PAYMENT_CONFIRMATION)
 ├─── priority (LOW | MEDIUM | HIGH | URGENT)
 ├─── isRead, createdAt, readAt
 └─── senderEmail
```

---

## API Endpoints Reference

### Auth — `/api/auth`
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/register` | Public | Register new user, sends OTP email |
| POST | `/verify-otp` | Public | Verify email OTP |
| POST | `/resend` or `/resend-otp` | Public | Resend verification code |
| POST | `/login` | Public | Login, returns JWT tokens |
| POST | `/refresh` | Public | Refresh access token |
| POST | `/reset-password/request` | Public | Request password reset OTP |
| POST | `/reset-password/confirm` | Public | Confirm password reset |
| GET | `/profile` | JWT | Get current user profile |
| PUT | `/profile` | JWT | Update current user profile |
| DELETE | `/account` | JWT | Delete account |
| POST | `/logout` | JWT | Logout |
| GET | `/dashboard-stats` | JWT | User donation and adoption stats |
| GET | `/users` | JWT | List all users |
| GET | `/users/{id}` | JWT | Get user by ID |
| GET | `/users/count` | JWT | Total user count |

### Pets — `/api/pets`
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | Public | List pets (paginated, filterable) |
| GET | `/{id}` | Public | Get pet by ID |
| POST | `/` | JWT | Create pet |
| POST | `/with-image` | JWT | Create pet with image upload |
| PUT | `/{id}` | JWT | Update pet |
| PUT | `/{id}/with-image` | JWT | Update pet with image |
| PATCH | `/{id}` | JWT | Partial update pet |
| PATCH | `/{id}/status` | JWT | Update pet status |
| PATCH | `/{id}/availability` | JWT | Toggle availability |
| PATCH | `/{id}/favorite` | JWT | Increment favorite count |
| DELETE | `/{id}` | JWT | Delete pet |
| GET | `/available` | Public | Available pets only |
| GET | `/type/{type}` | Public | Pets by type |
| GET | `/status/{status}` | Public | Pets by status |
| GET | `/location/{location}` | Public | Pets by location |
| GET | `/recently-rescued` | Public | Rescued in last 30 days |
| GET | `/popular` | Public | Ordered by view count |
| GET | `/senior` | Public | Age > 7 years |
| GET | `/special-needs` | Public | Has special needs |
| GET | `/statistics` | Public | Pet count statistics |
| GET | `/adoptable` | Public | Adoption-ready pets |

### Adoptions — `/api/adoptions`
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | Public | List adoptions (paginated, filterable) |
| GET | `/{id}` | Public | Get adoption by ID |
| GET | `/user/{userId}` | Public | User's adoptions |
| GET | `/status/{status}` | Public | Filter by status |
| GET | `/pet-type/{type}` | Public | Filter by pet type |
| POST | `/` | JWT | Create adoption application |
| PUT | `/{id}` | JWT | Update adoption |
| PUT | `/{id}/status` | JWT | Update adoption status |
| DELETE | `/{id}` | JWT | Delete adoption |
| GET | `/stats/total` | Public | Total adoption count |

### Donations — `/api/donations`
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | Public | List donations (paginated) |
| GET | `/{id}` | Public | Get donation by ID |
| GET | `/total` | Public | Total donations amount |
| GET | `/count` | Public | Total donations count |
| POST | `/initialize` | JWT | Initialize Paystack payment |
| POST | `/webhook/paystack` | Public | Paystack webhook |
| GET | `/user` | JWT | Current user's donations |
| POST | `/pet/{petId}` | JWT | Donate to specific pet |

### Blogs — `/api/blogs`
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/` | Public | List all blogs |
| GET | `/{id}` | Public | Get blog by ID |
| POST | `/` | JWT | Create blog (multipart) |
| PUT | `/{id}` | JWT | Update blog |
| DELETE | `/{id}` | JWT | Delete blog |

### Other
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/contact` | Public | Send contact message |
| POST | `/api/upload/image` | JWT | Upload image to Cloudinary |
| GET | `/api/test/health` | Public | Health check |
| GET | `/api/test/ping` | Public | Ping |
| GET | `/api/notifications` | JWT | User notifications |
| POST | `/api/notifications/send` | JWT | Send notification |
| POST | `/api/notifications/mark-read` | JWT | Mark as read |
| GET | `/api/notifications/unread/count` | JWT | Unread count |

---

## Third-Party Integrations

### Paystack (Payments)
```
Flow:
1. Client  → POST /api/donations/initialize { donorName, email, amount }
2. API     → POST https://api.paystack.co/transaction/initialize
3. Paystack→ { authorizationUrl, reference, accessCode }
4. Client  → Redirected to Paystack payment page
5. Paystack→ POST /api/paystack/webhook { event: "charge.success", data: { reference } }
6. API     → HMAC-SHA512 signature verification
7. API     → Update Donation.paymentStatus = COMPLETED
8. API     → Send donation confirmation email via Brevo

Env vars: PAYSTACK_SECRET_KEY, PAYSTACK_PUBLIC_KEY
```

### Cloudinary (File Storage)
```
Flow:
1. Client  → POST /api/upload/image (multipart/form-data)
2. API     → CloudinaryService.uploadFile()
3. Cloudinary → Stores file, returns secure URL
4. API     → Returns { imageUrl, publicId }

Folders: pet-images, blog-images, general-files
Env vars: CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET
```

### Brevo (Email)
```
Flow:
1. Service → AsyncEmailService.send()  (or EmailService.send())
2. API     → POST https://api.brevo.com/v3/smtp/email
             Header: api-key: ${BREVO_API_KEY}
             Body: { sender, to, subject, htmlContent }
3. Brevo   → Delivers email to recipient

Email types:
- Email verification OTP
- Password reset OTP
- Welcome email (after verified)
- Donation confirmation
- Adoption status updates
- Admin notifications
- Contact form forwarding

Env vars: BREVO_API_KEY, BREVO_FROM_EMAIL
Template engine: Thymeleaf (src/main/resources/templates/)
```

### WebSocket (Real-time Notifications)
```
Endpoint: ws://host/ws

Flow:
1. Client connects → NotificationWebSocketHandler.afterConnectionEstablished()
2. Session stored in ConcurrentHashMap<sessionId, WebSocketSession>
3. Server push → NotificationWebSocketHandler.sendToUser(email, message)
4. Broadcast  → NotificationWebSocketHandler.broadcastToAll(message)
5. PING/PONG  → Keep-alive handling
6. Disconnect → Session removed from map
```

---

## Email Flow (Registration)

```
POST /api/auth/register
  → Validate email/username uniqueness
  → Hash password (BCrypt)
  → Generate 6-digit OTP
  → Save User { isVerified: false }
  → EmailUtil.sendVerificationEmail()
      → AsyncEmailService.sendVerificationEmailAsync()   [@Async]
          → Thymeleaf renders email/EmailTemplate.html
          → POST https://api.brevo.com/v3/smtp/email
  → Return { message: "Registration successful. Check email." }

POST /api/auth/verify-otp { email, code, otpType: EMAIL_VERIFICATION }
  → Find user by email
  → Validate code + expiry (15 min) + otpType
  → Set user.isVerified = true, clear OTP fields
  → Send welcome email (async)
  → Return { message: "OTP verified successfully." }
```

---

## Deployment

### Docker
```dockerfile
Build stage:  maven:3.9.6-eclipse-temurin-17
Runtime stage: eclipse-temurin:17-jre-alpine

JVM flags:
  -XX:+UseContainerSupport       # Respect container memory limit
  -XX:MaxRAMPercentage=50.0      # Heap = 50% of 1GB = 512MB
  -XX:InitialRAMPercentage=15.0  # Start small, grow as needed
  -XX:MaxMetaspaceSize=200m      # Cap class metadata memory
  -Xss512k                       # Thread stack size
```

### Railway Configuration (`railway.json`)
```json
{
  "healthcheckPath": "/api/test/health",
  "healthcheckTimeout": 300,
  "restartPolicyType": "ON_FAILURE",
  "restartPolicyMaxRetries": 3
}
```

### Environment Variables
| Variable | Purpose |
|----------|---------|
| `DB_URL` | PostgreSQL JDBC connection URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET_KEY` | HS256 signing key |
| `PAYSTACK_SECRET_KEY` | Paystack API secret |
| `PAYSTACK_PUBLIC_KEY` | Paystack public key |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary account name |
| `CLOUDINARY_API_KEY` | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret |
| `BREVO_API_KEY` | Brevo transactional email API key |
| `BREVO_FROM_EMAIL` | Verified sender email address |
| `MAIL_USERNAME` | Gmail address (admin notifications) |
| `MAIL_PASSWORD` | Gmail app password |

---

## Performance Configuration

| Setting | Value | Reason |
|---------|-------|--------|
| HikariCP max pool | 5 | Low connection count for small container |
| HikariCP min idle | 2 | Keep 2 warm connections ready |
| Tomcat max threads | 20 | Enough for expected load, saves memory |
| Tomcat min-spare | 4 | Pre-warmed threads |
| Heap | 50% of RAM | Leaves room for metaspace + overhead |
| Metaspace cap | 200MB | Prevents unbounded class loading growth |
| open-in-view | false | Closes DB connections after service layer |
| Hibernate batch | 20 | Fewer round-trips for bulk inserts |
| Cache | Simple | users, adoptions, donations cached in-memory |

---

## Key Design Decisions

- **Stateless auth** — No server-side sessions; JWT carries identity on every request
- **Async email** — `@Async` on all email sends so HTTP requests return immediately; email failures are logged but don't fail the request
- **Brevo over SMTP** — Railway blocks outbound SMTP ports; Brevo uses HTTPS so emails work in production
- **Paystack webhook verification** — HMAC-SHA512 signature checked on every webhook to prevent spoofed payment confirmations
- **Lazy DB connections** — `spring.jpa.open-in-view=false` ensures connections are held only during the service/repository layer, not for the full HTTP lifecycle
- **Cloudinary for files** — Offloads storage entirely; Railway's filesystem is ephemeral (lost on redeploy)
- **WebSocket for notifications** — In-memory session map; suitable for single-instance deployment on Railway
