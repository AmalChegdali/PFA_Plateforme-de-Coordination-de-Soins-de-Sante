pipeline {
    agent {
        docker {
            image 'maven-git-image:latest'   // Mon image Maven + Git
            args '-v $HOME/.m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock' // cache Maven + accès Docker
        }
    }

    environment {
        DOCKER_IMAGE_BACKEND = "backend-service:latest"
       // DOCKER_IMAGE_FRONTEND = "frontend-service:latest"
        GIT_CREDENTIALS_ID = "ID12345" // Mon GitHub PAT pour Jenkins
    }

    stages {

        stage('Checkout') {
            steps {
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
                // Supprimer les containers existants si nécessaire
                sh 'docker rm -f backend-container || true'
                // sh 'docker rm -f frontend-container || true'

                // Lancer les containers
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
