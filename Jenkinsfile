pipeline {
    agent any
    environment {
        DOCKER_IMAGE = "maven:3.9.2-eclipse-temurin-17"
    }
    stages {
        stage('Checkout SCM') {
            steps {
                git url: 'https://github.com/Amal23-Hub/PFA_Plateforme-de-Coordination-de-Soins-de-Sant-.git',
                    branch: 'main',
                    credentialsId: 'ID12345'
            }
        }

        stage('Build Backend Microservices') {
            steps {
                script {
                    // Liste des microservices
                    def microservices = ['Patient-Service', 'Medecin-Service', 'RendezVous-Service']

                    microservices.each { service ->
                        dir("backend/${service}") {
                            echo "=== Build du microservice : ${service} ==="
                            
                            // Build Maven dans Docker
                            sh """
                                docker run --rm -v \$(pwd):/app -w /app ${DOCKER_IMAGE} mvn clean package -DskipTests
                            """
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def microservices = ['Patient-Service', 'Medecin-Service', 'RendezVous-Service']
                    
                    microservices.each { service ->
                        dir("backend/${service}") {
                            echo "=== Build Docker image : ${service} ==="
                            sh """
                                docker build -t ${service.toLowerCase()}:latest .
                            """
                        }
                    }
                }
            }
        }

        stage('Run Docker Containers') {
            steps {
                script {
                    def microservices = ['Patient-Service', 'Medecin-Service', 'RendezVous-Service']

                    microservices.each { service ->
                        echo "=== Run Docker container : ${service} ==="
                        sh """
                            docker run -d --name ${service.toLowerCase()} -p 8080:8080 ${service.toLowerCase()}:latest
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline terminé !'
        }
        failure {
            echo 'Erreur lors du pipeline, vérifier les logs.'
        }
    }
}
