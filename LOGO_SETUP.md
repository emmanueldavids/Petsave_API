# PetSave Logo Setup Guide

## 🖼️ Add Your Logo to Emails

### 1. Choose Your Logo
- PNG format recommended
- 120px width, transparent background
- File size under 50KB

### 2. Place Your Logo
Copy your logo to:
```
src/main/resources/static/images/logo.png
```

### 3. Supported Locations (in order of priority):
- `static/images/logo.png` ⭐ RECOMMENDED
- `images/logo.png`
- `logo.png`
- `static/images/petsave-logo.png`
- `images/petsave-logo.png`
- `petsave-logo.png`

### 4. Test It
1. Start the backend
2. Register a new user
3. Check your email for the logo!

### 5. Fallback
If no logo is found, the system will show:
🐾 PetSave (text logo with paw emoji)

## ✅ Done!
Your emails will now display your logo!
