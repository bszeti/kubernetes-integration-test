# Running integration tests with Kubernetes

Integration test on Kubernetes with Jenkins pipeline

Blog post: https://itnext.io/running-integration-tests-with-kubernetes-ae0fb71e207b

### Directories:
* **app-users**: Example Red Hat Fuse 7 application that takes messages from AMQ, makes database queries and calls a REST api
* **configuration/settings.xml**: Maven settings.xml with the requires repos to build
* **integration-test**: Tests written in Java with Junit, can be executed with Maven

### Jenkins pipelines:
* **Jenkinsfile**: Builds app and executes integration test.
* **Jenkinsfile-basic**: Executes integration test only. Setting only the important parameters.
* **Jenkinsfile-declarative**: Using declarative syntax. Setting a different agent at stage level. Builds app and executes integration test.
* **Jenkinsfile-declarative-one-agent**: Using declarative syntax. Agent is defined at the pipeline level. Executes integration test only.
* **Jenkinsfile-declarative-runas**: Using declarative syntax. Pod sets runAsUser to make sure all containers are started with the same uid. No "umask 0000" is needed in the command. Requires 'anyuid' scc.
* **Jenkinsfile-declarative-restricted**: Using declarative syntax. Only works if 'restricted' scc is being used, so the same random uid is set for all containers.  No "umask 0000" is needed in the command.
* **Jenkinsfile-jnlp-base**: Using separate container for jnlp and maven. Executes integration test only.
* **Jenkinsfile-mavenlocalrepo**: Mounting a persisted volume as maven local repository to avoid downloading jars every time. Requires a 'mavenlocalrepo' persistent volume claim. Builds app and executes integration test.
* **Jenkinsfile-yaml**: Reads yaml pod template from 'pod.yaml'. Builds app and executes integration test.

### App-users
A Spring Boot application running a Camel route based on the Red Hat Fuse 7 stack.

Steps:
* Take messages from _user.in_ queue. Json :`{"email": "me@mycompany.com"}`
* Query phone number from table _users_
* Call a REST api to get address. E.g. https://myhost/v1/address/email/me@mycompany.com
* Send the enriched user info to queue _user.out_

Build and run unit tests with Maven:
* Use the provided settings.xml to access the required Maven repositories:

 `mvn -s ../configuration/settings.xml clean package`

### Integration-test

Tests written in Java using Junit5. Run with Maven: `mvn clean test`

Env variables required:
* AMQ_USER: AMQ connection username
* AMQ_PASSWORD: AMQ connection password

Test cases:
* testSucc: Send message to _user.in_. Expect enriched message on _user.out_.
* testRetry: Test AMQ transacted. REST api throws http 500 3x times before sending response. Send message to _user.in_.  Expect enriched message on _user.out_.
