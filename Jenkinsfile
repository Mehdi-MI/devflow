pipeline {
    agent any

    environment {
        IMAGE_NAME = 'devflow-backend'
        IMAGE_TAG  = "${BUILD_NUMBER}"
        REGISTRY   = 'mehdihsb.azurecr.io'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                dir('backend') {
                    sh './gradlew clean build -x test'
                }
            }
        }

        stage('Test') {
            steps {
                dir('backend') {
                    sh './gradlew test'
                }
            }
        }

        stage('Build Multi-Arch Image') {
            steps {
                dir('backend') {
                    withCredentials([
                        usernamePassword(
                            credentialsId: 'devflow-jenkins-acr',
                            usernameVariable: 'ACR_USERNAME',
                            passwordVariable: 'ACR_PASSWORD'
                        )
                    ]) {
                        sh '''
                            set -e

                            echo "$ACR_PASSWORD" | docker login "$REGISTRY" \
                                --username "$ACR_USERNAME" \
                                --password-stdin

                            docker buildx use multiarch
                            docker buildx inspect multiarch --bootstrap

                            docker buildx build \
                                --platform linux/amd64,linux/arm64 \
                                -t ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} \
                                -t ${REGISTRY}/${IMAGE_NAME}:latest \
                                --push \
                                .
                        '''
                    }
                }
            }
        }

        stage('Verify Multi-Arch Image') {
            steps {
                sh '''
                    docker buildx imagetools inspect \
                        ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
                '''
            }
        }
    }

    post {
        success {
            echo 'DevFlow multi-architecture CI pipeline completed successfully.'
            echo "Image: ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
            echo "Image: ${REGISTRY}/${IMAGE_NAME}:latest"
        }

        failure {
            echo 'DevFlow CI pipeline failed.'
        }

        always {
            sh 'docker logout ${REGISTRY} || true'
            sh 'docker images ${IMAGE_NAME} || true'
        }
    }
}
