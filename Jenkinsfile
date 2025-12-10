pipeline {
    agent any

    // Définition des variables d'environnement pour Docker
    environment {
        BACKEND_IMAGE_PREFIX = "backend-service"   // Préfixe pour les images backend
        FRONTEND_IMAGE = "frontend-service:latest" // Image frontend
    }

    stages {
        /***************************************
         * Étape 1 : Récupération du code source
         ***************************************/
        stage('Checkout SCM') {
            steps {
                // Cloner le dépôt Git en utilisant les credentials
                git branch: 'main',
                    url: 'https://github.com/Amal23-Hub/PFA_Plateforme-de-Coordination-de-Soins-de-Sant-.git',
                    credentialsId: 'ID12345'
            }
        }

        /***************************************
         * Étape 2 : Build des microservices backend
         ***************************************/
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

                    // Itérer sur chaque microservice
                    for (svc in microservices) {
                        dir("backend/${svc}") {
                            echo "=== Build du microservice : ${svc} ==="
                            sh 'ls -la'  // Vérifie le contenu
                            sh './mvn clean package -DskipTests' // Utilise Maven Wrapper
                        }
                    }
                }
            }
        }

        /***************************************
         * Étape 3 : Build frontend (si présent)
         ***************************************/
        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    echo "=== Build Frontend ==="
                    sh 'ls -la'
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        /***************************************
         * Étape 4 : Construction des images Docker
         ***************************************/
        stage('Build Docker Images') {
            steps {
                script {
                    // Construire les images Docker pour chaque microservice backend
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

                    // Construire l'image frontend
                    dir('frontend') {
                        echo "=== Docker build Frontend ==="
                        sh "docker build -t ${FRONTEND_IMAGE} ."
                    }
                }
            }
        }

        /***************************************
         * Étape 5 : Lancer les containers Docker
         ***************************************/
        stage('Run Docker Containers') {
            steps {
                script {
                    // Backend
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

                    // Frontend
                    sh "docker rm -f frontend-container || true"
                    sh "docker run -d -p 3000:80 --name frontend-container ${FRONTEND_IMAGE}"
                }
            }
        }
    }

    /***************************************
     * Post-actions : Toujours exécuter
     ***************************************/
    post {
        always {
            echo "Pipeline terminé !"
        }
        failure {
            echo "Une erreur est survenue, vérifier les logs."
        }
    }
}
