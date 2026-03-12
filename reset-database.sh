#!/bin/bash

# PetSave API Database Reset Script
# Usage: ./reset-database.sh

echo "🗄️  PetSave API Database Reset"
echo "================================"

# Check if database URL is set
if [ -z "$DB_URL" ]; then
    echo "❌ Error: DB_URL environment variable not set"
    echo "Please set your database connection string:"
    echo "export DB_URL=jdbc:postgresql://localhost:5432/petsaveDB"
    exit 1
fi

# Extract connection details from DB_URL
DB_HOST=$(echo $DB_URL | sed -n 's/.*:\/\/\([^:]*\):.*/\1/p')
DB_PORT=$(echo $DB_URL | sed -n 's/.*:\([0-9]*\)\/.*/\1/p')
DB_NAME=$(echo $DB_URL | sed -n 's/.*\/\([^?]*\).*/\1/p')

echo "📊 Database: $DB_NAME"
echo "🌐 Host: $DB_HOST:$DB_PORT"

# Check if we can connect
if ! psql -h localhost -p 5432 -U postgres -d petsaveDB -c "SELECT 1;" > /dev/null 2>&1; then
    echo "❌ Error: Cannot connect to database"
    echo "Please check your connection and credentials"
    exit 1
fi

echo "✅ Connected to database"

# Get current table counts
echo ""
echo "📋 Current Database Status:"
psql -h localhost -p 5432 -U postgres -d petsaveDB -c "
SELECT 
    table_name as \"Table\", 
    (SELECT COUNT(*) FROM information_schema.columns WHERE table_name = t.table_name) as \"Columns\"
FROM information_schema.tables t 
WHERE table_schema = 'public' 
ORDER BY table_name;" 2>/dev/null || echo "No tables found"

# Clear data if tables exist
echo ""
echo "🧹 Clearing existing data..."

# Check if tables exist and clear them
TABLES=("users" "adoptions" "donations")

for table in "${TABLES[@]}"; do
    if psql -h localhost -p 5432 -U postgres -d petsaveDB -c "\dt $table" 2>/dev/null | grep -q "$table"; then
        echo "  🗑️  Clearing table: $table"
        psql -h localhost -p 5432 -U postgres -d petsaveDB -c "DELETE FROM $table;" 2>/dev/null
        
        # Reset sequence if it exists
        seq_name="${table}_id_seq"
        if psql -h localhost -p 5432 -U postgres -d petsaveDB -c "\d $seq_name" 2>/dev/null | grep -q "Sequence"; then
            psql -h localhost -p 5432 -U postgres -d petsaveDB -c "ALTER SEQUENCE $seq_name RESTART WITH 1;" 2>/dev/null
            echo "  🔄 Reset sequence: $seq_name"
        fi
    else
        echo "  ⚠️  Table $table does not exist"
    fi
done

echo ""
echo "✅ Database reset complete!"
echo ""
echo "🚀 Your PetSave API is now ready for a fresh start!"
echo ""
echo "📝 Next steps:"
echo "   1. Start your Spring Boot application"
echo "   2. Register a new admin user"
echo "   3. Test your API endpoints"
