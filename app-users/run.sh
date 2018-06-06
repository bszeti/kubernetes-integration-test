#sudo docker run --name mockserver -d -p 1080:1080 docker.io/jamesdbloom/mockserver
#sudo docker run --name mariadb -e MYSQL_USER=myuser -e MYSQL_PASSWORD=mypassord -e MYSQL_ROOT_PASSWORD=secret -e MYSQL_DATABASE=testdb -d -p 3306:3306 registry.access.redhat.com/rhscl/mariadb-102-rhel7
#sudo docker run --name amq -e AMQ_USER=test -e AMQ_PASSWORD=secret -d -p 61616:61616 -p 8181:8181 registry.access.redhat.com/jboss-amq-6/amq63-openshift

mvn -Dspring.profiles.active=local clean spring-boot:run
