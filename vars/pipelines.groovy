def call(){
    if(env.CHANGE_ID != null){
        pipeline{
            agent any
            stages {
                stage ("build") {
                    steps {
                        echo "Running build automation..."
                        sh './mvnw checkstyle:checkstyle'
                        sh './mvnw verify'
                        sh './mvnw clean package -DskipTests=true'
                    }
                }
                stage ("Build Docker Image") {
                    steps {
                        script{
                            app = docker.build("surtexx/mr:${GIT_COMMIT[0..7]}", "-f Dockerfile1 .")
                        }
                    }
                }
                stage ("Push Docker Image") {
                    steps {
                        script{
                            docker.withRegistry('https://registry.hub.docker.com', 'docker_hub_login') {
                                app.push("${GIT_COMMIT[0..7]}")
                                app.push("latest")
                            }
                        }
                    }
                }
            }
        }
    }
    else if(env.BRANCH_NAME == "main"){
        pipeline{
            agent any
            stages{
                stage ("Build Docker Image") {
                    steps {
                        script{
                            app = docker.build("surtexx/main", "-f Dockerfile1 .")
                        }
                    }
                }
                stage ("Push Docker Image") {
                    steps {
                        script{
                            docker.withRegistry('https://registry.hub.docker.com', 'docker_hub_login') {
                                app.push("latest")
                            }
                        }
                    }
                }
            }
        }
    }
}
