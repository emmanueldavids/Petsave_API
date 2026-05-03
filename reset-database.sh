#!/bin/bash

# PetSave API Database Reset Script
# Usage: ./reset-database.sh
# Preserves: khoswift@gmail.com (main user) and admin@petsave.com (admin)

echo "🗄️  PetSave API Database Reset"
echo "================================"

# Check if API is running
echo "🔍 Checking API connection..."
if ! curl -s "http://localhost:8080/api/test/health" > /dev/null 2>&1; then
    echo "❌ Error: PetSave API is not running on http://localhost:8080"
    echo "Please start your Spring Boot application first"
    exit 1
fi

echo "✅ API is running"

# Get admin token for authentication
echo ""
echo "🔐 Getting admin authentication token..."
ADMIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin@petsave.com","password":"admin123"}')

if echo "$ADMIN_RESPONSE" | grep -q "error\|Error\|401"; then
    echo "❌ Error: Could not authenticate as admin"
    echo "Please ensure admin@petsave.com exists with password 'admin123'"
    echo "Response: $ADMIN_RESPONSE"
    exit 1
fi

TOKEN=$(echo "$ADMIN_RESPONSE" | jq -r '.token // .access_token' 2>/dev/null)
if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
    echo "❌ Error: Could not extract token from response"
    echo "Response: $ADMIN_RESPONSE"
    exit 1
fi

echo "✅ Admin authentication successful"

# Get current database statistics
echo ""
echo "📋 Current Database Status:"
STATS_RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/admin/database/stats)

if echo "$STATS_RESPONSE" | grep -q "success.*true"; then
    echo "📊 Current statistics:"
    echo "$STATS_RESPONSE" | jq -r '.stats | to_entries[] | "  \(.key): \(.value)"' 2>/dev/null || echo "  Could not parse statistics"
else
    echo "⚠️  Could not get current statistics"
fi

# Confirm before proceeding
echo ""
echo "⚠️  WARNING: This will delete ALL data except:"
echo "   📧 khoswift@gmail.com (main user)"
echo "   👤 admin@petsave.com (admin account)"
echo ""
read -p "Are you sure you want to continue? (type 'yes' to confirm): " confirm

if [ "$confirm" != "yes" ]; then
    echo "❌ Database reset cancelled"
    exit 1
fi

# Perform database cleanup
echo ""
echo "🧹 Performing database cleanup..."
CLEANUP_RESPONSE=$(curl -s -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  http://localhost:8080/api/admin/database/cleanup)

if echo "$CLEANUP_RESPONSE" | grep -q "success.*true"; then
    echo "✅ Database cleanup completed successfully!"
    echo ""
    echo "📊 Cleanup Summary:"
    
    # Display before/after statistics
    echo "$CLEANUP_RESPONSE" | jq -r '
        if .before and .after then
            "  Before cleanup:"
            | .before | to_entries[] | "    \(.key): \(.value)"
            | "  After cleanup:"
            | .after | to_entries[] | "    \(.key): \(.value)"
        else
            "  Statistics not available"
        end
    ' 2>/dev/null || echo "  Could not parse cleanup statistics"
    
    echo ""
    echo "� Preserved Accounts:"
    echo "  📧 khoswift@gmail.com (main user)"
    echo "  👤 admin@petsave.com (admin account)"
    
else
    echo "❌ Error: Database cleanup failed"
    echo "Response: $CLEANUP_RESPONSE"
    exit 1
fi

echo ""
echo "🎉 Database reset complete!"
echo ""
echo "� What was preserved:"
echo "   ✅ Admin login credentials"
echo "   ✅ User login credentials"
echo "   ✅ Account verification status"
echo ""
echo "📝 What was deleted:"
echo "   🗑️  All pets and their data"
echo "   🗑️  All adoption applications"
echo "   🗑️  All donation records"
echo "   🗑️  All other user accounts"
echo ""
echo "🚀 Your PetSave API is now ready for fresh testing!"
