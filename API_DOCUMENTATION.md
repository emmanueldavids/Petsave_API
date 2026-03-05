# PetSave API - Complete Frontend Integration Guide

## 🚀 **API BASE URL**
```
Base URL: http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui/index.html
Health Check: http://localhost:8080/actuator/health
```

---

## 🔐 **AUTHENTICATION ENDPOINTS**

### **1. Register New User**
```http
POST /api/auth/register
Content-Type: application/json

Request Body:
{
  "name": "John Doe",
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePassword123"
}

Response:
{
  "message": "Registration successful. Check email for verification code.",
  "status": "success"
}
```

### **2. Verify Email OTP**
```http
POST /api/auth/verify-otp
Content-Type: application/json

Request Body:
{
  "email": "john@example.com",
  "code": "123456",
  "otpType": "EMAIL_VERIFICATION"
}

Response:
{
  "message": "OTP verified successfully.",
  "status": "success"
}
```

### **3. Login**
```http
POST /api/auth/login
Content-Type: application/json

Request Body:
{
  "username": "johndoe",
  "password": "securePassword123"
}

Response:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
}
```

### **4. Refresh Token**
```http
POST /api/auth/refresh
Content-Type: application/json
Authorization: Bearer <refresh_token>

Request Body:
{
  "email": "john@example.com",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response:
{
  "accessToken": "new_access_token_here",
  "refreshToken": "new_refresh_token_here"
}
```

### **5. Resend OTP**
```http
POST /api/auth/resend
Content-Type: application/json

Request Body:
{
  "email": "john@example.com"
}

Response:
{
  "message": "New verification code sent.",
  "status": "success"
}
```

### **6. Password Reset Request**
```http
POST /api/auth/reset-password/request
Content-Type: application/json

Request Body:
{
  "email": "john@example.com"
}

Response:
{
  "message": "Reset OTP sent to email.",
  "status": "success"
}
```

### **7. Password Reset Confirmation**
```http
POST /api/auth/reset-password/confirm
Content-Type: application/json

Request Body:
{
  "email": "john@example.com",
  "code": "123456",
  "newPassword": "newSecurePassword456"
}

Response:
{
  "message": "Password reset successful.",
  "status": "success"
}
```

---

## 🐾 **ADOPTION ENDPOINTS**

### **1. Create Adoption Application**
```http
POST /api/adoptions
Content-Type: application/json
Authorization: Bearer <access_token>

Request Body:
{
  "petName": "Buddy",
  "petBreed": "Golden Retriever",
  "petAge": 2,
  "petDescription": "Friendly and energetic dog looking for a loving home",
  "petImageUrl": "https://example.com/buddy.jpg",
  "petType": "DOG",
  "adopterName": "John Doe",
  "adopterEmail": "john@example.com",
  "adopterPhone": "+1234567890",
  "adopterAddress": "123 Main St, City, State",
  "applicationReason": "I love dogs and have experience with Golden Retrievers"
}

Response:
{
  "id": 1,
  "petName": "Buddy",
  "status": "PENDING",
  "applicationDate": "2026-03-05T14:18:34.057975",
  "user": {
    "id": 1,
    "name": "Admin",
    "email": "admin@example.com"
  }
}
```

### **2. Get All Adoptions (Paginated)**
```http
GET /api/adoptions?page=0&size=10
Authorization: Bearer <access_token>

Response:
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 25,
    "totalPages": 3,
    "first": true,
    "last": false
  }
}
```

### **3. Get Adoption by ID**
```http
GET /api/adoptions/1
Authorization: Bearer <access_token>

Response:
{
  "id": 1,
  "petName": "Buddy",
  "petBreed": "Golden Retriever",
  "status": "PENDING"
}
```

### **4. Search Adoptions**
```http
GET /api/adoptions?petName=Buddy&status=PENDING&petType=DOG&page=0&size=10
Authorization: Bearer <access_token>

Query Parameters:
- `petName`: Filter by pet name (partial match)
- `status`: Filter by adoption status (PENDING, APPROVED, REJECTED, COMPLETED)
- `petType`: Filter by pet type (DOG, CAT, BIRD, RABBIT, OTHER)
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)

Response:
{
  "content": [...],
  "pageable": {...}
}
```

### **5. Get Adoptions by User**
```http
GET /api/adoptions/user/1
Authorization: Bearer <access_token>

Response:
[
  {
    "id": 1,
    "petName": "Buddy",
    "status": "PENDING"
  }
]
```

### **6. Get Adoptions by Status**
```http
GET /api/adoptions/status/PENDING?page=0&size=10
Authorization: Bearer <access_token>

Path Parameters:
- `status`: PENDING, APPROVED, REJECTED, COMPLETED

Response:
{
  "content": [...],
  "pageable": {...}
}
```

