/*
	forma de invocación de método call:
	def ejecucion = load 'script.groovy'
	ejecucion.call()
*/

def call(){
  
  pipeline {
    agent any
    environment {
       STAGE = ''
    }
    parameters {
        choice choices: ['gradle', 'maven'], description: 'Indicar herramienta de construccion', name: 'buildTool'
    }   

    stages {
        stage('Pipeline') {
            steps {
                script {
                     
                        if (params.buildTool == 'gradle') {
                            gradle.call()
                        }
                        else{
                            maven.call()
                        }     
                }
            }
        }
    }
    post {
        success {
            slackSend channel: '#general', message: "Build Success: [Pablo Campos][${env.JOB_NAME}][${params.buildTool}]  Ejecución exitosa"
        }
        failure {
            slackSend channel: '#general', message: "Build Fail: [Pablo Campos][${env.JOB_NAME}][${params.buildTool}]  Ejecución fallida en stage ${STAGE}"
        }
    }
   }

}

return this;
