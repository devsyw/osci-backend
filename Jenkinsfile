pipeline {
    agent any

    environment {
        AWS_REGION = 'ap-northeast-2'
        ECR_REGISTRY = '224600478173.dkr.ecr.ap-northeast-2.amazonaws.com'
        ECR_REPOSITORY = 'resume-backend'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        BACKEND_SERVER = '43.201.36.232'
        BACKEND_USER = 'ec2-user'
    }

    stages {
        // Stage 1. 코드 체크아웃
        stage('Checkout') {
            steps {
                echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
                echo 'Checking out code from GitHub'
                echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'

                checkout scm

                // 현재 브랜치,, 커밋 정보 출력
                sh '''
                    echo "Branch: $(git branch --show-current)"
                    echo "Commit: $(git log -1 --pretty=format:'%h - %s')"
                    echo "Author: $(git log -1 --pretty=format:'%an <%ae>')"
                '''
            }
        }

        // Stage 2. Maven 빌드
        stage('Build with Maven') {
            steps {
                echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
                echo 'Building Spring Boot application'
                echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'

                sh '''
                    chmod +x mvnw
                    ./mvnw clean package -DskipTests
                    echo "Built JAR files:"
                    ls -lh target/*.jar
                '''
            }
        }

        // Stage 3: Docker 이미지 빌드
        stage('Build Docker Image') {
            steps {
                echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
                echo 'Building Docker image'
                echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'

                script {
                    docker.build("${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}")
                    docker.build("${ECR_REGISTRY}/${ECR_REPOSITORY}:latest")

                    echo "Docker image built: ${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}"
                }
            }
        }

        // Stage 4. ECR에 이미지 Push
        stage('Push to ECR') {
            steps {
                echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
                echo 'Pushing image to Amazon ECR'
                echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'

                script {
                    sh """
                        echo "Logging in to ECR"
                        aws ecr get-login-password --region ${AWS_REGION} | \
                        docker login --username AWS --password-stdin ${ECR_REGISTRY}

                        echo "Pushing image with tag: ${IMAGE_TAG}"
                        docker push ${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}

                        echo "Pushing image with tag: latest"
                        docker push ${ECR_REGISTRY}/${ECR_REPOSITORY}:latest

                        echo "Images pushed successfully!"
                    """
                }
            }
        }

        // Stage 5. Backend 서버에 배포
        stage('Deploy to Backend Server') {
            steps {
                echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
                echo 'Deploying to Backend Server'
                echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'

                // SSH deploy
                sshagent(credentials: ['backend-ssh-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${BACKEND_USER}@${BACKEND_SERVER} '
                            echo "Connected to Backend Server"
                            cd ~/app

                            echo "Running deployment script"
                            ./deploy.sh

                            echo "Deployment completed!"
                        '
                    """
                }
            }
        }

        // Stage 6. Health Check
        stage('Health Check') {
            steps {
                echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
                echo 'Performing health check'
                echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'

                script {
                    // 애플리케이션 시작 대기
                    echo "Waiting for application to start"
                    sleep(time: 30, unit: 'SECONDS')

                    // Health check 수행
                    def maxRetries = 5
                    def retryCount = 0
                    def healthCheckPassed = false

                    while (retryCount < maxRetries && !healthCheckPassed) {
                        retryCount++
                        echo "Health check attempt ${retryCount}/${maxRetries}..."

                        def response = sh(
                            script: "curl -s -o /dev/null -w '%{http_code}' http://${BACKEND_SERVER}:8080/health || echo '000'",
                            returnStdout: true
                        ).trim()

                        echo "HTTP Response: ${response}"

                        if (response == '200') {
                            healthCheckPassed = true
                            echo 'Health check passed! Application is running.'
                        } else {
                            if (retryCount < maxRetries) {
                                echo "Waiting 10 seconds before retry"
                                sleep(time: 10, unit: 'SECONDS')
                            }
                        }
                    }

                    if (!healthCheckPassed) {
                        error "Health check failed after ${maxRetries} attempts!"
                    }
                }
            }
        }
    }

    // 빌드 후
    post {
        success {
            echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
            echo 'Pipeline completed successfully!'
            echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
            echo "Application URL: http://${BACKEND_SERVER}:8080"
            echo "H2 Console: http://${BACKEND_SERVER}:8080/h2-console"
            echo "ECR Image: ${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}"
            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        }

        failure {
            echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
            echo 'Pipeline failed!'
            echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
            echo 'Please check the logs above for errors.'
        }

        always {
            echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'
            echo 'Cleaning up...'
            echo '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━'

            sh '''
                echo "Removing dangling images"
                docker image prune -f || true
            '''
        }
    }
}