### **7. Get Adoptions by Pet Type**
```http
GET /api/adoptions/pet-type/DOG?page=0&size=10
Authorization: Bearer <access_token>

Path Parameters:
- `petType`: DOG, CAT, BIRD, RABBIT, OTHER

Response:
{
  "content": [...],
  "pageable": {...}
}
```

### **8. Update Adoption Status**
```http
PUT /api/adoptions/1/status?status=APPROVED
Authorization: Bearer <access_token>

Query Parameters:
- `status`: New status (PENDING, APPROVED, REJECTED, COMPLETED)

Response:
{
  "id": 1,
  "petName": "Buddy",
  "status": "APPROVED",
  "updatedAt": "2026-03-05T14:19:05.107165"
}
```

### **9. Update Adoption Details (PUT)**
```http
PUT /api/adoptions/1
Content-Type: application/json
Authorization: Bearer <access_token>

Request Body:
{
  "petName": "Buddy Updated",
  "petDescription": "Updated description",
  "adopterAddress": "456 New Address St"
}

Response:
{
  "id": 1,
  "petName": "Buddy Updated",
  "updatedAt": "2026-03-05T14:20:30.123456"
}
```

### **10. Patch Adoption (Partial Update)**
```http
PATCH /api/adoptions/1
Content-Type: application/json
Authorization: Bearer <access_token>

Request Body:
{
  "petName": "Buddy Updated",
  "petDescription": "Updated description only"
}

Response:
{
  "id": 1,
  "petName": "Buddy Updated",
  "petDescription": "Updated description only",
  "updatedAt": "2026-03-05T14:20:30.123456"
}
```

### **11. Delete Adoption**
```http
DELETE /api/adoptions/1
Authorization: Bearer <access_token>

Response:
204 No Content
```

---

## 💰 **DONATION ENDPOINTS**

### **1. Create Donation**
```http
POST /api/donations
Content-Type: application/json
Authorization: Bearer <access_token>

Request Body:
{
  "donorName": "Jane Smith",
  "amount": 50.00,
  "date": "2026-03-05T14:30:00",
  "gender": "FEMALE",
  "country": "United States",
  "email": "jane@example.com"
}

Response:
{
  "id": 1,
  "donorName": "Jane Smith",
  "amount": 50.00,
  "paymentStatus": "PENDING",
  "reference": "uuid-generated-reference"
}
```

### **2. Initialize Payment (Paystack)**
```http
POST /api/donations/initialize
Content-Type: application/json
Authorization: Bearer <access_token>

Request Body:
{
  "donorName": "Jane Smith",
  "amount": 50.00,
  "gender": "FEMALE",
  "country": "United States",
  "email": "jane@example.com"
}

Response:
{
  "authorization_url": "https://paystack.co/pay/uuid-goes-here"
}
```

### **3. Get All Donations (Paginated)**
```http
GET /api/donations?page=0&size=10
Authorization: Bearer <access_token>

Response:
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 100,
    "totalPages": 10,
    "first": true,
    "last": false
  }
}
```

### **4. Search Donations by Donor Name**
```http
GET /api/donations?donorName=Jane&page=0&size=10
Authorization: Bearer <access_token>

Response:
{
  "content": [...],
  "pageable": {...}
}
```

### **5. Get Donation by ID**
```http
GET /api/donations/1
Authorization: Bearer <access_token>

Response:
{
  "id": 1,
  "donorName": "Jane Smith",
  "amount": 50.00,
  "paymentStatus": "COMPLETED"
}
```

### **6. Get User's Donations**
```http
GET /api/donations/user/1
Authorization: Bearer <access_token>

Response:
[
  {
    "id": 1,
    "donorName": "Jane Smith",
    "amount": 50.00
  }
]
```

### **7. Update Donation**
```http
PUT /api/donations/1
Content-Type: application/json
Authorization: Bearer <access_token>

Request Body:
{
  "donorName": "Jane Smith Updated",
  "amount": 75.00
}

Response:
{
  "id": 1,
  "donorName": "Jane Smith Updated",
  "amount": 75.00
}
```

### **8. Delete Donation**
```http
DELETE /api/donations/1
Authorization: Bearer <access_token>

Response:
204 No Content
```

### **9. Get Total Donations**
```http
GET /api/donations/stats/total
Authorization: Bearer <access_token>

Response:
15000.50
```

### **10. Get Donation Count**
```http
GET /api/donations/stats/count
Authorization: Bearer <access_token>

Response:
150
```

---

## 📝 **BLOG ENDPOINTS**

