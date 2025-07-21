#!/bin/bash

echo "🛑 Stopping User Management Application..."

# Stop and remove containers
docker-compose down

echo "✅ Services stopped successfully!"

echo ""
echo "💾 Data volumes are preserved. To remove them completely, run:"
echo "   docker-compose down -v"

echo ""
echo "🧹 To clean up everything (containers, volumes, images), run:"
echo "   docker-compose down -v --rmi all"