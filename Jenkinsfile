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
        VERSION = sh(returnStdout: true, script: 'gradle properties -q | grep "baseVersion:" | sed -E "s/baseVersion: (.*)/\\1/g"').trim()
        NS = 'openanalytics'
        REG = '196229073436.dkr.ecr.eu-west-1.amazonaws.com'
        TAG = 'develop'
        DOCKER_BUILDKIT = '1'
    }

    stages {
        stage('build integration test images') {
            matrix {
                axes {
                    axis {
                        name 'MODULE'
                        values 'app', 'repo', 'ldap'
                    }
                }
                stages {
                    stage('Docker build') {
                        steps {
                            withOARegistry {
                                sh """
                                    docker build --build-arg BUILDKIT_INLINE_CACHE=1 \
                                        --cache-from ${env.REG}/${env.NS}/rdepot-${MODULE}-it:${env.TAG} \
                                        -t ${env.NS}/rdepot-${MODULE}-it:${env.TAG} \
                                        ./src/integration-test/resources/docker/${MODULE}
                                """
                                ecrPush "${env.REG}", "${env.NS}/rdepot-${MODULE}-it", "${env.TAG}", '', 'eu-west-1'
                            }
                        }
                    }
                }
            }
        }

        stage('build and test') {
            steps {
                sh "gradle build"
            }
            post {
                success {
                    archiveArtifacts 'app/build/libs/rdepot-app-*.war,repo/build/libs/rdepot-repo-*.jar'
                    withCredentials([usernamePassword(credentialsId: 'oa-jenkins', usernameVariable: 'OA_NEXUS_USER', passwordVariable: 'OA_NEXUS_PWD')]) {
                        sh "gradle publish"         
                    }
                }
                always {
                    publishHTML([
                        reportDir: 'build/reports/tests/integrationTest', reportFiles: 'index.html',
                        reportName: 'Integration Test Report',
                        allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true])
                    publishHTML([
                        reportDir: 'app/build/reports/tests/test', reportFiles: 'index.html',
                        reportName: 'App Test Report',
                        allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true])
                    publishHTML([
                        reportDir: 'repo/build/reports/tests/test', reportFiles: 'index.html',
                        reportName: 'Repo Test Report',
                        allowMissing: true, alwaysLinkToLastBuild: true, keepAll: true])
                }
            }
        }

        stage('build Docker images and publish') {
            matrix {
                axes {
                    axis {
                        name 'MODULE'
                        values 'app', 'repo'
                    }
                }
                stages {
                    stage('build Docker images') {
                        steps {
                            withOARegistry {
                                sh """
                                docker build --build-arg BUILDKIT_INLINE_CACHE=1 \
                                    --cache-from ${env.REG}/${env.NS}/rdepot-${MODULE}:${env.VERSION} \
                                    -t ${env.NS}/rdepot-${MODULE}:${env.VERSION} \
                                    -t ${env.NS}/rdepot-${MODULE}:latest \
                                    -f ./docker/build/${MODULE}-standalone/Dockerfile \
                                    ./${MODULE}/build/libs
                                """
                                ecrPush "${env.REG}", "${env.NS}/rdepot-${MODULE}", "${env.VERSION}", '', 'eu-west-1'
                                ecrPush "${env.REG}", "${env.NS}/rdepot-${MODULE}", "latest", '', 'eu-west-1'
                            }
                        }
                    }
                    stage('publish') {
                        steps {
                            withDockerRegistry([
                                    credentialsId: "openanalytics-dockerhub",
                                    url: ""]) {
                                
                                sh "docker push ${env.NS}/rdepot-${MODULE}:${env.VERSION}"
                                sh "docker push ${env.NS}/rdepot-${MODULE}:latest"
                            }
                        }
                    }
                }
            }
        }
    }
}