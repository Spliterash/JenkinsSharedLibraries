def archiveFilesWithoutPath(globPattern) {
  def files = findFiles(glob: globPattern)
  for (def i = 0; i < files.size(); i++) {
    def file = files[i]
    def parentFolder = new File(file.path).getParent()
    dir(parentFolder) {
      archiveArtifacts artifacts: file.name, fingerprint: true
    }
  }
}
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
      stage("Build") {
        steps {
          sh "sh gradlew clean build"
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
            archiveFilesWithoutPath(artifacts.join(","))
        }
      }
    }
  }
}
