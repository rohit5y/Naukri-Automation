pipeline {
    agent any

    stages {
        stage('Run Naukri resume automation') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'naukri-login',
                        usernameVariable: 'NAUKRI_EMAIL',
                        passwordVariable: 'NAUKRI_PASSWORD'
                    ),
                    file(
                        credentialsId: 'naukri-resume',
                        variable: 'RESUME_FILE_PATH'
                    )
                ]) {
                    withEnv(['BROWSER_HEADLESS=true']) {
                        script {
                            if (isUnix()) {
                                sh 'mvn clean test'
                            } else {
                                bat 'mvn clean test'
                            }
                        }
                    }
                }
            }
        }
    }
}
