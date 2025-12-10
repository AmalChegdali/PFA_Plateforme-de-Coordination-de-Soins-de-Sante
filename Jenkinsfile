pipeline {
    agent any

    environment {
        // Définir ici éventuellement les variables d'environnement nécessaires
        DOCKER_IMAGE = "maven:3.9.2-eclipse-temurin-17"
    }

    stages {
        stage('Checkout SCM') {
            steps {
                git(
                    url: 'https://github.com/Amal23-Hub/PFA_Plateforme-de-Coordination-de-Soins-de-Sant-.git',
                    branch: 'main',
                    credentialsId: 'ID12345'
                )
            }
        }

        stage('Build Backend Microservices') {
            steps {
                script {
                    // Liste des microservices backend
                    def services = ['Patient-Service'] 

                    services.each { service ->
                        dir("backend/${service}") {
                            echo "=== Build du microservice : ${service} ==="
                            
                            // Construire avec Maven via Docker
                            sh """
                                docker run --rm \
                                -v \$(pwd):/app \
                                -w /app \
                                ${DOCKER_IMAGE} \
                                mvn clean package -DskipTests
                            """
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def services = ['Patient-Service'] 

                    services.each { service ->
                        echo "=== Build Docker image pour : ${service} ==="
                        sh """
                            docker build -t ${service.toLowerCase()}:latest backend/${service}
                        """
                    }
                }
            }
        }

        stage('Run Docker Containers') {
            steps {
                script {
                    def services = ['Patient-Service']

                    services.each { service ->
                        echo "=== Lancer le container : ${service} ==="
                        sh """
                            docker run -d -p 8080:8080 --name ${service.toLowerCase()} ${service.toLowerCase()}:latest
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline terminé avec succès !"
        }
        failure {
            echo "Erreur lors du pipeline, vérifier les logs."
        }
    }
}
