#!/bin/bash

echo "🚀 Starting User Management Application with Docker..."

# Create necessary directories
mkdir -p logs

# Start the services
docker-compose up -d

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
echo "🌐 Access points:"
echo "  - Application: http://localhost:8081"
echo "  - Swagger UI: http://localhost:8081/swagger-ui.html"
echo "  - API Docs: http://localhost:8081/v3/api-docs"
echo "  - Health Check: http://localhost:8081/actuator/health"

echo ""
echo "🗄️  Database:"
echo "  - Host: localhost:5432"
echo "  - Database: usermanagement_dev"
echo "  - Username: userapp"
echo "  - Password: userapp123"

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 10

# Check if services are healthy
echo "🔍 Checking service health..."
if docker-compose ps | grep -q "Up (healthy)"; then
    echo "✅ Services are healthy and ready!"
else
    echo "⚠️  Services are still starting up. Check logs with: docker-compose logs -f"
fi