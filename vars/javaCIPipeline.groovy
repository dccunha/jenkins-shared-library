def call(body) {

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
            stage('Unit Test') {
                steps {
                    echo 'Stage - Unit Test'
                    withMaven(globalMavenSettingsConfig: config.mavenSettingsConfig,
                              mavenSettingsConfig: config.mavenSettingsConfig) {
                        devfactory(portfolio: 'TestPFAurea', product: 'Symphony', types: 'Java') {
                            sh 'mvn clean test'
                        }
                    }
                }
            }
        }
    }
}
