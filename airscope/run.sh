#!/bin/bash

# =============================================================================
# AirScope Run Script
# Builds and starts the Spring Boot application
# =============================================================================

set -e

echo "🌬️  AirScope - Starting Application"
echo "====================================="

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check if Java 17+ is installed
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ] 2>/dev/null; then
    echo "❌ Java 17 or higher is required. Found: Java $JAVA_VERSION"
    echo "   Install with: brew install openjdk@17  (macOS)"
    echo "                 sudo apt install openjdk-17-jdk  (Ubuntu)"
    exit 1
fi

echo -e "${GREEN}✅ Java $JAVA_VERSION detected.${NC}"

# Check if PostgreSQL is reachable
if ! pg_isready -h localhost -p 5432 -U airscope_user -d airscope &>/dev/null; then
    echo -e "${YELLOW}⚠️  PostgreSQL doesn't seem to be running. Run ./setup.sh first.${NC}"
fi

# Build and run
echo ""
echo -e "${YELLOW}🔨 Building...${NC}"
./mvnw clean package -DskipTests -q

echo ""
echo -e "${GREEN}🚀 Starting AirScope...${NC}"
echo "   Swagger UI:  http://localhost:8080/swagger-ui.html"
echo "   API Docs:    http://localhost:8080/api-docs"
echo ""
echo "Press Ctrl+C to stop."
echo ""

./mvnw spring-boot:run
