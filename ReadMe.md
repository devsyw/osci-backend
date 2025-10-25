# 오픈소스컨설팅 CI/CD 과제를 위한 Backend Server

- 오픈소스컨설팅 과제테스트를 위한 개인 이력정보를 관리하는 REST API 서비스입니다. Jenkins CI/CD 파이프라인을 통해 AWS ECR 및 EC2에 자동 배포됩니다.
- 본 프로젝트는 과제 심사 이후 비공개 됩니다.

## 프로젝트 개요

- 이 프로젝트는 오픈소스컨설팅 과제테스트를 위한 개인 이력 정보를 관리하는 RESTful API 서버입니다. 
- Spring Boot 3.5 기반으로 개발되었으며, H2 데이터베이스를 사용하여 데이터를 영속화합니다.
- 삭제, 업데이트 API는 불필요하여 현재는 cors 보안 정책으로 비활성화 처리했습니다.

### 주요 특징

- RESTful API
- Spring Boot 3.5 + Java 17
- H2 Database (파일 기반 영속화)
- Docker 컨테이너화
- Jenkins CI/CD 자동 배포
- AWS ECR 이미지 저장소
- 요청자 IP 추적
- 초기 데이터 자동 로드

## 기술 스택

### Backend
- **Language:** Java 17
- **Framework:** Spring Boot 3.3.5
- **Build Tool:** Maven
- **Database:** H2 Database
- **ORM:** Spring Data JPA

### DevOps
- **Containerization:** Docker, Docker Compose
- **CI/CD:** Jenkins
- **Container Registry:** AWS ECR
- **Cloud Platform:** AWS EC2
- **Version Control:** Git, GitHub

### Dependencies
```xml
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- H2 Database
- Lombok
- Spring Boot Actuator
```

## 주요 기능

### 1. 이력서 관리 API
- 이력 목록 조회 (GET /api/resumes)
- 이력 상세 조회 (GET /api/resumes/{id})
- 이력 생성 (POST /api/resumes)
- 이력 수정 (PUT /api/resumes/{id})
- 이력 삭제 (DELETE /api/resumes/{id})

### 2. 요청자 IP 추적
- 모든 조회 API 응답에 클라이언트 IP 포함
- X-Forwarded-For 헤더 지원 (프록시 환경)

### 3. Health Check
- `/health` - 애플리케이션 상태 확인
- `/` - 서비스 정보 및 버전

### 4. H2 Console
- 웹 기반 데이터베이스 관리 인터페이스
- `/h2-console` 경로로 접근

## 🏗️ 시스템 아키텍처 (GPT로 그림)

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│   GitHub    │────▶│   Jenkins    │────▶│   AWS ECR   │
│ Repository  │     │   (CI/CD)    │     │   (Images)  │
└─────────────┘     └──────────────┘     └─────────────┘
                            │                     │
                            ▼                     ▼
                    ┌──────────────┐     ┌─────────────┐
                    │  Build & Test│────▶│  Backend    │
                    │   (Maven)    │     │  EC2 Server │
                    └──────────────┘     └─────────────┘
                                                 │
                                                 ▼
                                         ┌─────────────┐
                                         │   Docker    │
                                         │  Container  │
                                         └─────────────┘
