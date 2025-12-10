pipeline {
    agent any

    environment {
        BACKEND_IMAGE_PREFIX = "backend-service"
    }

    stages {

        stage('Checkout SCM') {
            steps {
                // Cloner le dépôt Git
                git branch: 'main',
                    url: 'https://github.com/Amal23-Hub/PFA_Plateforme-de-Coordination-de-Soins-de-Sant-.git',
                    credentialsId: 'ID12345'
            }
        }

        stage('Build Backend Microservices') {
            steps {
                script {
                    // Liste des microservices backend
                    def microservices = [
                        "Patient-Service",
                        "Provider-Service",
                        "Medicalrecord-Service",
                        "Gateway-Service",
                        "Eureka-Server",
                        "Config-server",
                        "Request-Service"
                    ]

                    // Construire chaque microservice avec Maven dans Docker
                    for (svc in microservices) {
                        dir("backend/${svc}") {
                            echo "=== Build du microservice : ${svc} ==="
                            sh 'ls -la'  // Vérifie le contenu
                            // Utilisation de Maven dans Docker
                            sh 'docker run --rm -v $PWD:/app -w /app maven:3.9.2-openjdk-17 mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    // Construire les images Docker pour chaque microservice
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
