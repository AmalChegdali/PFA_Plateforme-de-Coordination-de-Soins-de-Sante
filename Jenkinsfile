pipeline {
    agent any  // Exécuter le pipeline sur n'importe quel agent Jenkins disponible

    environment {
        // Définition des noms des images Docker pour les microservices
        DOCKER_IMAGE_BACKEND = "backend-service:latest"
       // DOCKER_IMAGE_FRONTEND = "frontend-service:latest"

        // ID des credentials GitHub (PAT) configuré dans Jenkins
        GIT_CREDENTIALS_ID = "ID12345"
    }

    stages {

        // Étape 1 : Récupérer le code depuis Git
        stage('Checkout') {
            steps {
                // Utilisation des credentials pour authentification GitHub
                git branch: 'main', 
                    url: 'https://github.com/Amal23-Hub/PFA_Plateforme-de-Coordination-de-Soins-de-Sant-.git',
                    credentialsId: "${GIT_CREDENTIALS_ID}"
                // Cette étape clone le dépôt et positionne le workspace sur la branche 'main'
            }
        }

        // Étape 2 : Build du backend
        stage('Build Backend') {
            steps {
                dir('backend') {  // Se positionner dans le dossier backend
                    echo "Vérification du POM backend"
                    sh 'ls -l pom.xml'  // Vérifie que le fichier pom.xml est présent
                    echo "Build du backend avec Maven"
                    sh 'mvn clean package -DskipTests'  // Compile et package le backend en ignorant les tests
                }
            }
        }

        // Étape 3 : Build du frontend
        // stage('Build Frontend') {
        //     steps {
        //         dir('frontend') {  // Se positionner dans le dossier frontend
        //             echo "Vérification du POM frontend"
        //             sh 'ls -l pom.xml'  // Vérifie que le fichier pom.xml est présent
        //             echo "Build du frontend avec Maven"
        //             sh 'mvn clean package -DskipTests'  // Compile et package le frontend
        //         }
        //     }
        // }

        // Étape 4 : Création des images Docker
        stage('Build Docker Images') {
            steps {
                echo "Création de l'image backend"
                sh 'docker build -t $DOCKER_IMAGE_BACKEND ./backend'  // Build Docker image pour le backend

                // echo "Création de l'image frontend"
                // sh 'docker build -t $DOCKER_IMAGE_FRONTEND ./frontend'  // Build Docker image pour le frontend
            }
        }

        // Étape 5 : Lancer les containers Docker
        stage('Run Docker Containers') {
            steps {
                echo "Suppression des containers existants (si présents)"
                sh 'docker rm -f backend-container || true'  // Supprime le container backend existant
                // sh 'docker rm -f frontend-container || true'  // Supprime le container frontend existant

                echo "Lancement du container backend"
                sh 'docker run -d -p 8080:8080 --name backend-container $DOCKER_IMAGE_BACKEND'  // Run backend container

                // echo "Lancement du container frontend"
                // sh 'docker run -d -p 3000:80 --name frontend-container $DOCKER_IMAGE_FRONTEND'  // Run frontend container
            }
        }
    }

    // Étape post-exécution
    post {
        always {
            echo "Pipeline terminé !"  // Toujours affiché à la fin, succès ou échec
        }
        failure {
            echo "Une erreur est survenue, vérifier les logs."  // Affiché seulement si le pipeline échoue
        }
    }
}
