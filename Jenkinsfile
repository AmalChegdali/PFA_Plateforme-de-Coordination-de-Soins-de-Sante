pipeline {
    agent any

    environment {
        DOCKER_IMAGE_BACKEND = "backend-service:latest"
        // DOCKER_IMAGE_FRONTEND = "frontend-service:latest"
        GIT_CREDENTIALS_ID = "ID12345" // Ton GitHub PAT dans Jenkins
    }

    stages {

        stage('Checkout') {
            steps {
                // Checkout sécurisé avec credentials
                git branch: 'main', 
                    url: 'https://github.com/Amal23-Hub/PFA_Plateforme-de-Coordination-de-Soins-de-Sant-.git',
                    credentialsId: "${GIT_CREDENTIALS_ID}"
            }
        }

        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        // Décommenter si frontend est nécessaire
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
                // Supprimer d'anciens containers si présents
                sh 'docker rm -f backend-container || true'
                // sh 'docker rm -f frontend-container || true'

                // Lancer le container backend
                sh 'docker run -d -p 8080:8080 --name backend-container $DOCKER_IMAGE_BACKEND'

                // Lancer le container frontend si nécessaire
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
