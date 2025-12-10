pipeline {
    // L'agent ici utilise une image Docker contenant Maven (pour le backend)
    agent {
        docker {
            image 'maven-git-image:latest' // Image Docker utilisée pour exécuter Maven
            args '-v $HOME/.m2:/root/.m2 -v /var/run/docker.sock:/var/run/docker.sock' // Montage du cache Maven et du socket Docker pour pouvoir builder et lancer des containers
        }
    }

    // Variables d'environnement
    environment {
        DOCKER_IMAGE_BACKEND = "backend-service:latest"   // Nom de l'image Docker backend
       // DOCKER_IMAGE_FRONTEND = "frontend-service:latest" // Nom de l'image Docker frontend
        GIT_CREDENTIALS_ID = "ID12345"                    // ID des credentials GitHub (token PAT)
    }

    stages {

        // Étape 1 : Cloner le code depuis GitHub
        stage('Checkout') {
            steps {
                git branch: 'main', 
                    url: 'https://github.com/Amal23-Hub/PFA_Plateforme-de-Coordination-de-Soins-de-Sant-.git',
                    credentialsId: "${GIT_CREDENTIALS_ID}" // Utilise le token PAT pour accéder au repo privé
            }
        }

        // Étape 2 : Build du backend avec Maven
        stage('Build Backend') {
            steps {
                dir('backend') { // Se place dans le dossier backend
                    sh 'mvn clean package -DskipTests' // Compile et package le backend, sans exécuter les tests
                }
            }
        }

        // Étape 3 : Build du frontend avec Node.js
        // stage('Build Frontend') {
        //     steps {
        //         dir('frontend') { // Se place dans le dossier frontend
        //             sh 'npm install'  // Installe les dépendances du frontend
        //             sh 'npm run build' // Build du frontend (génère les fichiers statiques dans /build ou /dist)
        //         }
        //     }
        // }

        // Étape 4 : Création des images Docker pour backend et frontend
        stage('Build Docker Images') {
            steps {
                // Build de l'image backend
                sh 'docker build -t $DOCKER_IMAGE_BACKEND ./backend'
                
                // Build de l'image frontend
               // sh 'docker build -t $DOCKER_IMAGE_FRONTEND ./frontend'
            }
        }

        // Étape 5 : Lancer les containers Docker
        stage('Run Docker Containers') {
            steps {
                // Supprime les containers existants s'ils existent
                sh 'docker rm -f backend-container || true'
                // sh 'docker rm -f frontend-container || true'

                // Lancer le container backend sur le port 8080
                sh 'docker run -d -p 8080:8080 --name backend-container $DOCKER_IMAGE_BACKEND'

                // Lancer le container frontend sur le port 3000
                // sh 'docker run -d -p 3000:80 --name frontend-container $DOCKER_IMAGE_FRONTEND'
            }
        }
    }

    post {
        // Actions exécutées à la fin du pipeline, succès ou échec
        always {
            echo "Pipeline terminé !"
        }
        // Actions spécifiques si le pipeline échoue
        failure {
            echo "Une erreur est survenue, vérifier les logs."
        }
    }
}
