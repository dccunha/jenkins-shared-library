package symphony.java;

def projectVersion() {
    sh(
        returnStdout: true,
        script: 'awk -F \'=\' \'/version/ { print $2 }\' ./target/maven-archiver/pom.properties'
    ).trim()
}

def imageName(String jobName) {
    "${jobName}".tokenize('/').last()
}

def imageTag(String buildType, String buildId) {
    "${buildType}-${projectVersion()}.${buildId}"
}

return this
