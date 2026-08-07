pipeline {
    agent any

    environment {
        IMAGE_NAME = 'devflow-backend'
        IMAGE_TAG = "${BUILD_NUMBER}"
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

        stage('Docker Build') {
            steps {
                dir('backend') {
                    sh """
                        docker build \
                            -t ${IMAGE_NAME}:${IMAGE_TAG} \
                            -t ${IMAGE_NAME}:latest \
                            .
                    """
                }
            }
        }

        stage('Docker Verify') {
            steps {
                sh 'docker image inspect ${IMAGE_NAME}:${IMAGE_TAG}'
            }
        }
    }

    post {
        success {
            echo 'DevFlow CI pipeline completed successfully.'
        }

        failure {
            echo 'DevFlow CI pipeline failed.'
        }

        always {
            sh 'docker images ${IMAGE_NAME} || true'
        }
    }
}