```

### 배포 플로우

1. **코드 Push** → GitHub Repository
2. **Webhook 트리거** → Jenkins Pipeline 자동 시작
3. **빌드** → Maven으로 JAR 파일 생성
4. **Docker 이미지 빌드** → Dockerfile 기반
5. **ECR Push** → AWS ECR에 이미지 업로드
6. **배포** → Backend EC2에서 Docker Compose로 실행
7. **Health Check** → 애플리케이션 정상 동작 확인

## API 명세

### Base URL
```
http://43.201.36.232:8080
```

### Endpoints

#### 1. Health Check
```http
GET /
GET /health
```

**Response:**
```json
{
  "status": "UP",
  "message": "Resume Backend API is running",
  "timestamp": "2025-10-25T14:30:00",
  "version": "1.0.0",
  "clientIp": "123.456.789.0"
}
```

#### 2. 이력서 목록 조회
```http
GET /api/resumes
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "서영우",
    "email": "devsyw@gmail.com",
    "phone": "010-4126-5701",
    "position": "Application Architect",
    "summary": "커머스, 금융, 인증/인가 도메인 경험이 있는 Application Architect 입니다.",
    "yearsOfExperience": 8,
    "skills": "Java, Spring Boot, Docker, Kubernetes, AWS, CI/CD, Jenkins",
    "requestIp": "123.456.789.0"
  }
]
```

#### 3. 이력서 상세 조회
```http
GET /api/resumes/{id}
```

#### 4. 이력서 생성
```http
POST /api/resumes
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "홍길동",
  "email": "hong@example.com",
  "phone": "010-1234-5678",
  "position": "Backend Developer",
  "summary": "5년차 백엔드 개발자입니다.",
  "yearsOfExperience": 5,
  "skills": "Java, Spring, MySQL"
}
```

#### 5. 이력서 수정
```http
PUT /api/resumes/{id}
Content-Type: application/json
```

#### 6. 이력서 삭제
```http
DELETE /api/resumes/{id}
```

## 로컬 개발 환경 설정

### 사전 요구사항
- Java 17 이상
- Maven 3.6 이상
- Docker & Docker Compose (선택사항)

### 1. 프로젝트 클론
```bash
git clone https://github.com/your-username/osci-backend.git
cd osci-backend
```

### 2. 애플리케이션 실행

#### Maven으로 실행
```bash
./mvnw spring-boot:run
```

#### Docker Compose로 실행
```bash
docker-compose up --build
```

### 3. 접속 확인
```bash
# Health Check
curl http://localhost:8080/health

# 이력서 목록 조회
curl http://localhost:8080/api/resumes
```

### 4. H2 Console 접속
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:resumedb
Username: sa
Password: -
```

## CI/CD 파이프라인

### Jenkins Pipeline 구성

```groovy
Jenkins Pipeline 단계:
1. Checkout       - GitHub에서 소스 코드 가져오기
2. Build          - Maven으로 JAR 빌드
3. Docker Build   - Docker 이미지 생성
4. Push to ECR    - AWS ECR에 이미지 업로드
5. Deploy         - Backend 서버에 배포
6. Health Check   - 서비스 정상 동작 확인
7. Verify Data    - 초기 데이터 로드 확인
```

### 자동 배포 트리거
- GitHub `main` 브랜치에 Push 시 자동 배포
- GitHub Webhook 연동

### 환경변수
```bash
AWS_REGION=ap-northeast-2
ECR_REGISTRY=224600478173.dkr.ecr.ap-northeast-2.amazonaws.com
ECR_REPOSITORY=resume-backend
BACKEND_SERVER=43.201.36.232
```

## 배포 환경

### Production Server
- **Platform:** AWS EC2
- **OS:** Amazon Linux 2
- **Runtime:** Docker Container
- **Port:** 8080

### Container Orchestration
- **Tool:** Docker Compose
- **Network:** Bridge Network
- **Volumes:** H2 Database 영속화

### docker-compose.prod.yml
```yaml
services:
  resume-backend:
    image: ${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}
    container_name: resume-backend
    restart: unless-stopped
    ports:
      - "8080:8080"
    volumes:
      - h2-data:/data
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:h2:file:/data/resumedb
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
      interval: 30s
      timeout: 3s
      start_period: 60s
      retries: 3
```

## 모니터링

### Health Check Endpoint
```bash
# 애플리케이션 상태 확인
curl http://43.201.36.232:8080/health

# 상세 정보
curl http://43.201.36.232:8080/
```

### Docker 로그 확인
```bash
# Backend 서버에서
docker-compose -f docker-compose.prod.yml logs -f resume-backend
```

### 컨테이너 상태 확인
```bash
docker-compose -f docker-compose.prod.yml ps
```


## 환경별 설정

### Local (개발 환경)
```properties
spring.datasource.url=jdbc:h2:mem:resumedb
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
```

### Production (운영 환경)
```properties
spring.datasource.url=jdbc:h2:file:/data/resumedb
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
```

##  참고 자료

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Docker Documentation](https://docs.docker.com/)
- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [AWS ECR Documentation](https://docs.aws.amazon.com/ecr/)