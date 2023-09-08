#!/bin/bash

#sudo apt-get update && \
#sudo apt-get install -y openjdk-11-jdk && \
#sudo apt-get install -y ant && \
#sudo apt-get clean;

cd checker-framework
export JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64/"

export PATH="${JAVA_HOME}/bin:{$PATH}"

echo "Building Checker Framework Locally"
#echo $JAVA_HOME
git checkout master &> /dev/null
./gradlew assemble &> /dev/null
git checkout oopsla-2023 &> /dev/null
./gradlew publishToMavenLocal &> /dev/null
cd ..
echo "########### Completed ############"

export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/"
export PATH="${JAVA_HOME}/bin:{$PATH}"

echo "Running inference on Zookeeper"
cd zookeeper
git checkout oopsla-2023-wpi-enabled
#git pull &> /dev/null

${ZK_CLEAN} &> /dev/null
#echo "Zookeeper started:"
./wpi.sh &> "zk_inf_typecheck.out"
echo "completed"
cd ..

echo "Running inference on Hadoop"
cd hadoop
git checkout oopsla-2023-wpi-enabled &> /dev/null
#git pull &> /dev/null

${HADOOP_CLEAN} &> /dev/null
#echo "Hadoop starting:"
./wpi.sh &> "hadoop_inf_typecheck.out"
echo "completed"
cd ..

echo "Running inference on HBase"
cd hbase
git checkout master  &> /dev/null
${HBASE_CMD} &> /dev/null
git checkout oopsla-2023-wpi-enabled &> /dev/null
#git pull &> /dev/null

${HBASE_CLEAN} &> /dev/null
#echo "Hbase starting:"
./wpi.sh &> "hbase_inf_typecheck.out"
echo "completed"
cd ..
