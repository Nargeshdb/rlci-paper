#!/bin/bash

cd checker-framework
git checkout master
git pull
./gradlew assemble
export PATH=$CHECKERFRAMEWORK/checker/bin:${PATH}
git checkout oopsla-2023
git pull
./gradlew publishToMavenLocal
cd ..

JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
export JAVA_HOME

cd zookeeper
git checkout oopsla-2023-wpi-enabled
git pull

${ZK_CLEAN} &> /dev/null
echo "Zookeeper starting:"
sh ./wpi.sh &> "typecheck.out"
echo "completed"
cd ..

cd hbase
git checkout oopsla-2023-wpi-enabled
git pull

${HBASE_CLEAN} &> /dev/null
echo "Hbase starting:"
sh ./wpi.sh &> "typecheck.out"
echo "completed"
cd ..

cd hadoop
git checkout oopsla-2023-wpi-enabled
git pull

${HADOOP_CLEAN} &> /dev/null
echo "Hadoop starting:"
sh ./wpi.sh &> "typecheck.out"
echo "completed"
cd ..