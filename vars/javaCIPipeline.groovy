def call(body) {

    def config = [:]
    def utils  = new symphony.java.Utils()

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
            stage('Unit Test') {
                steps {
                    echo 'Stage - Unit Test'
                    withMaven(globalMavenSettingsConfig: config.mavenSettingsConfig,
                              mavenSettingsConfig: config.mavenSettingsConfig) {
                        devfactory(portfolio: 'TestPFAurea', types: 'Java', product: 'Symphony',
                                   productVersion: utils.imageName("${JOB_NAME}")) {
                            sh 'mvn clean test'
                        }
                    }
                }
            }
        }
    }
}
