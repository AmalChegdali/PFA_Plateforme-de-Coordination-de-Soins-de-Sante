pipeline {
    agent any
    environment {
        DOCKER_IMAGE_BACKEND = "backend-service:latest"
       // DOCKER_IMAGE_FRONTEND = "frontend-service:latest"
    }
    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/Amal23-Hub/PFA_Plateforme-de-Coordination-de-Soins-de-Sant-.git'
            }
        }

        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        // stage('Build Frontend') {
        //     steps {
        //         dir('frontend') {
        //             sh 'npm install'
        //             sh 'npm run build'
        //         }
        //     }
        // }

        stage('Build Docker Images') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE_BACKEND ./backend'
                // sh 'docker build -t $DOCKER_IMAGE_FRONTEND ./frontend'
            }
        }

        stage('Run Docker Containers') {
            steps {
                sh 'docker rm -f backend-container || true'
               // sh 'docker rm -f frontend-container || true'
                sh 'docker run -d -p 8080:8080 --name backend-container $DOCKER_IMAGE_BACKEND'
               // sh 'docker run -d -p 3000:80 --name frontend-container $DOCKER_IMAGE_FRONTEND'
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
