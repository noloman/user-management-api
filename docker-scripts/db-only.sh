#!/bin/bash

echo "🗄️  Starting PostgreSQL Database Only (for Local Development)..."
echo "   Use this when you want to run Spring Boot locally with hot reload"
echo "   but keep the database in Docker (recommended for development)"
echo ""

# Start only the database service
echo "📦 Starting PostgreSQL container..."
docker-compose up -d postgres

echo ""
echo "✅ PostgreSQL is starting..."

# Wait for database to be ready
echo ""
echo "⏳ Waiting for database to be ready..."
sleep 8

# Check if database is healthy
echo "🔍 Checking database health..."
if docker-compose ps postgres | grep -q "Up (healthy)"; then
    echo "✅ PostgreSQL is healthy and ready!"
else
    echo "⚠️  PostgreSQL is still starting up..."
    echo "📝 Check logs with: docker-compose logs -f postgres"
fi

echo ""
echo "🗄️  Database Connection Details:"
echo "  - Host: localhost:5432"
echo "  - Database: usermanagement_dev"
echo "  - Username: userapp"
echo "  - Password: userapp123"
echo "  - JDBC URL: jdbc:postgresql://localhost:5432/usermanagement_dev"

echo ""
echo "🔧 Database Commands:"
echo "  - Connect via psql: docker-compose exec postgres psql -U userapp -d usermanagement_dev"
echo "  - View database logs: docker-compose logs -f postgres"
echo "  - Reset data: docker-compose down -v && docker-compose up -d postgres"

echo ""
echo "🚀 Now run your Spring Boot application locally:"
echo "   mvn spring-boot:run"
echo ""
echo "   This will use application.yml (port 8081) and connect to Docker PostgreSQL"
echo "   Access at: http://localhost:8081/swagger-ui.html"

echo ""
echo "💡 Hot Reload Benefits:"
echo "  - Fast application restarts on code changes"
echo "  - Live reload of static resources with Spring Boot DevTools"
echo "  - Database persists between app restarts"
echo "  - No need to rebuild Docker images during development"

echo ""
echo "🛑 To stop database: docker-compose stop postgres"
echo "🛑 To stop and remove data: docker-compose down -v"