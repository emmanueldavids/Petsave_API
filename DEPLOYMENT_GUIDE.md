# 🚀 PetSave Deployment Guide

## 📋 Platform Setup: Railway + Vercel

---

## 🔧 **RAILWAY (Backend) Environment Variables**

### **Required Variables:**
```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://username:password@host:port/database

# JWT Configuration
JWT_SECRET=your-super-secure-jwt-secret-key-here

# Cloudinary Configuration
CLOUDINARY_CLOUD_NAME=your-cloudinary-cloud-name
CLOUDINARY_API_KEY=your-cloudinary-api-key
CLOUDINARY_API_SECRET=your-cloudinary-api-secret

# Paystack Configuration
PAYSTACK_SECRET_KEY=sk_test_xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
PAYSTACK_PUBLIC_KEY=pk_test_xxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# CORS Configuration
CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app
```

### **Setup Steps:**
1. Go to [railway.app](https://railway.app)
2. Click "New Project" → "Deploy from GitHub repo"
3. Select your `Petsave_API` repository
4. Add environment variables in Railway dashboard
5. Deploy!

---

## 🎨 **VERCEL (Frontend) Environment Variables**

### **Required Variables:**
```bash
# API Configuration
VITE_API_URL=https://your-backend.railway.app

# Optional: Custom domain
VITE_CUSTOM_DOMAIN=your-domain.com
```

### **Setup Steps:**
1. Go to [vercel.com](https://vercel.com)
2. Click "New Project" → "Import Git Repository"
3. Select your `Petsave_Frontend` repository
4. Add environment variables in Vercel dashboard
5. Deploy!

---

## 🔄 **Deployment Workflow**

### **Automatic Deployments:**
```bash
# Backend (Railway)
git push origin main
# → Railway automatically builds and deploys

# Frontend (Vercel)  
git push origin main
# → Vercel automatically builds and deploys
```

### **Manual Deployments:**
```bash
# Backend
./mvnw clean package -DskipTests
# Upload JAR to Railway

# Frontend
npm run build
# Upload dist/ to Vercel
```

---

## 🔗 **Connecting Frontend to Backend**

### **Update API Configuration:**
```typescript
// src/lib/api.ts
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';
```

### **CORS Configuration:**
```java
// Backend: src/main/java/.../Config/SecurityConfig.java
corsConfiguration.setAllowedOrigins(List.of(
    "http://localhost:3000",
    "https://your-frontend.vercel.app"
));
```

---

## 🧪 **Testing Your Deployment**

### **Health Check:**
```bash
# Backend health
curl https://your-backend.railway.app/api/test/health

# Frontend access
# Visit https://your-frontend.vercel.app
```

### **Common Issues:**
- **CORS errors** → Update allowed origins in backend
- **Database connection** → Check DATABASE_URL format
- **Environment variables** → Ensure all required vars are set
- **Build failures** → Check logs in Railway/Vercel dashboards

---

## 📊 **Monitoring**

### **Railway:**
- Logs: Railway dashboard → Logs tab
- Metrics: Railway dashboard → Metrics tab
- Environment: Variables tab

### **Vercel:**
- Logs: Vercel dashboard → Functions tab
- Analytics: Vercel dashboard → Analytics tab
- Environment: Settings → Environment Variables

---

## 🎯 **Production Checklist**

### **Before Going Live:**
- [ ] All environment variables set
- [ ] HTTPS URLs working
- [ ] Database connected
- [ ] CORS configured correctly
- [ ] Health check endpoint accessible
- [ ] Frontend can call backend APIs
- [ ] Cloudinary configuration working
- [ ] Paystack integration tested

### **After Deployment:**
- [ ] Test all CRUD operations
- [ ] Test file uploads (blog images)
- [ ] Test authentication flow
- [ ] Test payment integration
- [ ] Monitor error logs
- [ ] Set up custom domains (optional)

---

## 🆘 **Troubleshooting**

### **Railway Issues:**
```bash
# Common fixes:
- Check build logs for compilation errors
- Verify DATABASE_URL format
- Ensure port 8080 is exposed
- Check Java version compatibility
```

### **Vercel Issues:**
```bash
# Common fixes:
- Check build logs for React errors
- Verify VITE_API_URL is set
- Ensure build outputs to /dist
- Check for missing dependencies
```

---

## 🎉 **Success!**

Your PetSave application is now live with:
- ✅ Scalable backend on Railway
- ✅ Fast frontend on Vercel  
- ✅ Automatic deployments from Git
- ✅ Free tier for testing
- ✅ Ready for production traffic!
