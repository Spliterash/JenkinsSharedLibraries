def call(Map params = [:]) {
  def jdkVersion = params.containsKey("jdk") ? params.jdk : 17
  def imageName = "amazoncorretto:${jdkVersion}"
  def artifacts = params.containsKey("artifacts") ? params.artifacts : []

  pipeline {
    agent {
      docker { image imageName }
    }

    stages {
      stage("Build") {
        steps {
          sh "sh gradlew build"
        }
      }
      stage("Publish") {
        steps {
          sh "sh gradlew publish"
        }
      }
    }
    post {
      for (artifact in artifacts) {
        success {
          archiveArtifacts artifacts: artifact, fingerprint: true
        }
      }
    }
  }
}