### **1. Create Blog Post**
```http
POST /api/blogs
Content-Type: application/json
Authorization: Bearer <access_token>

Request Body:
{
  "title": "How to Adopt a Pet",
  "content": "Complete guide to pet adoption...",
  "author": "PetSave Team",
  "image": "base64-encoded-image-data"
}

Response:
{
  "id": 1,
  "title": "How to Adopt a Pet",
  "imageUrl": "https://example.com/blog-image.jpg"
}
```

### **2. Get All Blogs (Paginated)**
```http
GET /api/blogs?page=0&size=10
Authorization: Bearer <access_token>

Response:
{
  "content": [...],
  "pageable": {...}
}
```

### **3. Get Blog by ID**
```http
GET /api/blogs/1
Authorization: Bearer <access_token>

Response:
{
  "id": 1,
  "title": "How to Adopt a Pet",
  "content": "Complete guide to pet adoption...",
  "author": "PetSave Team"
}
```

---

## 📧 **CONTACT ENDPOINT**

### **Send Contact Message**
```http
POST /api/contact
Content-Type: application/json

Request Body:
{
  "name": "John Doe",
  "email": "john@example.com",
  "title": "General Inquiry",
  "message": "I would like to know more about your services."
}

Response:
{
  "message": "Contact message sent successfully",
  "status": "success"
}
```

---

## 🔍 **ADMIN ENDPOINTS**

### **1. Get All Users**
```http
GET /api/auth/users
Authorization: Bearer <admin_access_token>

Response:
[
  {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "username": "johndoe",
    "isVerified": true
  }
]
```

### **2. Get User by ID**
```http
GET /api/auth/users/1
Authorization: Bearer <admin_access_token>

Response:
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "username": "johndoe",
  "isVerified": true
}
```

---

## 📊 **STATISTICS ENDPOINTS**

### **Adoption Statistics**
```http
GET /api/adoptions/stats/total
Authorization: Bearer <access_token>

Response:
25

http://GET /api/adoptions/stats/status/PENDING
Response:
15

http://GET /api/adoptions/stats/pet-type/DOG
Response:
12
```

### **Donation Statistics**
```http
GET /api/donations/stats/total
Authorization: Bearer <access_token>

Response:
15000.50

http://GET /api/donations/stats/count
Response:
150
```

---

## 🏥‍♂️ **HEALTH & MONITORING ENDPOINTS**

### **Health Check**
```http
GET /actuator/health

Response:
{
  "status": "UP",
  "timestamp": "2026-03-05T14:42:37.996+00:00",
  "details": {
    "status": "API is running"
  }
}
```

### **Application Metrics**
```http
GET /actuator/metrics

Response:
{
  // Various JVM and application metrics
  "jvm.memory.used": 256000000,
  "http.server.requests.active": 5,
  "database.connections.active": 12
}
```

---

## 🔐 **AUTHENTICATION GUIDE**

### **JWT Token Usage**
1. **Include in all API calls** (except auth endpoints):
   ```http
   Authorization: Bearer <access_token>
   ```

2. **Token Refresh Flow**:
   - When access token expires, use refresh token to get new access token
   - Store refresh token securely on client side

3. **Token Expiration**:
   - Access Token: 15 minutes
   - Refresh Token: 7 days

---

## 📝 **ERROR HANDLING**

### **Standard Error Response Format**
```json
{
  "timestamp": "2026-03-05T14:30:00.000Z",
  "status": 400,
  "error": "Validation failed",
  "message": "Input validation failed",
  "errors": [
    "Pet name is required",
    "Email must be valid"
  ]
}
```

### **Common HTTP Status Codes**
- `200`: Success
- `201`: Created
- `400`: Bad Request (Validation errors)
- `401`: Unauthorized (Invalid/missing token)
- `403`: Forbidden (Insufficient permissions)
- `404`: Not Found
- `500`: Internal Server Error

---

## 🌐 **CORS CONFIGURATION**

### **Allowed Origins**
- `http://localhost:3000`
- `http://127.0.0.1:3000`

### **Credentials Support**
- `allowCredentials: true` enabled

---

## 📱 **FRONTEND INTEGRATION EXAMPLES**

### **React.js Example**
```javascript
// API Configuration
const API_BASE_URL = 'http://localhost:8080';

// Authentication Service
class AuthService {
  constructor() {
    this.baseURL = API_BASE_URL;
  }

  async login(credentials) {
    const response = await fetch(`${this.baseURL}/api/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(credentials)
    });
    
    const data = await response.json();
    if (response.ok) {
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      return data;
    } else {
      throw new Error('Login failed');
    }
  }

  async getAdoptions(page = 0, size = 10) {
    const token = localStorage.getItem('accessToken');
    const response = await fetch(`${this.baseURL}/api/adoptions?page=${page}&size=${size}`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    });
    
    return response.json();
  }

  async createAdoption(adoptionData) {
    const token = localStorage.getItem('accessToken');
    const response = await fetch(`${this.baseURL}/api/adoptions`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(adoptionData)
    });
    
    return response.json();
  }

  async patchAdoption(id, patchData) {
    const token = localStorage.getItem('accessToken');
    const response = await fetch(`${this.baseURL}/api/adoptions/${id}`, {
      method: 'PATCH',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(patchData)
    });
    
    return response.json();
  }
}

