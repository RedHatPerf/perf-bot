pipeline {
    agent {
        label "lab1"
    }
    options {
        timeout(time: 15, unit: 'MINUTES')
    }
    stages {
        stage('run') {
            steps {
                script {
                    sh "echo \"{\\\"timestamps\\\": {\\\"start\\\": \\\"1747063271703\\\", \\\"stop\\\": \\\"1747063372727\\\"}}\" > ${WORKSPACE}/run.json"
                }
            }
        }
        stage('check') {
            steps {
                script {
                    sh "cat ${WORKSPACE}/run.json"
                }
            }
        }
        stage('upload'){
            steps {
                catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                    horreumUpload (
                        credentials: "horreum-apikey",
                        authenticationType: "API_KEY",
                        test: "getting-started",
                        owner: 'dev-team',
                        schema: 'uri:getting-started:0.1',
                        access: 'PUBLIC',
                        start: '$.timestamps.start',
                        stop: '$.timestamps.stop',
                        jsonFile: "${WORKSPACE}/run.json",
                        addBuildInfo: false
                    )
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: '**/*.json', fingerprint: false
        }
    }
}
