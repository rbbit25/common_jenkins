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
            gitParameter(branch: '', branchFilter: 'origin/(.*)', 
            defaultValue: 'origin/version/0.1', description: 'Please go ahead  and select the version ', 
            name: 'release_name', quickFilterEnabled: false, selectedValue: 'NONE', 
            sortMode: 'NONE', tagFilter: 'origin/(.*)', type: 'PT_BRANCH', useRepository: 'https://github.com/fuchicorp/artemis')
            ])
            ])
    podTemplate(name: k8slabel, label: k8slabel, yaml: slavePodTemplate, showRawYaml: false) {
      node(k8slabel) {
        stage('Pull SCM') {
            git branch: "${params.release_name}", url: 'https://github.com/fuchicorp/artemis'
        }
        stage("Docker Build") {
            container("docker") {
                sh "docker build -t rbbit/artemis:${release_name.replace('version/', 'v')}  ."
            }
        }
        stage("Docker Login") {
            withCredentials([usernamePassword(credentialsId: 'docker-hub-creds', passwordVariable: 'password', usernameVariable: 'username')]) {
                container("docker") {
                    sh "docker login --username ${username} --password ${password}"
                }
            }
        }
        stage("Docker Push") {
            container("docker") {
                sh "docker push rbbit/artemis:${release_name.replace('version/', 'v')}"
            }
        }
      }
    }