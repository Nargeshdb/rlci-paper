#!/bin/bash

ZK_REPO="/home/oopsla/zookeeper"

HBASE_REPO="/home/oopsla/hbase"

HADOOP_REPO="/home/oopsla/hadoop"

print_result() {
  grep_result="$1"

  sum_left=0
  sum_right=0

  i=1;
  while read n; do
    echo "$n"
    i=$(($i+1));
  done <<< "$grep_result"
    
}


echo "Printing inference time for ZOOKEEPER:"
cd ${ZK_REPO}
git checkout oopsla-2023-wpi-enabled
git pull

zk_times=$(grep " time: " ${ZK_REPO}/typecheck.out)
print_result "$zk_times"
echo ""
echo "Printing verification time for ZOOKEEPER:"
echo "TODO"

echo ""
echo "Printing result for HADOOP:"

cd ${HADOOP_REPO}
git checkout oopsla-2023-wpi-enabled
git pull
hadoop_times=$(grep " time: " ${HADOOP_REPO}/typecheck.out)
print_result "$hadoop_times"

echo ""
echo "Printing verification time for ZOOKEEPER:"
echo "TODO"

echo ""
echo "Printing result for HBASE:"

cd ${HBASE_REPO}
git checkout oopsla-2023-wpi-enabled
git pull
hbase_times=$(grep " time: " ${HBASE_REPO}/typecheck.out)
print_result "$hbase_times"

echo ""
echo "Printing verification time for ZOOKEEPER:"
echo "TODO"
