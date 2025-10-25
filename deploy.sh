#!/bin/bash

# 변수 설정
ECR_REGISTRY="123456789012.dkr.ecr.ap-northeast-2.amazonaws.com"  # 본인 ECR URI로 변경!
ECR_REPOSITORY="resume-backend"
IMAGE_TAG="latest"

echo "======================================"
echo "🚀 Starting deployment with Docker Compose..."
echo "======================================"

# ECR 로그인
echo "📦 Logging in to ECR..."
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin $ECR_REGISTRY

if [ $? -ne 0 ]; then
    echo "❌ ECR login failed!"
    exit 1
fi

# 이미지 Pull
echo "⬇️ Pulling new image..."
docker pull $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG

if [ $? -ne 0 ]; then
    echo "❌ Image pull failed!"
    exit 1
fi

# docker-compose.yml 생성
echo "📝 Creating docker-compose.yml..."
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  resume-backend:
    image: ECR_IMAGE_PLACEHOLDER
    container_name: resume-backend
    restart: unless-stopped
    ports:
      - "8080:8080"
    volumes:
      - h2-data:/data
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:h2:file:/data/resumedb
      - SPRING_DATASOURCE_USERNAME=sa
      - SPRING_DATASOURCE_PASSWORD=password
      - SPRING_DATASOURCE_DRIVER=org.h2.Driver
      - SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.H2Dialect
      - SPRING_JPA_HIBERNATE_DDL_AUTO=update
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/health"]
      interval: 30s
      timeout: 3s
      start_period: 60s
      retries: 3
    networks:
      - resume-network

volumes:
  h2-data:
    driver: local

networks:
  resume-network:
    driver: bridge
EOF

# ECR 이미지 경로로 치환
sed -i "s|ECR_IMAGE_PLACEHOLDER|$ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG|g" docker-compose.yml

# 기존 컨테이너 중지
echo "🛑 Stopping existing containers..."
docker-compose down

# 구 이미지 정리 (latest 제외)
echo "🗑️ Removing old images..."
docker images | grep $ECR_REPOSITORY | grep -v latest | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || true

# Docker Compose로 실행
echo "▶️ Starting service with Docker Compose..."
docker-compose up -d

if [ $? -ne 0 ]; then
    echo "❌ Docker Compose start failed!"
    exit 1
fi

# 컨테이너 상태 확인
sleep 10
echo ""
echo "📊 Container status:"
docker-compose ps

# 로그 확인
echo ""
echo "📋 Recent logs:"
docker-compose logs --tail=30 resume-backend

echo ""
echo "======================================"
echo "✅ Deployment completed successfully!"
echo "======================================"
echo "🌐 Application URL: http://$(curl -s ifconfig.me):8080"
echo "🗄️ H2 Console: http://$(curl -s ifconfig.me):8080/h2-console"
echo "💾 H2 Data Volume: h2-data"
echo "📁 Volume Location: /var/lib/docker/volumes/app_h2-data"
echo ""
echo "Useful commands:"
echo "  docker-compose logs -f          # View logs"
echo "  docker-compose ps               # Check status"
echo "  docker-compose down             # Stop service"
echo "  docker-compose restart          # Restart service"
echo "  docker-compose exec resume-backend ls -la /data  # Check H2 files"