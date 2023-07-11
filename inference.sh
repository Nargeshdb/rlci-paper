#!/bin/bash

# CURDIR=$(pwd)
# RESULTS="${CURDIR}/results"

# if [ -d "${RESULTS}" ]; then
#     rm -rf "${RESULTS}"
# fi

# mkdir "${RESULTS}"

cd checker-framework
git checkout oopsla-2023
git pull
./gradlew assemble
export PATH=$CHECKERFRAMEWORK/checker/bin:${PATH}
./gradlew publishToMavenLocal
cd ..

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