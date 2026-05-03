# Email Setup Guide for PetSave Donation System

## 🚨 Issue: Donation Emails Not Being Sent

Your donation emails are not being sent because the email environment variables are not configured.

## ✅ Solution: Configure Email Settings

### Step 1: Create Gmail App Password

1. **Enable 2-Factor Authentication** on your Gmail account
2. **Go to Google Account Settings** → Security
3. **Select "App Passwords"** (you may need to sign in again)
4. **Generate a new app password**:
   - Select "Mail" for the app
   - Select "Other (custom name)" and enter "PetSave API"
   - Click "Generate"
   - **Copy the 16-character password** (you won't see it again)

### Step 2: Set Environment Variables

Create a `.env` file in the Petsave_API directory:

```bash
# Email Configuration
MAIL_USERNAME=your-gmail-address@gmail.com
MAIL_PASSWORD=your-16-character-app-password

# Database Configuration (already set)
DB_URL=jdbc:postgresql://localhost:5432/petsaveDB
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Paystack Configuration (already set)
PAYSTACK_SECRET_KEY=your-paystack-secret-key
PAYSTACK_PUBLIC_KEY=your-paystack-public-key

# JWT Configuration (already set)
JWT_SECRET_KEY=mySuperSecretKey123456789012345678901234567890
```

### Step 3: Restart Backend Server

```bash
# Stop the current server (Ctrl+C)
# Then restart with environment variables
cd /Users/emmanueldavids/Documents/pp/Petsave_API
source .env
DB_URL="jdbc:postgresql://localhost:5432/petsaveDB" DB_USERNAME="postgres" DB_PASSWORD="postgres" MAIL_USERNAME="your-gmail@gmail.com" MAIL_PASSWORD="your-app-password" ./mvnw spring-boot:run
```

## 📧 Email Features After Setup

Once configured, the system will automatically send:

### ✅ Donor Confirmation Emails
- **Trigger**: After successful Paystack payment
- **Content**: Payment confirmation with amount, reference, and thank you message
- **Recipient**: Donor's email address

### ✅ Admin Notification Emails  
- **Trigger**: After successful Paystack payment
- **Content**: Donation details for admin records
- **Recipient**: admin@petsave.com

### ✅ Beautiful HTML Email Templates
- **Professional Design**: PetSave branding and colors
- **Complete Details**: Donation amount, transaction ID, date
- **Impact Statement**: How donation helps animals
- **Call-to-Action**: Link to view impact

## 🔧 Troubleshooting

### If Emails Still Don't Work:

1. **Check Gmail Settings**:
   - Ensure 2FA is enabled
   - Verify app password is correct
   - Check if Gmail is blocking less secure apps

2. **Check Backend Logs**:
   ```bash
   # Look for email-related errors
   tail -f logs/application.log | grep -i email
   ```

3. **Test Webhook Manually**:
   ```bash
   curl -X POST http://localhost:8080/api/donations/webhook/paystack \
   -H "Content-Type: application/json" \
   -d '{"event":"charge.success","data":{"reference":"test123","amount":5000,"customer":{"email":"test@example.com"},"paid_at":"2025-01-01T12:00:00"}}'
   ```

4. **Verify Environment Variables**:
   ```bash
   echo $MAIL_USERNAME
   echo $MAIL_PASSWORD
   ```

## 🎯 Alternative: Use Email Service Provider

If Gmail doesn't work, consider using:

- **SendGrid** (free tier available)
- **Mailgun** (free tier available)  
- **AWS SES** (pay-as-you-go)

## 🚀 Quick Test After Setup

1. Make a test donation through the frontend
2. Check your email inbox (and spam folder)
3. Verify both confirmation and receipt emails arrive

## 📞 Support

If you need help setting up the email configuration:

1. **Check this guide** for step-by-step instructions
2. **Verify your Gmail settings** are correct
3. **Test the webhook** to ensure it's working
4. **Check backend logs** for any error messages

---

**🎉 Once configured, your donation emails will work automatically!**
