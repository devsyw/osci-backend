#!/bin/bash

# 변수 설정
ECR_REGISTRY = '224600478173.dkr.ecr.ap-northeast-2.amazonaws.com'
ECR_REPOSITORY="resume-backend"
IMAGE_TAG="latest"

echo "======================================"
echo "Starting deployment"
echo "======================================"

# ECR 로그인
echo "Logging in to ECR"
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin $ECR_REGISTRY

if [ $? -ne 0 ]; then
    echo "ECR login failed!"
    exit 1
fi

# 이미지 Pull
echo "Pulling new image"
docker pull $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG

if [ $? -ne 0 ]; then
    echo "Image pull failed!"
    exit 1
fi

# 기존 컨테이너 중지
echo "Stopping existing containers"
docker-compose -f docker-compose.prod.yml down

# 구 이미지 정리
echo "Removing old images"
docker images | grep $ECR_REPOSITORY | grep -v latest | awk '{print $3}' | xargs -r docker rmi -f 2>/dev/null || true

# 환경변수 설정 후 Docker Compose 실행
echo "Starting service with Docker Compose..."
ECR_REGISTRY=$ECR_REGISTRY ECR_REPOSITORY=$ECR_REPOSITORY IMAGE_TAG=$IMAGE_TAG \
docker-compose -f docker-compose.prod.yml up -d

if [ $? -ne 0 ]; then
    echo "Docker Compose start failed!"
    exit 1
fi

# 컨테이너 상태 확인
sleep 10
echo ""
echo "Container status:"
docker-compose -f docker-compose.prod.yml ps

# 로그 확인
echo ""
echo "Recent logs:"
docker-compose -f docker-compose.prod.yml logs --tail=30 resume-backend

echo ""
echo "======================================"
echo "Deployment completed!"
echo "======================================"
echo "Application: http://$(curl -s ifconfig.me):8080"
echo "H2 Console: http://$(curl -s ifconfig.me):8080/h2-console"