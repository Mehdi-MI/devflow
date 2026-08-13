pipeline {
    agent any

    environment {
        IMAGE_NAME = 'devflow-backend'
        IMAGE_TAG  = "${BUILD_NUMBER}"
        REGISTRY   = 'mehdihsb.azurecr.io'

        CI_NETWORK = 'devflow-ci-network'
        TEST_DB    = 'devflow-test-postgres'
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

        stage('Start Test Database') {
            steps {
                sh '''
                    set -e

                    echo "=== CREATE CI NETWORK ==="

                    docker network create "$CI_NETWORK" 2>/dev/null || true

                    echo "=== CONNECT JENKINS TO CI NETWORK ==="

                    docker network connect "$CI_NETWORK" jenkins 2>/dev/null || true

                    echo "=== REMOVE OLD TEST DATABASE ==="

                    docker rm -f "$TEST_DB" 2>/dev/null || true

                    echo "=== START POSTGRESQL ==="

                    docker run -d \
                        --name "$TEST_DB" \
                        --network "$CI_NETWORK" \
                        -e POSTGRES_DB=devflow_test \
                        -e POSTGRES_USER=postgres \
                        -e POSTGRES_PASSWORD=postgres \
                        postgres:17

                    echo
                    echo "=== WAIT FOR POSTGRESQL ==="

                    READY=false

                    for i in $(seq 1 60); do

                        if docker exec "$TEST_DB" \
                            pg_isready \
                            -U postgres \
                            -d devflow_test \
                            >/dev/null 2>&1; then

                            echo "PostgreSQL is ready."

                            if docker exec "$TEST_DB" \
                                psql \
                                -U postgres \
                                -d devflow_test \
                                -c "SELECT 1;" \
                                >/dev/null 2>&1; then

                                echo "Database connection verified."
                                READY=true
                                break
                            fi
                        fi

                        echo "Waiting for PostgreSQL... $i/60"
                        sleep 2
                    done

                    if [ "$READY" != "true" ]; then
                        echo "ERROR: PostgreSQL did not become ready."
                        docker logs "$TEST_DB" || true
                        exit 1
                    fi

                    echo
                    echo "=== TEST DATABASE ==="

                    docker exec "$TEST_DB" \
                        psql \
                        -U postgres \
                        -d devflow_test \
                        -c "SELECT current_database();"

                    echo
                    echo "=== NETWORK ==="

                    docker network inspect "$CI_NETWORK" \
                        --format '{{range .Containers}}{{.Name}} {{.IPv4Address}}{{println}}'
                '''
            }
        }

        stage('Test') {
            steps {
                dir('backend') {
                    withEnv([
                        'SPRING_DATASOURCE_URL=jdbc:postgresql://devflow-test-postgres:5432/devflow_test',
                        'SPRING_DATASOURCE_USERNAME=postgres',
                        'SPRING_DATASOURCE_PASSWORD=postgres'
                    ]) {
                        sh './gradlew test'
                    }
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
            sh '''
                echo "=== CLEANUP TEST DATABASE ==="
                docker rm -f "$TEST_DB" 2>/dev/null || true

                echo "=== DISCONNECT JENKINS FROM CI NETWORK ==="
                docker network disconnect "$CI_NETWORK" jenkins 2>/dev/null || true

                echo "=== REMOVE CI NETWORK ==="
                docker network rm "$CI_NETWORK" 2>/dev/null || true

                echo "=== DOCKER LOGOUT ==="
                docker logout "$REGISTRY" 2>/dev/null || true

                echo "=== LOCAL IMAGES ==="
                docker images "$IMAGE_NAME" || true
            '''
        }
    }
}
