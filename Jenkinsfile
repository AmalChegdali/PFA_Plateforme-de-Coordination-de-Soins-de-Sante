pipeline {
    agent any

    environment {
        BACKEND_IMAGE_PREFIX = "backend-service"   // Préfixe pour les images backend
        FRONTEND_IMAGE = "frontend-service:latest" // Image frontend
    }

    stages {

        /***************************************
         * Étape 1 : Récupération du code source
         ***************************************/
        stage('Checkout SCM') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/Amal23-Hub/PFA_Plateforme-de-Coordination-de-Soins-de-Sant-.git',
                    credentialsId: 'ID12345'
            }
        }

        /***************************************
         * Étape 2 : Build des microservices backend
         ***************************************/
        stage('Build Backend Microservices') {
            steps {
                script {
                    def microservices = [
                        "Patient-Service",
                        "Provider-Service",
                        "Medicalrecord-Service",
                        "Gateway-Service",
                        "Eureka-Server",
                        "Config-server",
                        "Request-Service"
                    ]

                    for (svc in microservices) {
                        dir("backend/${svc}") {
                            echo "=== Build du microservice : ${svc} ==="
                            sh 'ls -la'  // Vérification du contenu
                            // Build avec Maven dans un container
                            docker.image('maven:3.9.2-openjdk-17').inside {
                                sh 'mvn clean package -DskipTests'
                            }
                        }
                    }
                }
            }
        }

        /***************************************
         * Étape 3 : Build frontend
         ***************************************/
        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    echo "=== Build Frontend ==="
                    sh 'ls -la'
                    // Build frontend dans un container Node.js
                    docker.image('node:20-alpine').inside {
                        sh 'npm install'
                        sh 'npm run build'
                    }
                }
            }
        }

        /***************************************
         * Étape 4 : Construction des images Docker
         ***************************************/
        stage('Build Docker Images') {
            steps {
                script {
                    def microservices = [
                        "Patient-Service",
                        "Provider-Service",
                        "Medicalrecord-Service",
                        "Gateway-Service",
                        "Eureka-Server",
                        "Config-server",
                        "Request-Service"
                    ]

                    for (svc in microservices) {
                        echo "=== Docker build ${svc} ==="
                        sh "docker build -t ${BACKEND_IMAGE_PREFIX}-${svc.toLowerCase()}:latest ./backend/${svc}"
                    }

                    dir('frontend') {
                        echo "=== Docker build Frontend ==="
                        sh "docker build -t ${FRONTEND_IMAGE} ."
                    }
                }
            }
        }

        /***************************************
         * Étape 5 : Lancer les containers Docker
         ***************************************/
        stage('Run Docker Containers') {
            steps {
                script {
                    def microservices = [
                        "Patient-Service",
                        "Provider-Service",
                        "Medicalrecord-Service",
                        "Gateway-Service",
                        "Eureka-Server",
                        "Config-server",
                        "Request-Service"
                    ]

                    for (svc in microservices) {
                        def containerName = "container-${svc.toLowerCase()}"
                        sh "docker rm -f ${containerName} || true"
                        sh "docker run -d --name ${containerName} ${BACKEND_IMAGE_PREFIX}-${svc.toLowerCase()}:latest"
                    }

                    sh "docker rm -f frontend-container || true"
                    sh "docker run -d -p 3000:80 --name frontend-container ${FRONTEND_IMAGE}"
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline terminé !"
        }
        failure {
            echo "Une erreur est survenue, vérifier les logs."
        }
    }
}
