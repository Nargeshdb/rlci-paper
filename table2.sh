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

echo ""
echo ""
echo "Running RLC on Zookeeper"
cd zookeeper
git checkout oopsla-2023-no-anno &> /dev/null
#git pull &> /dev/null

${ZK_CLEAN} &> /dev/null
#echo "Zookeeper started:"
mvn -B --projects zookeeper-server --also-make compile -DskipTests &> "typecheck_no_anno.out"
zk_warnings_no_anno=$(grep -o "The type of object is:" typecheck_no_anno.out | wc -l)
git checkout oopsla-2023-using-anno &> /dev/null
#git pull &> /dev/null
mvn -B --projects zookeeper-server --also-make compile -DskipTests &> "typecheck_with_anno.out"
zk_warnings_with_anno=$(grep -o "builder:missing.\|builder:required.\|builder:mustcallalias\|builder:contracts\|builder:reset.\|mustcall:arg" typecheck_with_anno.out | wc -l)

echo "#warnings with no-annotation on zookeeper: $zk_warnings_no_anno "
echo "#warnings with inferred annotation on zookeeper: $zk_warnings_with_anno "
echo "completed"
cd ..

echo ""
echo ""
echo "Running RLC on Hadoop"
cd hadoop
git checkout oopsla-2023-no-anno &> /dev/null
#git pull &> /dev/null
${HADOOP_CLEAN} &> /dev/null
#echo "Hadoop starting:"
mvn --projects hadoop-hdfs-project/hadoop-hdfs --also-make clean compile -DskipTests &> "typecheck_no_anno.out"
hadoop_warnings_no_anno=$(grep -o "The type of object is:" typecheck_no_anno.out | wc -l)
git checkout oopsla-2023-using-anno &> /dev/null
#git pull &> /dev/null
mvn --projects hadoop-hdfs-project/hadoop-hdfs --also-make clean compile -DskipTests &> "typecheck_with_anno.out"
hadoop_warnings_with_anno=$(grep -o "builder:missing.\|builder:required.\|builder:mustcallalias\|builder:contracts\|builder:reset.\|mustcall:arg" typecheck_with_anno.out | wc -l)

echo "#warnings with no-annotation on hadoop: $hadoop_warnings_no_anno "
echo "#warnings with inferred annotation on hadoop: $hadoop_warnings_with_anno "
echo "completed"
cd ..

echo ""
echo ""
echo "Running RLC on HBase"
cd hbase
git checkout master  &> /dev/null
${HBASE_CMD} &> /dev/null
git checkout oopsla-2023-no-anno &> /dev/null
#git pull &> /dev/null

${HBASE_CLEAN} &> /dev/null
#echo "Hbase starting:"
mvn --projects hbase-server --also-make clean compile -DskipTests &> "typecheck_no_anno.out"
hbase_warnings_no_anno=$(grep -o "The type of object is:" typecheck_no_anno.out | wc -l)
git checkout oopsla-2023-using-anno &> /dev/null
#git pull &> /dev/null
mvn --projects hbase-server --also-make clean compile -DskipTests &> "typecheck_with_anno.out"
hbase_warnings_with_anno=$(grep -o "builder:missing.\|builder:required.\|builder:mustcallalias\|builder:contracts\|builder:reset.\|mustcall:arg" typecheck_with_anno.out | wc -l)

echo "#warnings with no-annotation on hbase: $hbase_warnings_no_anno "
echo "#warnings with inferred annotation on hbase: $hbase_warnings_with_anno "
echo "completed"
cd ..
