pipeline {
    agent any

    parameters {
        string(
            name: 'RESUME_FILE_PATH',
            defaultValue: '',
            description: 'Absolute path to a resume file available on the Jenkins agent'
        )
    }

    stages {
        stage('Validate parameters') {
            steps {
                script {
                    if (params.RESUME_FILE_PATH == null || params.RESUME_FILE_PATH.trim().isEmpty()) {
                        error('Set RESUME_FILE_PATH to a resume file available on the Jenkins agent.')
                    }
                }
            }
        }

        stage('Run Naukri resume automation') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'naukri-login',
                    usernameVariable: 'NAUKRI_EMAIL',
                    passwordVariable: 'NAUKRI_PASSWORD'
                )]) {
                    withEnv([
                        "RESUME_FILE_PATH=${params.RESUME_FILE_PATH}",
                        'BROWSER_HEADLESS=true'
                    ]) {
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
