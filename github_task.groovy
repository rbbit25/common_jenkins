def k8slabel = "jenkins-pipeline-${UUID.randomUUID().toString()}"
def slavePodTemplate = """
      metadata:
        labels:
          k8s-label: ${k8slabel}
        annotations:
          jenkinsjoblabel: ${env.JOB_NAME}-${env.BUILD_NUMBER}
      spec:
        affinity:
          podAntiAffinity:
            requiredDuringSchedulingIgnoredDuringExecution:
            - labelSelector:
                matchExpressions:
                - key: component
                  operator: In
                  values:
                  - jenkins-jenkins-master
              topologyKey: "kubernetes.io/hostname"
        containers:
        - name: fuchicorptools
          image: fuchicorp/buildtools:latest
          imagePullPolicy: IfNotPresent
          command:
          - cat
          tty: true
          volumeMounts:
            - mountPath: /var/run/docker.sock
              name: docker-sock
        serviceAccountName: default
        securityContext:
          runAsUser: 0
          fsGroup: 0
        volumes:
          - name: docker-sock
            hostPath:
              path: /var/run/docker.sock
    """
    properties([
        parameters([
            choice(choices: ['fsadykov', 'antonbabenko', 'mojombo', 'defunkt'], 
            description: 'please select github username', name: 'Github_users')
        ])
        ])
    podTemplate(name: k8slabel, label: k8slabel, yaml: slavePodTemplate, showRawYaml: false) {
      node(k8slabel) {
        withCredentials([string(credentialsId: 'github_token', variable: 'GIT_TOKEN')]) {
      stage("Get User") {
            println("Selected Github user is: ${Github_users}")
            println('Building the github-task')  
            container("fuchicorptools") {
                sh "curl -H  \"Authorization: token $GIT_TOKEN \" -X GET 'https://api.github.com/users/${Github_users}'"
            }
        }
      stage("Check Creds") {
           container("fuchicorptools") {
                sh 'curl -H "Authorization: token $GIT_TOKEN " -X GET "https://api.github.com/users" -I | grep "HTTP/1.1 200 OK" '
                }
            }
      }                         
     }
    }