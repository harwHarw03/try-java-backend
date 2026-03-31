#!/bin/bash

# =============================================================================
# AirScope DB Utility Script
# Handy commands for inspecting the database during development
# =============================================================================

DB_NAME="airscope"
DB_USER="airscope_user"
DB_HOST="localhost"
DB_PORT="5432"

DYNAMO_ENDPOINT="http://localhost:8000"

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

show_help() {
    echo "🌬️  AirScope DB Utility"
    echo "Usage: ./db.sh [command]"
    echo ""
    echo "Commands:"
    echo "  users       - List all users"
    echo "  devices     - List all devices"
    echo "  alerts      - List all alerts"
    echo "  dynamo      - List sensor_data items in DynamoDB Local"
    echo "  reset       - Drop and recreate the PostgreSQL database (⚠️  deletes all data)"
    echo "  psql        - Open interactive psql shell"
    echo ""
}

case "$1" in
    users)
        echo -e "${YELLOW}👥 Users:${NC}"
        psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT id, email, role FROM users ORDER BY id;"
        ;;
    devices)
        echo -e "${YELLOW}📡 Devices:${NC}"
        psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT d.id, d.name, u.email as owner FROM devices d JOIN users u ON d.user_id = u.id ORDER BY d.id;"
        ;;
    alerts)
        echo -e "${YELLOW}🔔 Alerts:${NC}"
        psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT a.id, a.type, a.threshold, d.name as device FROM alerts a JOIN devices d ON a.device_id = d.id ORDER BY a.id;"
        ;;
    dynamo)
        echo -e "${YELLOW}📊 DynamoDB sensor_data (first 10 items):${NC}"
        aws dynamodb scan \
            --table-name sensor_data \
            --max-items 10 \
            --endpoint-url "$DYNAMO_ENDPOINT" \
            --region us-east-1 \
            --output table 2>/dev/null || echo "❌ DynamoDB Local not running or table doesn't exist."
        ;;
    reset)
        echo -e "${YELLOW}⚠️  This will delete ALL data in the '$DB_NAME' database.${NC}"
        read -p "Are you sure? (yes/no): " confirm
        if [ "$confirm" = "yes" ]; then
            psql -h "$DB_HOST" -U postgres -c "DROP DATABASE IF EXISTS $DB_NAME;"
            psql -h "$DB_HOST" -U postgres -c "CREATE DATABASE $DB_NAME OWNER $DB_USER;"
            echo -e "${GREEN}✅ Database reset. Restart the app to recreate tables.${NC}"
        else
            echo "Cancelled."
        fi
        ;;
    psql)
        psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME"
        ;;
    *)
        show_help
        ;;
esac
