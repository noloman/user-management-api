#!/bin/bash

echo "🚀 Starting User Management Application with Docker..."
echo "   Features: JWT Authentication, Refresh Tokens, Profile Management, RBAC"
echo ""

# Create necessary directories
mkdir -p logs

# Always REBUILD the app image for latest code
# This avoids stale code in development!
echo "📦 Building and starting PostgreSQL database and Spring Boot application..."
docker-compose up --build -d

echo ""
echo "✅ Services are starting..."
echo ""
echo "📊 Service Status:"
docker-compose ps

echo ""
echo "📝 To view logs:"
echo "  - All services: docker-compose logs -f"
echo "  - Database only: docker-compose logs -f postgres"
echo "  - App only: docker-compose logs -f app"

echo ""
echo "🌐 Access Points (Docker Setup - Primary):"
echo "  - 🏠 Application: http://localhost:8082"
echo "  - 📚 Swagger UI: http://localhost:8082/swagger-ui.html"
echo "  - 📋 API Docs: http://localhost:8082/v3/api-docs"
echo "  - ❤️  Health Check: http://localhost:8082/actuator/health"

echo ""
echo "🗄️  Database Connection:"
echo "  - Host: localhost:5432 (from host machine)"
echo "  - Database: usermanagement_dev"
echo "  - Username: userapp"
echo "  - Password: userapp123"
echo "  - Connect: docker-compose exec postgres psql -U userapp -d usermanagement_dev"

echo ""
echo "🔧 Helpful Commands:"
echo "  - View app logs: docker-compose logs -f app"
echo "  - Restart app: docker-compose restart app"
echo "  - Rebuild app: docker-compose up --build -d app"
echo "  - Reset database: docker-compose down -v && docker-compose up --build -d"

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check if services are healthy
echo ""
echo "🔍 Checking service health..."
if docker-compose ps | grep -q "Up"; then
    echo "✅ Services are running!"
    echo ""
    echo "🎯 Quick Start:"
    echo "  1. Open: http://localhost:8082/swagger-ui.html"
    echo "  2. Register first user (gets ADMIN role automatically)"
    echo "  3. Login to get access + refresh tokens"
    echo "  4. Use 'Authorize' button with: Bearer <your-access-token>"
    echo "  5. Try endpoints: auth, profile, admin operations"
else
    echo "⚠️  Services may still be starting up."
    echo "📝 Check logs with: docker-compose logs -f"
fi

echo ""
echo "🛑 To stop everything: ./docker-scripts/stop.sh"