#!/bin/sh
sudo docker rm -f mockserver
sudo docker rm -f mariadb
sudo docker rm -f amq

sudo docker run --name mockserver -d -p 1080:1080 docker.io/jamesdbloom/mockserver
sudo docker run --name mariadb -e MYSQL_USER=myuser -e MYSQL_PASSWORD=mypassword -e MYSQL_ROOT_PASSWORD=secret -e MYSQL_DATABASE=testdb -d -p 3306:3306 registry.access.redhat.com/rhscl/mariadb-102-rhel7
sudo docker run --name amq -e AMQ_USER=test -e AMQ_PASSWORD=secret -d -p 61616:61616 -p 8181:8181 registry.access.redhat.com/jboss-amq-6/amq63-openshift

sleep 10

sh sql/setup.sh
sh mockserver/setup.sh

cd ../app-users
java -Dspring.profiles.active=k8sit -jar target/app-users-1.0-SNAPSHOT.jar &
pid=$!
cd -

export AMQ_USER=test
export AMQ_PASSWORD=secret
mvn clean test

kill $pid