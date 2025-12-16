#!/bin/bash

echo "========================================"
echo "Wallet System - Redis Mode Startup"
echo "========================================"
echo ""

echo "[1/4] Stopping existing containers..."
docker compose down

echo ""
echo "[2/4] Starting PostgreSQL and Redis..."
docker compose up -d

echo ""
echo "[3/4] Waiting for services to be ready..."
sleep 15

echo ""
echo "[4/4] Checking services status..."
docker compose ps

echo ""
echo "========================================"
echo "Docker containers started successfully!"
echo "========================================"
echo "PostgreSQL: localhost:5432"
echo "Redis: localhost:6379"
echo "Database: wallet_db"
echo "Username: wallet_user"
echo ""
echo "Starting Spring Boot application..."
echo "Profile: redis (RedisLockManager)"
echo "========================================"
echo ""

./gradlew bootRun --args='--spring.profiles.active=redis'
