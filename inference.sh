#!/bin/bash

sudo apt-get update && \
sudo apt-get install -y openjdk-11-jdk && \
sudo apt-get install -y ant && \
sudo apt-get clean;

cd checker-framework
export JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64/"

export PATH="${JAVA_HOME}/bin:{$PATH}"

echo "Building Checker Framework Locally"
echo $JAVA_HOME
git checkout master
./gradlew assemble
git checkout oopsla-2023
./gradlew publishToMavenLocal
cd ..


export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/"
export PATH="${JAVA_HOME}/bin:{$PATH}"

echo "Running inference on Zookeeper"
cd zookeeper
git checkout oopsla-2023-wpi-enabled
git pull

${ZK_CLEAN} &> /dev/null
echo "Zookeeper starting:"
./wpi.sh &> "typecheck.out"
echo "completed"
cd ..

echo "Running inference on HBase"
cd hbase
${HBASE_CMD}
git checkout oopsla-2023-wpi-enabled
git pull

${HBASE_CLEAN} &> /dev/null
echo "Hbase starting:"
./wpi.sh &> "typecheck.out"
echo "completed"
cd ..

echo "Running inference on Hadoop"
cd hadoop
git checkout oopsla-2023-wpi-enabled
git pull

${HADOOP_CLEAN} &> /dev/null
echo "Hadoop starting:"
./wpi.sh &> "typecheck.out"
echo "completed"
cd ..