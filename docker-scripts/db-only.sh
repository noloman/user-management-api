#!/bin/bash

echo "🗄️  Starting PostgreSQL Database only..."

# Start only the database service
docker-compose up -d postgres

echo "✅ PostgreSQL is starting..."
echo ""

# Wait for database to be ready
echo "⏳ Waiting for database to be ready..."
sleep 5

# Check if database is healthy
echo "🔍 Checking database health..."
if docker-compose ps postgres | grep -q "Up (healthy)"; then
    echo "✅ PostgreSQL is healthy and ready!"
else
    echo "⚠️  PostgreSQL is still starting up..."
    echo "📝 Check logs with: docker-compose logs -f postgres"
fi

echo ""
echo "🗄️  Database connection details:"
echo "  - Host: localhost:5432"
echo "  - Database: usermanagement_dev"
echo "  - Username: userapp"
echo "  - Password: userapp123"

echo ""
echo "🚀 You can now run your Spring Boot application locally with:"
echo "   mvn spring-boot:run"

echo ""
echo "🛑 To stop the database:"
echo "   docker-compose stop postgres"