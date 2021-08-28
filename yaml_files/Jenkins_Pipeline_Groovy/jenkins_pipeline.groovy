def call (String BRANCH_NAME, String NAMESPACE, String SERVICE_NAME, String DOCKER_FILE_DIR) {
  pipeline {
     environment {
         docker = "DOCKER_REPO"
         registry = "192.168.100.220:30003/docker-registry/" + "$NAMESPACE/" + "$SERVICE_NAME"
         registryCredential = "Nexus-Cred"
         dockerImage = ''
         path = ''
     }
     agent any
     stages {
       stage('Building our image') { 
         steps { 
           script { 
              def version = readFile('VERSION')
              patch = version.trim()
              patch = patch + ".$BUILD_NUMBER"
              echo version
              echo patch               
              
              if (BRANCH_NAME == 'master') {
                    dockerImage = docker.build registry + ":$patch", "$DOCKER_FILE_DIR"
              }      
           }
         } 
       }  
       stage('Deploy our image') { 
         steps { 
           script { 
              docker.withRegistry( 'http://192.168.100.220:30003/docker-registry/', registryCredential ) 
                 {
                   dockerImage.push(patch)
                 }
           } 
         }
        } 
        stage('Cleaning up') { 
         steps { 
           sh "docker rmi $registry:$patch" 
         }
        }  
     }          
  }   
}
