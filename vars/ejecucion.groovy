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
        string defaultValue: '', description: 'Stages a ejecutar, separar por ;', name: 'stage'
    }   

    stages {
        stage('Pipeline') {
            steps {
                script {
                    String[] stepsChoices = params.stage.split(';')
                    if (params.buildTool == 'gradle') {
                        gradle.call(stepsChoices)
                    }
                    else{
                        maven.call(stepsChoices)
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
