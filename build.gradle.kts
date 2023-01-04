import com.mkobit.jenkins.pipelines.http.AnonymousAuthentication
import java.io.ByteArrayOutputStream

plugins {
  id("com.mkobit.jenkins.pipelines.shared-library") version "0.10.1"
  id("com.github.ben-manes.versions") version "0.21.0"
}

val commitSha: String by lazy {
  ByteArrayOutputStream().use {
    project.exec {
      commandLine("git", "rev-parse", "HEAD")
      standardOutput = it
    }
    it.toString(Charsets.UTF_8.name()).trim()
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  val spock = "org.spockframework:spock-core:1.2-groovy-2.4"
  testImplementation(spock)
  testImplementation("org.assertj:assertj-core:3.12.2")
  integrationTestImplementation(spock)
}

jenkinsIntegration {
  baseUrl.set(uri("https://jenkins.spliterash.ru").toURL())
  authentication.set(providers.provider { AnonymousAuthentication })
  downloadDirectory.set(layout.projectDirectory.dir("jenkinsResources"))
}

sharedLibrary {
  // TODO: this will need to be altered when auto-mapping functionality is complete
  coreVersion.set(jenkinsIntegration.downloadDirectory.file("core-version.txt").map { it.asFile.readText().trim() })
  // TODO: retrieve downloaded plugin resource
  pluginDependencies {
    dependency("org.jenkins-ci.plugins", "pipeline-build-step", "2.18")
    val declarativePluginsVersion = "2.2118.v31fd5b_9944b_5"
    dependency("org.jenkinsci.plugins", "pipeline-model-api", declarativePluginsVersion)
    dependency("org.jenkinsci.plugins", "pipeline-model-declarative-agent", "1.1.1")
    dependency("org.jenkinsci.plugins", "pipeline-model-definition", declarativePluginsVersion)
    dependency("org.jenkinsci.plugins", "pipeline-model-extensions", declarativePluginsVersion)
  }
}
