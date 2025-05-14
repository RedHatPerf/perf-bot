def run = """{
  "counters": [],
  "timestamps": {
    "setupStart": 1747063271923,
    "downloadStop": 1747063372718,
    "cleanupStop": 1747063372714,
    "stop": 1747063372727,
    "downloadStart": 1747063372714,
    "start": 1747063271703,
    "cleanupStart": 1747063370851,
    "runStart": 1747063285140,
    "setupStop": 1747063284831,
    "runStop": 1747063370644
  },
  "state": {
    "SUT_SERVER": "LOCAL",
    "APP_IMAGE": "localhost/\${{APP_TAG_GROUP}}/\${{APP_TAG_NAME}}:\${{APP_TAG_VERSION}}",
    "APP_JAVA_VERSION": "temurin-21.0.5+11.0.LTS",
    "HF_STEADY_PHASE_NAME": "performFight",
    "HF_RUN_ID": "0003",
    "HF_REPORT_FILE": "/tmp/report.html",
    "LOCAL": {
      "APP_POD_ID": "7c645b9fe9d407d151f21da3eb30c7f4d239af9a2c757872022dc1c1b3763a43",
      "HF_REPORT_ALL": {
        "agentCpu": {
          "hello": {"in-vm": "7.1% (1.6/22 cores), 1 core max 28.0%"},
          "warmUp": {"in-vm": "12.3% (2.7/22 cores), 1 core max 33.8%"}
        },
        "failures": [],
        "\$schema": "http://hyperfoil.io/run-schema/v3.0",
        "stats": [
          {
            "phase": "hello",
            "isWarmup": false,
            "fork": "",
            "total": {
              "phase": "hello",
              "summary": {
                "requestCount": 1180,
                "percentileResponseTime": {
                  "99.9": 4947967,
                  "90.0": 1236991,
                  "99.99": 23855103,
                  "50.0": 737279,
                  "99.0": 2080767
                },
                "minResponseTime": 110592,
                "meanResponseTime": 807556,
                "maxResponseTime": 23855103,
                "connectionErrors": 0,
                "responseCount": 1180,
                "requestTimeouts": 0,
                "internalErrors": 0,
                "extensions": {"http": {
                  "status_5xx": 0,
                  "@type": "http",
                  "status_2xx": 1180,
                  "cacheHits": 0,
                  "status_3xx": 0,
                  "status_other": 0,
                  "status_4xx": 0
                }},
                "invalid": 0,
                "startTime": 1747063309156,
                "endTime": 1747063369157,
                "blockedTime": 0,
                "stdDevResponseTime": 775043
              },
              "failures": 0,
              "maxSessions": 2,
              "metric": "hello",
              "start": 1747063309156,
              "end": 1747063369157,
              "minSessions": 0
            },
            "metric": "hello",
            "name": "hello",
            "iteration": ""
          },
          {
            "phase": "warmUp",
            "isWarmup": false,
            "fork": "",
            "total": {
              "phase": "warmUp",
              "summary": {
                "requestCount": 236,
                "percentileResponseTime": {
                  "99.9": 78118911,
                  "90.0": 2179071,
                  "99.99": 78118911,
                  "50.0": 1474559,
                  "99.0": 3047423
                },
                "minResponseTime": 399360,
                "meanResponseTime": 1826616,
                "maxResponseTime": 78118911,
                "connectionErrors": 0,
                "responseCount": 236,
                "requestTimeouts": 0,
                "internalErrors": 0,
                "extensions": {"http": {
                  "status_5xx": 0,
                  "@type": "http",
                  "status_2xx": 236,
                  "cacheHits": 0,
                  "status_3xx": 0,
                  "status_other": 0,
                  "status_4xx": 0
                }},
                "invalid": 0,
                "startTime": 1747063289154,
                "endTime": 1747063309159,
                "blockedTime": 0,
                "stdDevResponseTime": 4992448
              },
              "failures": 0,
              "maxSessions": 2,
              "metric": "getAllHeroes",
              "start": 1747063289154,
              "end": 1747063309159,
              "minSessions": 0
            },
            "metric": "getAllHeroes",
            "name": "warmUp",
            "iteration": ""
          }
        ],
        "commit": "cd58b4a5e7bace943eaf4f93f6d59c06c1f225fc",
        "version": "0.27.1",
        "info": {
          "terminateTime": 1747063369157,
          "cancelled": false,
          "startTime": 1747063289154,
          "id": "0003",
          "params": {"SERVICE_PORT": "8081"},
          "benchmark": "hello",
          "errors": []
        }
      },
      "APP_JAVA_PID": 118979,
      "start-sut": {"id=88": {}},
      "start-driver": {"id=94": {}}
    },
    "HF_BENCHMARK_NAME": "hello",
    "HF_BENCHMARK_REF": "\${{HF_LOCAL_DIR}}/\${{HF_BENCHMARK_NAME}}\${{HF_BENCHMARK_EXTENSION}}",
    "APP_URL": "https://github.com/lampajr/webhook-umb-example.git",
    "CONTAINER_RUNTIME": "podman",
    "HF_BENCHMARK_PARAMS": "",
    "APP_TAG_NAME": "getting-started",
    "HF_BENCHMARK_EXTENSION": ".hf.yaml",
    "HF_REPORT_ALL_ENABLED": "true",
    "LOAD_DRIVER_SERVER": "LOCAL",
    "APP_LOGS_FILE": "/tmp/\${{APP_TAG_NAME}}.log",
    "APP_FOLDER": "/tmp/quarkus_app",
    "APP_TAG_VERSION": "0.0.1-SNAPSHOT",
    "HF_LOCAL_DIR": "/tmp/hf-benchmarks",
    "HF_REPORT_ALL": "",
    "APP_TAG_GROUP": "test",
    "APP_PORT": "8081",
    "APP_COMMIT": "dev",
    "REPO_FULL_NAME": "lampajr/webhook-umb-example",
    "PULL_REQUEST_NUMBER": "1"
  }
}
"""
pipeline {
    agent {
        label "lab1"
    }
    options {
        timeout(time: 15, unit: 'MINUTES')
    }
    parameters {
        string(
            name: 'PULL_REQUEST_NUMBER',
            defaultValue: '1',
            description: 'Pull request number',
        )
    }
    stages {
        stage('run') {
            steps {
                script {
                    writeFile (file: "${WORKSPACE}/run.json", text: run)
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
