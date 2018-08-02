package tpl.ci.node
import tpl.ci.tplBaseCiPipeline
import tpl.services.Deployer

class tplNodeCiPipeline extends tplBaseCiPipeline{

 tplNodeCiPipeline(script){
        super(script)
 }

@Override
    void setup() {
        gitConfig()


        // automatically capture environment variables while downloading and uploading files
    }

    
@Override
    void checkout() {
        script.checkout script.scm
    }
 @Override
    void build() {


        script.dir("${script.env.WORKSPACE}") {
            script.withCredentials([script.usernamePassword(credentialsId: 'dockerHub', passwordVariable: 'DOCKER_REGISTRY_PASS', usernameVariable: 'DOCKER_REGISTRY_USER')]) {

                
                script.sh "npm install"
                script.sh "npm run build"
                script.docker.withRegistry('https://index.docker.io/v1/tikalk','dockerHub') {
                        def customImage = script.docker.build("${script.env.DOCKER_REPOSITORY}","./docker/src/${script.env.DOCKER_GROUP}/${script.env.DOCKER_REPOSITORY}")
                        /* Push the container to the custom Registry */
                        customImage.push()
                    }
        
    
            }

        }
            
    }     
@Override
    void deploy() {
        logger.info "Helm Deploy"
        def deployer = new Deployer(script,script.scm.branchName,serviceTag)
        deployer.deploy()
    }



    void gitConfig() {
   }

    void gitCommit() {

    }



    void buildNotifier() {

        def subject = script.env.JOB_NAME + ' - Build #' + script.currentBuild.number + ' - ' + script.currentBuild.currentResult
        script.emailext(
                to: 'user@domain.com',
                subject: subject,
                body: script.env.BUILD_URL
//                attachLog: true,
                //recipientProviders: [[$class: 'DevelopersRecipientProvider']]
        )
    }
}
