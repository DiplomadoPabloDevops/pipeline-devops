def call(String[] insertStages){

        String stageBuild = 'build'
        String stageSonar = 'sonar'
        String stageRun = 'run'
        String stageTestRun = 'test run'
        String stageNexus = 'nexus'

        
        String[] stages = [
            stageBuild,
            stageSonar,
            stageRun,
            stageTestRun,
            stageNexus
        ]

        String[] insertStages = []

        if (insertStages.size() == 1 && insertStages[0] == '') {
            runStages = stages
        } else {
            runStages = insertStages
        }

        if (stages.findAll { e -> runStages.contains( e ) }.size() == 0) {
            throw new Exception('Una o mas stage no pertenecen a los stages validos. inserte alguno de estos Stages validos: ' + stages.join(', ') + '. Recibe: ' + runStages.join(', '))
        }
        if (runStages.contains(stageBuild)) {
            stage('Build and Test') {
                STAGE = STAGE_NAME
                bat  "./gradlew clean build"           
            }
        }

        if (runStages.contains(stageSonar)) {
            stage('SonarQube analysis 2') {
                STAGE = STAGE_NAME
                def scannerHome = tool 'sonar-scanner';
                withSonarQubeEnv('sonar-scanner') {
                    bat "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=ejemplo-maven2 -Dsonar.sources=src/main/java/  -Dsonar.java.binaries=build -Dsonar.projectBaseDir=${env.WORKSPACE} -Dsonar.login=8e8236752890bf7bb18bc071593360e27a3d0346"
                }
            }
        }

        if (runStages.contains(stageRun)) {
            stage('Run') {
                STAGE = STAGE_NAME
                bat  "start /min gradlew bootRun &"
                bat "ping 127.0.0.1 -n 6 > nul"
                
            }
        }
        if (runStages.contains(stageTestRun)) {
            stage('Test') {
                STAGE = STAGE_NAME
                bat  'curl -X GET "http://localhost:8081/rest/mscovid/test?msg=testing"'
            }
        }
        if (runStages.contains(stageNexus)) {
            stage('Nexus Upload') {
                STAGE = STAGE_NAME
                nexusPublisher nexusInstanceId: 'nexus_test', nexusRepositoryId: 'test-repo', 
                packages: [[$class: 'MavenPackage', 
                mavenAssetList: [[classifier: '', extension: '', filePath: "${env.WORKSPACE}/build/libs/DevOpsUsach2020-0.0.1.jar"]], 
                mavenCoordinate: [artifactId: 'DevOpsUsach2020', 
                groupId: 'com.devopsusach2020', 
                packaging: 'jar', 
                version: '0.0.1']
                ]]
                
            }
        }
}
return this;