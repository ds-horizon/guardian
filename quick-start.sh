#!/usr/bin/env bash
set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to compare versions
version_ge() {
    printf '%s\n%s\n' "$2" "$1" | sort -V -C
}

PREREQUISITES_MET=true

echo "=========================================="
echo "Guardian Quick Start - Prerequisites Check"
echo "=========================================="
echo ""

# Check Java
echo -n "Checking Java 17... "
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | grep -i version | awk -F'"' '{print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" = "17" ] || [ "$JAVA_VERSION" -ge 17 ]; then
        echo -e "${GREEN}✓ Java $JAVA_VERSION found${NC}"
    else
        echo -e "${RED}✗ Java 17 required, but Java $JAVA_VERSION found${NC}"
        echo -e "   Please install Java 17: https://adoptium.net/"
        PREREQUISITES_MET=false
    fi
else
    echo -e "${RED}✗ Java not found${NC}"
    echo -e "   Please install Java 17: https://adoptium.net/"
    PREREQUISITES_MET=false
fi

# Check Maven
echo -n "Checking Maven ≥ 3.6... "
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | grep "Apache Maven" | awk '{print $3}')
    if version_ge "$MVN_VERSION" "3.6"; then
        echo -e "${GREEN}✓ Maven $MVN_VERSION found${NC}"
    else
        echo -e "${RED}✗ Maven ≥ 3.6 required, but Maven $MVN_VERSION found${NC}"
        echo -e "   Please install Maven: https://maven.apache.org/download.cgi"
        PREREQUISITES_MET=false
    fi
else
    echo -e "${RED}✗ Maven not found${NC}"
    echo -e "   Please install Maven: https://maven.apache.org/download.cgi"
    PREREQUISITES_MET=false
fi

# Check Docker
echo -n "Checking Docker ≥ 20.10... "
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version | awk '{print $3}' | sed 's/,//')
    if version_ge "$DOCKER_VERSION" "20.10"; then
        echo -e "${GREEN}✓ Docker $DOCKER_VERSION found${NC}"
    else
        echo -e "${RED}✗ Docker ≥ 20.10 required, but Docker $DOCKER_VERSION found${NC}"
        echo -e "   Please update Docker Desktop: https://www.docker.com/products/docker-desktop"
        PREREQUISITES_MET=false
    fi
else
    echo -e "${RED}✗ Docker not found${NC}"
    echo -e "   Please install Docker Desktop: https://www.docker.com/products/docker-desktop"
    PREREQUISITES_MET=false
fi

# Check if Docker daemon is running
if command -v docker &> /dev/null; then
    echo -n "Checking Docker daemon... "
    if docker info &> /dev/null; then
        echo -e "${GREEN}✓ Docker daemon is running${NC}"
    else
        echo -e "${RED}✗ Docker daemon is not running${NC}"
        echo -e "   Please start Docker Desktop and try again."
        PREREQUISITES_MET=false
    fi
fi

# Check Docker Compose
echo -n "Checking Docker Compose ≥ 2.0... "
if command -v docker &> /dev/null && docker compose version &> /dev/null; then
    COMPOSE_VERSION=$(docker compose version | awk '{print $4}' | sed 's/v//')
    if version_ge "$COMPOSE_VERSION" "2.0"; then
        echo -e "${GREEN}✓ Docker Compose $COMPOSE_VERSION found${NC}"
    else
        echo -e "${RED}✗ Docker Compose ≥ 2.0 required, but Docker Compose $COMPOSE_VERSION found${NC}"
        echo -e "   Please update Docker Desktop: https://www.docker.com/products/docker-desktop"
        PREREQUISITES_MET=false
    fi
else
    echo -e "${RED}✗ Docker Compose not found${NC}"
    echo -e "   Please install Docker Desktop (includes Docker Compose): https://www.docker.com/products/docker-desktop"
    PREREQUISITES_MET=false
fi

echo ""

if [ "$PREREQUISITES_MET" = false ]; then
    echo -e "${RED}=========================================="
    echo "Prerequisites not met!"
    echo -e "==========================================${NC}"
    echo -e "${YELLOW}Please install the missing prerequisites and try again.${NC}"
    exit 1
fi

echo -e "${GREEN}=========================================="
echo -e "All prerequisites satisfied!"
echo -e "==========================================${NC}"
echo ""

# Build and run
echo "Building Guardian..."
mvn clean package

echo "Stopping existing containers..."
docker compose down

echo "Starting Guardian services..."
docker compose up --build --detach

echo ""
echo "Waiting for Guardian service to be ready..."
sleep 5

echo "Running health check..."
if curl -f http://localhost:8080/healthcheck 2>/dev/null; then
    echo ""
    echo -e "${GREEN}=========================================="
    echo -e "✓ Guardian is up and running!"
    echo -e "==========================================${NC}"
    echo ""
    echo -e "Guardian API: http://localhost:8080"
    echo -e "MySQL: localhost:3306"
    echo -e "Redis: localhost:6379"
    echo -e "Mock Service: http://localhost:6000"
else
    echo ""
    echo -e "${YELLOW}Health check failed. Guardian may still be starting up.${NC}"
    echo -e "Check logs with: docker compose logs -f guardian"
fi
