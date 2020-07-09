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
        - name: terraform
          image: "hashicorp/terraform"
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
        - name: fuchicorptools
          image: fuchicorp/buildtools
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
            booleanParam(defaultValue: false, description: 'Please select to be able to see more info ', name: 'DebugMode'), 
            choice(choices: ['DEV', 'QA', 'STAGE', 'PROD'], description: 'Please select where to deploy', name: 'Environment')
            ])
        ])
    podTemplate(name: k8slabel, label: k8slabel, yaml: slavePodTemplate, showRawYaml: params.DebugMode) {
      node(k8slabel) {
        stage("Docker check") {
            container("docker") {
                sh 'docker --version'
            }
        }
        stage("Terraform Deploy") {
            container("terraform") {
                println("""
                Environment: ${params.Environment}
                """)
                sh 'terraform version'
            }
        }
        stage("Kubectl Deploy") {
            container("fuchicorptools") {
                sh 'kubectl version'
            }
        }
      }
    }
