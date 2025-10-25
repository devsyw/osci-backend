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
        stage('Checkout') {
            steps {
                echo 'Checking out code from GitHub'
                checkout scm
            }
        }

        stage('Build with Maven') {
            steps {
                echo 'Building Spring Boot application'
                sh '''
                    chmod +x mvnw
                    ./mvnw clean package -DskipTests
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image'
                script {
                    docker.build("${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}")
                    docker.build("${ECR_REGISTRY}/${ECR_REPOSITORY}:latest")
                }
            }
        }

        stage('Push to ECR') {
            steps {
                echo 'Pushing image to Amazon ECR'
                script {
                    sh """
                        aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                        docker push ${ECR_REGISTRY}/${ECR_REPOSITORY}:${IMAGE_TAG}
                        docker push ${ECR_REGISTRY}/${ECR_REPOSITORY}:latest
                    """
                }
            }
        }

stage('Deploy to Backend Server') {
    steps {
        echo 'Deploying to Backend Server'
        sshagent(credentials: ['backend-ssh-key']) {
            sh """
                # 필요한 파일들 복사
                scp -o StrictHostKeyChecking=no docker-compose.prod.yml ${BACKEND_USER}@${BACKEND_SERVER}:~/app/
                scp -o StrictHostKeyChecking=no deploy.sh ${BACKEND_USER}@${BACKEND_SERVER}:~/app/

                # deploy.sh 실행
                ssh -o StrictHostKeyChecking=no ${BACKEND_USER}@${BACKEND_SERVER} '
                    cd ~/app
                    chmod +x deploy.sh
                    export ECR_REGISTRY=${ECR_REGISTRY}
                    export ECR_REPOSITORY=${ECR_REPOSITORY}
                    export IMAGE_TAG=${IMAGE_TAG}
                    ./deploy.sh
                '
            """
        }
    }
}

        stage('Health Check') {
            steps {
                echo 'Performing health check'
                script {
                    sleep(time: 30, unit: 'SECONDS')
                    def response = sh(
                        script: "curl -s -o /dev/null -w '%{http_code}' http://${BACKEND_SERVER}:8080/health || echo '000'",
                        returnStdout: true
                    ).trim()

                    if (response == '200') {
                        echo 'Application is healthy! HTTP 200'
                    } else {
                        echo "Health check returned HTTP ${response}"
                        error "Health check failed!"
                    }
                }
            }
        }

        stage('Initial Data') {
            steps {
                echo 'initial data'
                script {
                    def apiResponse = sh(
                        script: "curl -s http://${BACKEND_SERVER}:8080/api/resumes | grep -o '서영우' || echo 'NOT_FOUND'",
                        returnStdout: true
                    ).trim()

                    if (apiResponse.contains('서영우')) {
                        echo 'Initial data loaded successfully!'
                    } else {
                        echo 'Initial data not found, but application is running'
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
            echo "Application URL: http://${BACKEND_SERVER}:8080"
            echo "API Endpoint: http://${BACKEND_SERVER}:8080/api/resumes"
            echo "H2 Console: http://${BACKEND_SERVER}:8080/h2-console"
        }
        failure {
            echo 'Pipeline failed!'
        }
        always {
            echo 'Cleaning up workspace'
            sh 'docker image prune -f'
        }
    }
}