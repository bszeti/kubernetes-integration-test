### Building

The example can be built with

    mvn clean install

### Running on localhost

    mvn spring-boot:run

### Running the example in OpenShift

The example can then be built and deployed using a single goal:

    $ mvn fabric8:deploy

### Accessing the Spring REST Actuator services

curl http://localhost:8080/health
