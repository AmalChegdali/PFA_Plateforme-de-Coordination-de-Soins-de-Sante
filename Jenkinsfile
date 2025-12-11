pipeline {
    agent any
    stages {
        stage('Checkout SCM') {
            steps {
                checkout scm
            }
        }

        stage('Build Backend Microservices') {
            steps {
                script {
                    // Récupère le répertoire absolu de Jenkins
                    def workspaceDir = pwd()
                    
                    // Cherche tous les dossiers contenant un pom.xml
                    def microservices = sh(
                        script: "find backend -name pom.xml -exec dirname {} \\;",
                        returnStdout: true
                    ).trim().split("\n")

                    for (ms in microservices) {
                        echo "=== Build du microservice : ${ms} ==="

                        // Docker a besoin de chemins absolus correctement échappés
                        def dockerPath = "${workspaceDir}/${ms}".replace(" ", "\\ ")

                        sh """
                        docker run --rm -v "${dockerPath}:/app" -w /app maven:3.9.2-eclipse-temurin-17 mvn clean package -DskipTests
                        """
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def microservices = sh(
                        script: "find backend -name pom.xml -exec dirname {} \\;",
                        returnStdout: true
                    ).trim().split("\n")

                    for (ms in microservices) {
                        def name = ms.tokenize('/')[-1]
                        def dockerPath = "${pwd()}/${ms}".replace(" ", "\\ ")
                        echo "=== Build Docker Image : ${name} ==="
                        sh "docker build -t ${name.toLowerCase()}:latest ${dockerPath}"
                    }
                }
            }
        }

        stage('Run Docker Containers') {
            steps {
                script {
                    def microservices = sh(
                        script: "find backend -name pom.xml -exec dirname {} \\;",
                        returnStdout: true
                    ).trim().split("\n")

                    for (ms in microservices) {
                        def name = ms.tokenize('/')[-1]
                        echo "=== Run Docker Container : ${name} ==="
                        sh "docker run -d --name ${name.toLowerCase()} -p 8080:8080 ${name.toLowerCase()}:latest"
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
