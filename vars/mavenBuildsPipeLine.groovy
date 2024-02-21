#!groovy​
import com.jenkins.*

def call() {
    //LinkedHashMap config = [:]
    //body.resolveStrategy = Closure.DELEGATE_FIRST
    //body.delegate = config
    //body()

    def props = new Properties(libraryResource('config.properties'))
    def maven = props['maven']
    def cron = props['cron']
    def label = props['label']
    

    pipeline {

        triggers {
            script {
                cron(cron)
            }
        }
        script {
        agent { label label }
        }

        stages {

            stage('Compile/Test/Install') {
                steps {
                    script {
                        new MavenBuild(this, 'maven3').callMaven("clean install")
                    }
                }
            }

            stage('Deploy') {
                steps {
                    script {
                        new MavenBuild(this, 'maven3').callMaven('deploy -Dmaven.test.skip=true')
                    }
                }
            }

            stage('Archive artifacts') {
                steps {
                    archiveArtifacts artifacts: '**/*.jar', allowEmptyArchive: true
                    archiveArtifacts artifacts: 'target/surefire-reports/*.xml', allowEmptyArchive: true
                }
            }
        }
    }
}