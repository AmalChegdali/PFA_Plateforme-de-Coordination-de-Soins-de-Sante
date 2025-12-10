pipeline {
    agent any

    environment {
        BACKEND_IMAGE_PREFIX = "backend-service"
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
                            sh 'ls -la'
                            // Utilisation de Maven Docker pour le build
                            sh """
                            docker run --rm \\
                              -v \$(pwd):/app \\
                              -w /app \\
                              maven:3.9.2-eclipse-temurin-17 \\
                              mvn clean package -DskipTests
                            """
                        }
                    }
                }
            }
        }

        /***************************************
         * Étape 3 : Build des images Docker
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
                }
            }
        }

        /***************************************
         * Étape 4 : Lancer les containers Docker
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
                        sh "docker run -d --name ${containerName} -p 8080:8080 ${BACKEND_IMAGE_PREFIX}-${svc.toLowerCase()}:latest"
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline backend terminé !"
        }
        failure {
            echo "Erreur lors du build backend, vérifier les logs."
        }
    }
}