// Usage Example
const authService = new AuthService();

// Login
const loginData = await authService.login({
  username: 'johndoe',
  password: 'password123'
});

// Get adoptions
const adoptions = await authService.getAdoptions(0, 10);

// Create adoption
const newAdoption = await authService.createAdoption({
  petName: 'Buddy',
  petBreed: 'Golden Retriever',
  petAge: 2,
  petType: 'DOG',
  adopterName: 'John Doe',
  adopterEmail: 'john@example.com',
  adopterPhone: '+1234567890',
  adopterAddress: '123 Main St',
  applicationReason: 'I love dogs!'
});
```

### **Vue.js Example**
```javascript
// API Configuration
const API_BASE_URL = 'http://localhost:8080';

// Authentication Service
class AuthService {
  constructor() {
    this.baseURL = API_BASE_URL;
  }

  async login(credentials) {
    try {
      const response = await axios.post(`${this.baseURL}/api/auth/login`, credentials);
      const { accessToken, refreshToken } = response.data;
      
      // Store tokens
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Login failed');
    }
  }

  async getAdoptions(params = {}) {
    const token = localStorage.getItem('accessToken');
    const response = await axios.get(`${this.baseURL}/api/adoptions`, {
      params,
      headers: {
        'Authorization': `Bearer ${token}`,
      }
    });
    
    return response.data;
  }

  async createAdoption(adoptionData) {
    const token = localStorage.getItem('accessToken');
    const response = await axios.post(`${this.baseURL}/api/adoptions`, adotionData, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      }
    });
    
    return response.data;
  }
}

// Usage in Vue Component
export default {
  data() {
    return {
      adoptions: [],
      loading: false,
      error: null
    };
  },
  
  async mounted() {
    await this.loadAdoptions();
  },
  
  methods: {
    async loadAdoptions() {
      this.loading = true;
      try {
        this.adoptions = await this.authService.getAdoptions();
      } catch (error) {
        this.error = error.message;
      } finally {
        this.loading = false;
      }
    },
    
    async createAdoption(adoptionData) {
      try {
        const newAdoption = await this.authService.createAdoption(adoptionData);
        this.adoptions.unshift(newAdoption);
      } catch (error) {
        this.error = error.message;
      }
    }
  }
}
```

---

## 🚀 **DEPLOYMENT INSTRUCTIONS**

### **Environment Variables**
```bash
# Development
export DB_URL=jdbc:postgresql://localhost:5432/petsaveDB
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET_KEY=your-super-secret-jwt-key-here
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password

# Production
export DB_URL=jdbc:postgresql://your-production-db:5432/petsaveDB
export DB_USERNAME=postgres
export DB_PASSWORD=production-password
export JWT_SECRET_KEY=production-jwt-secret-key
```

### **Running the Application**
```bash
# Development
./mvnw spring-boot:run

# Production
java -jar -Xmx2g -Xms512m petsave-0.0.1-SNAPSHOT.jar
```

### **Docker Deployment**
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/petsave-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# Build and Run
docker build -t petsave-api .
docker run -p 8080:8080 -e DB_URL=postgresql://host.docker.internal:5432/petsaveDB petsave-api
```

---

## 📚 **TESTING GUIDE**

### **Postman Collection**
Import the provided Postman collection or create requests using the examples above.

### **Automated Testing**
```bash
# Health Check
curl -f http://localhost:8080/actuator/health

# API Test
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"test"}'

# Create Adoption
curl -X POST http://localhost:8080/api/adoptions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"petName":"Test","petType":"DOG","adopterName":"Test User","adopterEmail":"test@example.com"}'
```

---

## 🎯 **RECOMMENDATIONS**

### **For Production**
1. **Load Balancer**: Use Nginx or AWS ALB
2. **CDN**: CloudFlare for static assets
3. **Monitoring**: Prometheus + Grafana
4. **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
5. **Database**: Read replicas for high availability
6. **Caching**: Redis for distributed caching
7. **Security**: HTTPS with valid SSL certificates

### **For Development**
1. **Hot Reload**: Spring Boot DevTools enabled
2. **Database**: H2 for quick testing
3. **Profiling**: Spring Boot Actuator
4. **Documentation**: Swagger UI available

---

This guide provides everything needed to integrate your PetSave API with any frontend framework! 🚀
