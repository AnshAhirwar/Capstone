pipeline {
    agent any

    tools {
        jdk 'JDK21'
        maven 'Maven3'
    }

    stages {

        stage('Build & Test') {
            steps {
                bat 'mvn clean test'
            }
        }

        stage('Publish Allure Report') {
            steps {
                allure([
                    results: [[path: 'target/allure-results']]
                ])
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
        }
    }
}
