def call(Map params = [:]) {
  def jdkVersion = params.containsKey("jdk") ? params.jdk : 17
  def imageName = "amazoncorretto:${jdkVersion}"
  def artifacts = params.containsKey("artifacts") ? params.artifacts : []

  pipeline {
    agent {
      docker {
        image imageName
        args '-v $HOST_HOME/.gradle:/root/.gradle -v $HOST_HOME/.m2:/root/.m2'
      }
    }
    environment {
      ORG_GRADLE_PROJECT_SPLITERASH_NEXUS = credentials("spliterash-repo")
    }

    stages {
      stage("Prepare") {
        steps {
          vkSendStart()
        }
      }
      stage("Publish") {
        steps {
          sh "sh gradlew clean publish"
        }
      }
    }
    post {
      always {
        vkSendEnd()
      }
      success {
        script {
          if (!artifacts.isEmpty())
            archiveArtifacts artifacts: artifacts.join(","), fingerprint: true
        }
      }
    }
  }
}
