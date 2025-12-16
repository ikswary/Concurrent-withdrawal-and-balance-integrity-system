#!/bin/bash

echo "========================================"
echo "Wallet Withdrawal System - DB Setup"
echo "========================================"
echo ""

echo "[1/3] Stopping existing containers..."
docker compose down

echo ""
echo "[2/3] Starting PostgreSQL..."
docker compose up -d postgres

echo ""
echo "[3/3] Waiting for PostgreSQL to be ready..."
sleep 10

echo ""
echo "[INFO] Checking PostgreSQL status..."
docker compose ps postgres

echo ""
echo "[INFO] Checking database connection..."
docker compose exec -T postgres psql -U wallet_user -d wallet_db -c "\dt"

echo ""
echo "========================================"
echo "Setup completed!"
echo "========================================"
echo "PostgreSQL is running on localhost:5432"
echo "Database: wallet_db"
echo "Username: wallet_user"
echo ""
echo "To start Redis (for distributed lock):"
echo "  docker compose up -d redis"
echo ""
echo "To view logs:"
echo "  docker compose logs -f postgres"
echo "========================================"
