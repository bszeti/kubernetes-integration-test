podTemplate(
  name: 'app-users-it',
  label: 'app-users-it',
  cloud: 'openshift',
  containers: [
    //Java agent, test executor
    containerTemplate(name: 'jnlp',
                      image: 'registry.access.redhat.com/openshift3/jenkins-slave-maven-rhel7:v3.9',
                      resourceLimitMemory: '512Mi',
                      workingDir: '/home/jenkins',
                      command: '/bin/sh -c',
                      args: '"umask 0000; /usr/local/bin/run-jnlp-client ${computer.jnlpmac} ${computer.name}"',
                      alwaysPullImage: false,
                      envVars: [
                        //Heap for jnlp is 1/2, for mvn and surefire process is 1/4 of resourceLimitMemory by default
                        envVar(key: 'JNLP_MAX_HEAP_UPPER_BOUND_MB', value: '64')
                      ]),
    //App under test
    containerTemplate(name: 'app-users',
                      image: '172.30.1.1:5000/myproject/app-users:latest',
                      resourceLimitMemory: '512Mi',
                      alwaysPullImage: true,

                      livenessProbe: '/bin/bash -c "date >>/home/jenkins/starttimes; sleep 60; date >>/home/jenkins/starttimes"',
                      readinessProbe2: '/bin/bash -c "date >>/home/jenkins/starttimes; sleep 60; date >>/home/jenkins/starttimes"',
                      envVars: [
                        envVar(key: 'SPRING_PROFILES_ACTIVE', value: 'k8sit'),
                        envVar(key: 'SPRING_CLOUD_KUBERNETES_ENABLED', value: 'false')
                      ]),
    //DB
    containerTemplate(name: 'mariadb',
                      image: 'registry.access.redhat.com/rhscl/mariadb-102-rhel7',
                      resourceLimitMemory: '256Mi',
                      alwaysPullImage: false,
                      envVars: [
                        envVar(key: 'MYSQL_USER', value: 'myuser'),
                        envVar(key: 'MYSQL_PASSWORD', value: 'mypassword'),
                        envVar(key: 'MYSQL_DATABASE', value: 'testdb'),
                        envVar(key: 'MYSQL_ROOT_PASSWORD', value: 'secret')
                      ]),
    //AMQ
    containerTemplate(name: 'amq',
                      image: 'registry.access.redhat.com/jboss-amq-6/amq63-openshift',
                      resourceLimitMemory: '256Mi',
                      alwaysPullImage: false,
                      readinessProbe: '/bin/bash -c /opt/amq/bin/readinessProbe.sh',
                      envVars: [
                        envVar(key: 'AMQ_USER', value: 'test'),
                        envVar(key: 'AMQ_PASSWORD', value: 'secret')
                      ]),
    //External API Third party (provided by mockserver)
    containerTemplate(name: 'mockserver',
                      image: 'jamesdbloom/mockserver',
                      resourceLimitMemory: '256Mi',
                      alwaysPullImage: false,
                      envVars: [
                        envVar(key: 'LOG_LEVEL', value: 'INFO'),
                        envVar(key: 'JVM_OPTIONS', value: '-Xmx128m'),
                      ])
    ],
  volumes:[
    configMapVolume(mountPath: '/home/jenkins/.m2', configMapName: 'maven-settings'),
    persistentVolumeClaim(mountPath: '/home/jenkins/maven', claimName:'mavenlocal') //mounted as root drwxrwxrwx
    ]
    )
{ /*
    node('maven'){
        stage('Pull source') {
            checkout scm //git url: 'https://github.com/bszeti/kubernetes-integration-test.git'
        }

        dir ("app-users") {
            stage('Build app') {
                sh "mvn -B -DskipTests package"
            }

            stage('Build Image') {
                // Requires: minishift config set insecure-registry 172.30.0.0/16
                sh "oc new-build --name=app-users --docker-image=registry.access.redhat.com/fuse7/fuse-java-openshift:latest --binary=true --labels=app=app-users || true"
                sh 'rm -rf oc-build && mkdir -p oc-build/deployments'
                sh 'cp target/*.jar oc-build/deployments'

                openshift.withCluster() {
                    openshift.withProject('myproject') {
                        openshift.selector('bc', 'app-users').startBuild('--from-dir=oc-build', '--wait=true').logs('-f')
                    }

                }
            }
        }

    } */

    node('app-users-it') {
        stage('Pull source') {
          sh 'env'
          sh 'cat /home/jenkins/starttimes'
          checkout scm
        }
        dir ("integration-test") {
            stage('Prepare test') {
                container('app-users') {
                    //requires mysql
                    sh 'cat /tmp/starttime'
                }
                container('mariadb') {
                    //requires mysql
                    sh 'sql/setup.sh'
                }
                container('jnlp') {
                    //requires curl and python
                    sh 'mockserver/setup.sh'
                }
            }

            withEnv(['AMQ_USER=test',
                     'AMQ_PASSWORD=secret']) {
                stage('Build and run test') {
                    try {
                        sh 'mvn -B clean test'
                    } finally {
                        junit 'target/surefire-reports/*.xml'
                    }
                }
            }
        }
    }
}
