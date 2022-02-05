void call(String[] stagesToRun, String pipelineType) {

    figlet 'Maven'
    figlet pipelineType

    if (pipelineType == 'CI') {
        runCi(stagesToRun)
    } else if (pipelineType == 'CD') {
        runCd(stagesToRun)
    } else {
        throw new Exception('PipelineType Inválido: ' + pipelineType)
    }
}

void runCd(String[] stagesToRun) {
    String downloadNexus = 'downloadNexus'
    String runDownloadedJar = 'runDownloadedJar'
    String rest = 'rest'
    String nexusCD = 'nexusCD'
    String gitdiff = 'gitdiff'

    String[] stages = [
        downloadNexus,
        runDownloadedJar,
        rest,
        nexusCD,
        gitdiff
    ]

    String[] currentStages = []

    if (stagesToRun.size() == 1 && stagesToRun[0] == '') {
        currentStages = stages
    } else {
        currentStages = stagesToRun
    }

    if (stages.findAll { e -> currentStages.contains( e ) }.size() == 0) {
        throw new Exception('Al menos una stage es inválida. Stages válidas: ' + stages.join(', ') + '. Recibe: ' + currentStages.join(', '))
    }

    if (currentStages.contains(downloadNexus)) {
        stage(downloadNexus) {
            CURRENT_STAGE = downloadNexus
            figlet CURRENT_STAGE
            withCredentials([usernameColonPassword(credentialsId: 'nexus_test', variable: 'NEXUS_CREDENTIALS')]) {
                bat  "curl -X GET -u ${NEXUS_CREDENTIALS} http://localhost:8089/repository/test-repo/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar -O"
            }
        }
    }

    if (currentStages.contains(runDownloadedJar)) {
        stage(runDownloadedJar) {
            CURRENT_STAGE = runDownloadedJar
            figlet CURRENT_STAGE
            bat 'start /min java -jar DevOpsUsach2020-0.0.1.jar &'
            bat "ping 127.0.0.1 -n 6 > nul"
        }
    }

    if (currentStages.contains(rest)) {
        stage(rest) {
            CURRENT_STAGE = rest
            figlet CURRENT_STAGE
            bat  'curl -X GET "http://localhost:8081/rest/mscovid/test?msg=testing"'
        }
    }
    
    if (currentStages.contains('gitdiff')) {
        stage('gitdiff') {
            withCredentials([usernamePassword(credentialsId: 'github-password', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                bat "git checkout origin/main"
                bat "git merge origin/${env:BRANCH_NAME}"
                bat ("git push https://${GIT_USERNAME}:${GIT_PASSWORD}@${GIT_URL}")
            }

                bat "git config --add remote.origin.fetch +refs/heads/main:refs/remotes/origin/main"
                bat "git fetch --no-tags"
                bat " git diff origin/main origin/${env:BRANCH_NAME}"
                
        }
    }
        
    if (currentStages.contains(nexusCD)) {
        stage(nexusCD) {
            CURRENT_STAGE = nexusCD
            figlet CURRENT_STAGE
            nexusPublisher nexusInstanceId: NEXUS_INSTANCE_ID,
            nexusRepositoryId: NEXUS_REPOSITORY,
            packages: [
                [
                    $class: 'MavenPackage',
                    mavenAssetList: [
                        [classifier: '', extension: '', filePath: 'DevOpsUsach2020-0.0.1.jar']
                    ],
                    mavenCoordinate: [
                        artifactId: 'DevOpsUsach2020',
                        groupId: 'com.devopsusach2020',
                        packaging: 'jar',
                        version: '1.0.0'
                    ]
                ]
            ]
        }
    }
}


void runCi(String[] stagesToRun) {
    String stageBuild = 'buildAndTest'
    String stageSonar = 'sonar'
    String stageRun = 'runJar'
    String stageTestRun = 'rest'
    String stageNexus = 'nexusCI'

    String[] stages = [
        stageBuild,
        stageSonar,
        stageRun,
        stageTestRun,
        stageNexus
    ]

    String[] currentStages = []

    if (stagesToRun.size() == 1 && stagesToRun[0] == '') {
        currentStages = stages
    } else {
        currentStages = stagesToRun
    }

    if (stages.findAll { e -> currentStages.contains( e ) }.size() == 0) {
        throw new Exception('Al menos una stage es inválida. Stages válidas: ' + stages.join(', ') + '. Recibe: ' + currentStages.join(', '))
    }

    if (currentStages.contains(stageBuild)) {
        stage(stageBuild) {
            CURRENT_STAGE = stageBuild
            figlet CURRENT_STAGE
            bat  "./mvnw.cmd clean compile -e"
            bat  "./mvnw.cmd clean test -e"
            bat  "./mvnw.cmd clean package -e"
        }
    }

    if (currentStages.contains(stageSonar)) {
        stage(stageSonar) {
            CURRENT_STAGE = stageSonar
            figlet CURRENT_STAGE
            def scannerHome = tool 'sonar-scanner';
            withSonarQubeEnv('sonar-scanner') {
                bat "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=ejemplo-maven2 -Dsonar.sources=src/main/java/  -Dsonar.java.binaries=build -Dsonar.projectBaseDir=${env.WORKSPACE} -Dsonar.login=8e8236752890bf7bb18bc071593360e27a3d0346"
            }
        }
    }

    if (currentStages.contains(stageRun)) {
        stage(stageRun) {
            CURRENT_STAGE = stageRun
            figlet CURRENT_STAGE
            bat  "start /min mvnw.cmd spring-boot:run &"
            bat "ping 127.0.0.1 -n 6 > nul"
        }
    }

    if (currentStages.contains(stageTestRun)) {
        stage(stageTestRun) {
            CURRENT_STAGE = stageTestRun
            figlet CURRENT_STAGE
            bat  'curl -X GET "http://localhost:8081/rest/mscovid/test?msg=testing"'
        }
    }
    if (currentStages.contains(stageNexus)) {
        stage(stageNexus) {
            CURRENT_STAGE = stageNexus
            figlet CURRENT_STAGE
            nexusPublisher nexusInstanceId: NEXUS_INSTANCE_ID,
            nexusRepositoryId: NEXUS_REPOSITORY,
            packages: [
            [
                $class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '', extension: '', filePath: 'build/DevOpsUsach2020-0.0.1.jar']
                ],
                mavenCoordinate: [
                    artifactId: 'DevOpsUsach2020',
                    groupId: 'com.devopsusach2020',
                    packaging: 'jar',
                    version: '0.0.1'
                ]
            ]
            ]
        }
    }
}

return this
