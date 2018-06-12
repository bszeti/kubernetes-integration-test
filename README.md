# Running integration tests with Kubernetes

Integration test on Kubernetes with Jenkins pipeline

### Directories:
* **app-users**: Example Red Hat Fuse 7 application that takes messages from AMQ, makes database queries and calls a REST api
* **configuration/settings.xml**: Maven settings.xml with the requires repos to build
* **integration-test**: Tests written in Java with Junit, can be executed with Maven

### Jenkins pipelines:
* **Jenkinsfile**: Builds app and executes integration test.
* **Jenkinsfile-basic**: Executes integration test only. Setting only the important parameters.
* **Jenkinsfile-declarative**: Using declarative syntax. Setting a different agent at stage level. Builds app and executes integration test.
* **Jenkinsfile-declarative-one-agent**: Using declarative syntax. Agent is defined at the pipeline level. Executes integration test only.
* **Jenkinsfile-jnlp-base**: Using separate container for jnlp and maven. Executes integration test only.
* **Jenkinsfile-mavenlocalrepo**: Mounting a persisted volume as maven local repository to avoid downloading jars every time. Requires a 'mavenlocalrepo' persistent volume claim. Builds app and executes integration test.
* **Jenkinsfile-yaml**: Reads yaml pod template from 'pod.yaml'. Builds app and executes integration test.
