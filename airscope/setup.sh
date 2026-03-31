#!/bin/bash

# =============================================================================
# AirScope Setup Script
# Sets up PostgreSQL database and DynamoDB Local for development
# =============================================================================

set -e  # Exit immediately if any command fails

echo "🌬️  AirScope - Setup Script"
echo "================================"

# --- Colors for output ---
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# --- PostgreSQL Setup ---
echo ""
echo -e "${YELLOW}📦 Setting up PostgreSQL...${NC}"

DB_NAME="airscope"
DB_USER="airscope_user"
DB_PASS="airscope_pass"

# Check if psql is available
if ! command -v psql &> /dev/null; then
    echo -e "${RED}❌ PostgreSQL not found. Please install it first:${NC}"
    echo "   macOS:  brew install postgresql && brew services start postgresql"
    echo "   Ubuntu: sudo apt install postgresql && sudo systemctl start postgresql"
    exit 1
fi

# Create database and user
echo "Creating database and user..."
psql -U postgres <<EOF
-- Create the user if it doesn't exist
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = '${DB_USER}') THEN
    CREATE USER ${DB_USER} WITH PASSWORD '${DB_PASS}';
  END IF;
END
\$\$;

-- Create the database if it doesn't exist
SELECT 'CREATE DATABASE ${DB_NAME} OWNER ${DB_USER}'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${DB_NAME}')\gexec

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE ${DB_NAME} TO ${DB_USER};
EOF

echo -e "${GREEN}✅ PostgreSQL database '${DB_NAME}' ready.${NC}"

# --- DynamoDB Local Setup ---
echo ""
echo -e "${YELLOW}📦 Setting up DynamoDB Local...${NC}"

DYNAMODB_DIR="$HOME/.dynamodb-local"
DYNAMODB_JAR="$DYNAMODB_DIR/DynamoDBLocal.jar"
DYNAMODB_PORT=8000

if [ ! -f "$DYNAMODB_JAR" ]; then
    echo "Downloading DynamoDB Local..."
    mkdir -p "$DYNAMODB_DIR"

    curl -fL --retry 3 \
    -o /tmp/dynamodb_local.tar.gz \
    https://dynamodb-local.s3.us-west-2.amazonaws.com/dynamodb_local_latest.tar.gz

    if ! file /tmp/dynamodb_local.tar.gz | grep -q gzip; then
        echo -e "${RED}❌ Download failed or invalid file format.${NC}"
        exit 1
    fi

    tar -xzf /tmp/dynamodb_local.tar.gz -C "$DYNAMODB_DIR"
    rm /tmp/dynamodb_local.tar.gz

    echo -e "${GREEN}✅ DynamoDB Local downloaded.${NC}"
else
    echo -e "${GREEN}✅ DynamoDB Local already installed.${NC}"
fi

# Start DynamoDB Local in background if not already running
if ! lsof -Pi :$DYNAMODB_PORT -sTCP:LISTEN -t &>/dev/null; then
    echo "Starting DynamoDB Local on port $DYNAMODB_PORT..."
    java -Djava.library.path="$DYNAMODB_DIR/DynamoDBLocal_lib" \
         -jar "$DYNAMODB_JAR" \
         -sharedDb \
         -port $DYNAMODB_PORT &
    sleep 2
    echo -e "${GREEN}✅ DynamoDB Local running on port $DYNAMODB_PORT.${NC}"
else
    echo -e "${GREEN}✅ DynamoDB Local already running on port $DYNAMODB_PORT.${NC}"
fi

# Create the sensor_data table in DynamoDB Local
echo "Creating DynamoDB 'sensor_data' table..."
aws dynamodb create-table \
    --table-name sensor_data \
    --attribute-definitions \
        AttributeName=deviceId,AttributeType=S \
        AttributeName=timestamp,AttributeType=S \
    --key-schema \
        AttributeName=deviceId,KeyType=HASH \
        AttributeName=timestamp,KeyType=RANGE \
    --billing-mode PAY_PER_REQUEST \
    --endpoint-url http://localhost:$DYNAMODB_PORT \
    --region us-east-1 \
    2>/dev/null && echo -e "${GREEN}✅ 'sensor_data' table created.${NC}" \
               || echo -e "${YELLOW}⚠️  'sensor_data' table may already exist (that's OK).${NC}"

# --- Update application.properties for local dev ---
echo ""
echo -e "${YELLOW}📝 Updating application.properties for local development...${NC}"
PROPS_FILE="src/main/resources/application.properties"

if [ -f "$PROPS_FILE" ]; then
    sed -i.bak 's/aws.dynamodb.local=false/aws.dynamodb.local=true/' "$PROPS_FILE"
    echo -e "${GREEN}✅ DynamoDB local mode enabled in application.properties${NC}"
fi

echo ""
echo -e "${GREEN}🎉 Setup complete! You can now run the app with: ./run.sh${NC}"
echo ""
echo "Connection details:"
echo "  PostgreSQL: jdbc:postgresql://localhost:5432/$DB_NAME (user: $DB_USER)"
echo "  DynamoDB:   http://localhost:$DYNAMODB_PORT"
echo "  Swagger UI: http://localhost:8080/swagger-ui.html (after app starts)"
