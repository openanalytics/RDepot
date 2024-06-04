pipeline {
  agent {
    kubernetes {
      yamlFile 'kubernetesPod.yaml'
      defaultContainer 'rdepot-build'
    }
  }
  options {
    buildDiscarder(logRotator(numToKeepStr: '3'))
  }
  environment {
    VERSION = sh(returnStdout: true, script: 'mvn help:evaluate -Dexpression=project.version -q -DforceStdout').trim()
    NS = 'openanalytics'
    DOCKER_BUILDKIT = '1'
  }
  stages {
    stage('license check') {
      steps {
        sh "mvn com.mycila:license-maven-plugin:check"
      }
    }
    stage('static code analysis') {
      steps {
        sh "mvn clean install -DskipTests -Ddependency-check.skip=true -Perrorprone com.github.spotbugs:spotbugs-maven-plugin:check"
        sh "mvn pmd:pmd"
      }
    }
    stage('build test images and publish') {
      matrix {
        axes {
          axis {
            name 'MODULE'
            values 'app', 'repo'
          }
        }
        stages {
          stage('build') {
            steps {
              withDockerRegistry([
                  credentialsId: "oa-sa-jenkins-registry",
                  url: "https://registry.openanalytics.eu"]) {
                sh """
                  docker build --build-arg BUILDKIT_INLINE_CACHE=1 \
                      --cache-from registry.openanalytics.eu/${env.NS}/rdepot-${MODULE}-it:${env.VERSION} \
                      -t registry.openanalytics.eu/${env.NS}/rdepot-${MODULE}-it:${env.VERSION} \
                      ./rdepot-app/src/test/resources/docker/${MODULE}
                """
              }
            }
          }
          stage('publish') {
            steps {
              withDockerRegistry([
                  credentialsId: "oa-sa-jenkins-registry",
                  url: "https://registry.openanalytics.eu"]) {
                sh "docker push registry.openanalytics.eu/${env.NS}/rdepot-${MODULE}-it:${env.VERSION}"
              }
            }
          }
        }
      }
    }
    stage('tests') {
      steps {
        withDockerRegistry([
            credentialsId: "oa-sa-jenkins-registry",
            url: "https://registry.openanalytics.eu"]) {


	      configFileProvider([configFile(fileId: 'maven-settings-rsb', variable: 'MAVEN_SETTINGS_RSB')]) {
	        sh "mvn -s $MAVEN_SETTINGS_RSB clean deploy -Ddependency-check.skip=true"
	      }
	      publishHTML([
	        reportDir: 'rdepot-app/target/site/', reportFiles: 'failsafe-report.html',
	        reportName: 'Integration Test / App Test Report',
	        allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true])
	      publishHTML([
	        reportDir: 'rdepot-repo/target/site/', reportFiles: 'surefire-report.html',
	        reportName: 'Repo Test Report',
	        allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true])
	      publishHTML([
	        reportDir: 'rdepot-r-module/target/site/', reportFiles: 'surefire-report.html',
	        reportName: 'R Module Test Report',
	        allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true])
	      publishHTML([
	        reportDir: 'rdepot-python-module/target/site/', reportFiles: 'surefire-report.html',
	        reportName: 'Python Module Test Report',
	        allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true])
	      // publishHTML([
	      //   reportDir: 'target/', reportFiles: 'dependency-check-report.html',
	      //   reportName: 'OWASP Dependency Analysis Report',
	      //   allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true])
        }
      }

    }
    stage('build images and publish') {
      matrix {
        axes {
          axis {
            name 'MODULE'
            values 'app', 'repo'
          }
        }
        stages {
          stage('build') {
            steps {
              withDockerRegistry([
                  credentialsId: "oa-sa-jenkins-registry",
                  url: "https://registry.openanalytics.eu"]) {
                sh """
                  docker build --build-arg BUILDKIT_INLINE_CACHE=1 \
                      --cache-from registry.openanalytics.eu/${env.NS}/rdepot-${MODULE}:${env.VERSION} \
                      -t registry.openanalytics.eu/${env.NS}/rdepot-${MODULE}:${env.VERSION} \
                      -t registry.openanalytics.eu/${env.NS}/rdepot-${MODULE}:latest \
                      -f ./docker/build/${MODULE}-standalone/Dockerfile \
                      ./
                """
              }
            }
          }
          stage('publish') {
						when {
							anyOf {
								branch 'develop'
								branch 'master'
							}
						}
            steps {
              withDockerRegistry([
                  credentialsId: "oa-sa-jenkins-registry",
                  url: "https://registry.openanalytics.eu"]) {
                sh "docker push registry.openanalytics.eu/${env.NS}/rdepot-${MODULE}:${env.VERSION}"
                sh "docker push registry.openanalytics.eu/${env.NS}/rdepot-${MODULE}:latest"
              }
            }
          }
        }
      }
    }
  }
}
