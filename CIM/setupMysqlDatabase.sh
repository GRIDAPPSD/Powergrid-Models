#!/bin/bash


if [ $# -lt 4 ]; 
then
    echo "Usage: $0 <db_host> <root_username> <root_pw> <gridappsd_pw>"
    echo "* dbhost: Host of your database"
    echo "* root_username: Root or other priviledged user name"
    echo "* root_pw: Root or other priviledged user password"
    echo "* gridappsd: The password that you would like to assign to the new gridappsd user"
    exit 1;
fi
pwd=`pwd`

#Create new gridappsd account

mysql -u$2 -p$3 -h$1 -e "CREATE USER 'gridappsd'@'localhost' IDENTIFIED BY '$4';"
mysql -u$2 -p$3 -h$1 -e "GRANT ALL PRIVILEGES ON gridappsd.* TO 'gridappsd'@'localhost';"
#Reset or create database
mysql -ugridappsd -p$4 -h$1 < Drop_RC1.sql;
mysql -ugridappsd -p$4 -h$1 < Create_RC1.sql;

#Make sure package to load data is up to date

pushd cim-parser;
pwd;
mvn package;
popd;
pwd

#Call load data
java -jar cim-parser/target/cim-parser-0.0.1-SNAPSHOT-jar-with-dependencies.jar ieee8500.xml $1 gridappsd $4;

