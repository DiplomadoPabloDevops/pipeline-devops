void call() {
    pipeline {
        agent any
        environment {
            CURRENT_STAGE = ''
            NEXUS_INSTANCE_ID = 'nexus_test'
            NEXUS_REPOSITORY = 'test-repo'
        }

        parameters {
            choice choices: ['gradle', 'maven'], description: 'indicar la herramienta de construcción', name: 'buildTool'
            string defaultValue: '', description: 'Stages a ejecutar', name: 'stage'
        }
        stages {
            stage('pipeline') {
                steps {
                    script {
                        if (params.buildTool == 'maven') {
                            maven.call(getStepsToRun(), getPipelineType())
                        } else {
                            gradle.call(getStepsToRun(), getPipelineType())
                        }
                    }
                }
            }
        }
        post {
            success {
                slackSend(color: '#00FF00', message: '[Pablo Campos][' + env.JOB_NAME + '][' + params.buildTool + '] Ejecución Exitosa.')
            }
            failure {
                slackSend(color: '#FF0000', message: '[Pablo Campos][' + env.JOB_NAME + '][' + params.buildTool + '] Ejecución Fallida en Stage [' + STAGE + '].')
            }
        }
    }
}

String[] getStepsToRun() {
    String[] stepsToRun = params.stage.split(';')
    return stepsToRun
}

String getPipelineType() {
    if (env.GIT_BRANCH.contains('feature-') || env.GIT_BRANCH.contains('develop')) {
        return 'CI'
    } else {
        return 'CD'
    }
}

return this