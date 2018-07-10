def call(body) {

    def config = [:]
    def utils = new symphony.java.Utils()

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
            stage('Build Package') {
                steps {
                    echo 'stage - Build Package'
                    withMaven(globalMavenSettingsConfig: config.mavenSettingsConfig,
                              mavenSettingsConfig: config.mavenSettingsConfig) {
                        sh 'mvn -DskipTests clean package'
                    }
                }
            }

            stage('Build Image') {
                environment {
                    PROJECT_VERSION = utils.projectVersion()
                    IMAGE_NAME = utils.imageName("${JOB_NAME}")
                    IMAGE_TAG = utils.imageTag('rc', "${BUILD_ID}")
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
