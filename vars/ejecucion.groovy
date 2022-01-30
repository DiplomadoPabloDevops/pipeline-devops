/*
	forma de invocación de método call:
	def ejecucion = load 'script.groovy'
	ejecucion.call()
*/

def call(){
  
  pipeline {
    agent any
    parameters {
        choice choices: ['gradle', 'maven'], description: 'Indicar herramienta de construcciÃ³n', name: 'buildTool'
    }   
    environment{
        STAGE = ''
            
    }
    stages {
        stage('Pipeline') {
            steps {
                script {
                        println('Pipeline')
                        println params.buildTool
                        if (params.buildTool == 'gradle') {
                            def ejecucion = load 'gradle.groovy'
                             ejecucion.call()
                        }
                        else{
                            def ejecucion = load 'maven.groovy'
                            ejecucion.call()
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
