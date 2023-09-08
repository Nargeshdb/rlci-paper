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


export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/"
export PATH="${JAVA_HOME}/bin:{$PATH}"


cd zookeeper
git checkout oopsla-2023-verification-time &> /dev/null
#git pull &> /dev/null

${ZK_CLEAN} &> /dev/null
#echo "Zookeeper starting:"
echo "Running Resource Leak Checker on Zookeeper"
mvn -B --projects zookeeper-server --also-make compile -DskipTests &> "verification-perf.out"
echo "completed"
cd ..


cd hadoop
git checkout oopsla-2023-verification-time &> /dev/null
#git pull &> /dev/null

${HADOOP_CLEAN} &> /dev/null
#echo "Hadoop starting:"
echo "Running Resource Leak Checker on Hadoop"
mvn --projects hadoop-hdfs-project/hadoop-hdfs --also-make clean compile -DskipTests &> "verification-perf.out"
echo "completed"
cd ..

cd hbase
git checkout master &> /dev/null
${HBASE_CMD} &> /dev/null
git checkout oopsla-2023-verification-time &> /dev/null
#git pull &> /dev/null

${HBASE_CLEAN} &> /dev/null
#echo "Hbase starting:"
echo "Running Resource Leak Checker on HBase"
mvn --projects hbase-server --also-make clean compile -DskipTests &> "verification-perf.out"
echo "completed"
cd ..