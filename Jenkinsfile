pipeline {
    agent any

    environment {
        DOCKER_IMAGE_PREFIX = "sante-maroc" // préfixe pour vos images Docker
    }

    stages {
        stage('Checkout SCM') {
            steps {
                checkout([$class: 'GitSCM', 
                          branches: [[name: '*/main']], 
                          userRemoteConfigs: [[
                              url: 'https://github.com/Amal23-Hub/PFA_Plateforme-de-Coordination-de-Soins-de-Sant-.git',
                              credentialsId: 'ID12345'
                          ]]
                ])
            }
        }

        stage('Build Backend Microservices') {
            steps {
                script {
                    // Récupère tous les dossiers contenant un pom.xml
                    def services = sh(
                        script: "find backend -name pom.xml -exec dirname {} \\;",
                        returnStdout: true
                    ).trim().split("\n")

                    services.each { serviceDir ->
                        dir(serviceDir) {
                            echo "=== Build du microservice : ${serviceDir} ==="
                            sh """
                                docker run --rm -v \$(pwd):/app -w /app maven:3.9.2-eclipse-temurin-17 mvn clean package -DskipTests
                            """
                        }
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    def services = sh(
                        script: "find backend -name pom.xml -exec dirname {} \\;",
                        returnStdout: true
                    ).trim().split("\n")

                    services.each { serviceDir ->
                        dir(serviceDir) {
                            def imageName = "${DOCKER_IMAGE_PREFIX}/${serviceDir.split('/').last()}:latest"
                            echo "=== Build Docker image : ${imageName} ==="
                            sh "docker build -t ${imageName} ."
                        }
                    }
                }
            }
        }

        stage('Run Docker Containers') {
            steps {
                script {
                    def services = sh(
                        script: "find backend -name pom.xml -exec dirname {} \\;",
                        returnStdout: true
                    ).trim().split("\n")

                    services.each { serviceDir ->
                        def containerName = serviceDir.split('/').last()
                        def imageName = "${DOCKER_IMAGE_PREFIX}/${containerName}:latest"
                        echo "=== Run Docker container : ${containerName} ==="
                        sh """
                            docker rm -f ${containerName} || true
                            docker run -d --name ${containerName} -p 8${containerName.hashCode().toString().take(3)}:8080 ${imageName}
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
