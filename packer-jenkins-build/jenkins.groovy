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
        - name: packer
          image: hashicorp/packer:latest
          imagePullPolicy: IfNotPresent
          command:
          - cat
          tty: true
          volumeMounts:
            - mountPath: /var/run/docker.sock
              name: docker-sock
        - name: docker
          image: docker:latest
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
            choice(choices: [
                'us-west-2', 'us-west-1', 'us-east-2', 'us-east-1', 'eu-west-1'], 
                description: 'Please select the region to build the packer for.', name: 'aws_region')])
        ])
    podTemplate(name: k8slabel, label: k8slabel, yaml: slavePodTemplate, showRawYaml: false) {
      node(k8slabel) {
       
        stage("Pull SCM") {
            git 'https://github.com/rbbit25/evolvecyber-jenkins.git'
            println('Pulling the source code.')
        }
        dir('class4/packer/') {
         container('packer') {
            withCredentials([usernamePassword(credentialsId: 'packer-build-creds', passwordVariable: 'AWS_SECRET_KEY', usernameVariable: 'AWS_ACCESS_KEY')]) {
                stage("Packer Validate") {
                println('Validating the syntax code.') 
                sh 'packer validate -syntax-only jenkins.json'

                println('Validating the packer code.') 
                sh 'packer validate jenkins.json' 
            }
                stage("Packer Build") {
                println("Selected AWS region is: ${params.aws_region}")
                println('Validating the packer code')
                sh """
                #!/bin/bash
                export AWS_REGION=${aws_region}
                packer build jenkins.json
                """ 
            }
}
 
         }
      
       }
       
      }
    }