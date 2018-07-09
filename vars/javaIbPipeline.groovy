def call(body) {
    // evaluate the body block, and collect configuration into the object
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    pipeline {
        agent {
            node {
                label config.nodeLabel
            }
        }

        options { timestamps() }

        stages {
            stage('Integration Test') {
                steps {
                    echo 'Stage - Integration Test'
                    withMaven(globalMavenSettingsConfig: config.mavenSettingsConfig,
                              mavenSettingsConfig: config.mavenSettingsConfig) {
                        sh 'mvn clean verify'
                    }
                }
            }

            stage('Build Image') {
                environment {
                    PROJECT_VERSION = sh(
                        returnStdout: true,
                        script: 'awk -F \'=\' \'/version/ { print $2 }\' ./target/maven-archiver/pom.properties'
                    ).trim()
                    IMAGE_NAME = "${JOB_NAME}".tokenize('/').last()
                    IMAGE_TAG = "ib-${PROJECT_VERSION}.${BUILD_ID}"
                }

                steps {
                    script {
                        stage('Build Image') {
                            echo 'Stage - Build Image'
                            echo "${IMAGE_NAME}"
                            dockerImage = docker.build("${IMAGE_NAME}")
                        }

                        stage('Push Image') {
                            echo 'Stage - Push Image'
                            docker.withRegistry(config.registry) {
                                dockerImage.push("${IMAGE_TAG}")
                            }
                        }
                    }
                }
            }
        }
    }
}
