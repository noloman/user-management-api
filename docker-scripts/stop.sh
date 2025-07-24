#!/bin/bash

echo "🛑 Stopping User Management Application..."
echo ""

# Show current status before stopping
echo "📊 Current Status:"
docker-compose ps

echo ""
echo "⏹️  Stopping all services (preserving data)..."

# Stop and remove containers
docker-compose down

echo ""
echo "✅ All services stopped successfully!"

echo ""
echo "💾 Database data is preserved in Docker volumes."

echo ""
echo "🔧 Additional Options:"
echo ""
echo "  🗑️  Remove data volumes (CAUTION - deletes all database data):"
echo "     docker-compose down -v"
echo ""
echo "  🧹 Complete cleanup (containers, volumes, images):"
echo "     docker-compose down -v --rmi all"
echo ""
echo "  🔄 Restart everything:"
echo "     ./docker-scripts/start.sh"
echo ""
echo "  📊 Check what's still running:"
echo "     docker-compose ps"
echo "     docker ps"

echo ""
echo "💡 Note: Database data persists between stops/starts unless you use -v flag"