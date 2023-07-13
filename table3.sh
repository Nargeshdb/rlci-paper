#!/bin/bash

ZK_REPO="/home/oopsla/zookeeper"

HBASE_REPO="/home/oopsla/hbase"

HADOOP_REPO="/home/oopsla/hadoop"

sudo apt-get install bc &> /dev/null

convert_to_minutes() {
  time="$1"

  local seconds=$((time % 60))
  local minutes=$((time / 60))
  echo "Total time is: $minutes:"${seconds%.*}" min"
}

compute_total_time() {
  grep_result="$1"

  sum=0 # it is in seconds

  i=1;
  while read n; do
      while IFS= read -r line; do
        unit="$(echo "$line" | awk '{print $NF}')" # get the unit of a line

        if [[ "$unit" == "s" ]]; then
          sum=$(($sum + ${line%.*}))
        elif [[ "$unit" == "min" ]]; then
          minutes=$(echo "$line" | awk -F'[: ]' '{print $1}')
          seconds=$(echo "$line" | awk -F'[: ]' '{print $2}')
          seconds=$(echo "$seconds" | sed 's/^0*//') # Remove leading zeros
          sum=$(($sum + 10#$minutes * 60))
          sum=$(($sum + $seconds))
        fi
      done <<< "$n"

    i=$(($i+1));
  done <<< "$grep_result"

  convert_to_minutes "$sum"
    
}



cd ${ZK_REPO} &> /dev/null
git checkout oopsla-2023-wpi-enabled &> /dev/null
git pull &> /dev/null

echo "Printing inference time for ZOOKEEPER:"
zk_times=$(grep " time: " ${ZK_REPO}/zk_inf_typecheck.out | sed "s/.* time: \(.*\)/\1/")
compute_total_time "$zk_times"
echo ""

git checkout oopsla-2023-verification-time &> /dev/null
git pull &> /dev/null
echo "Printing verification time for ZOOKEEPER:"
zk_times=$(grep " time: " ${ZK_REPO}/verification-perf.out | sed "s/.* time: \(.*\)/\1/")
echo "Total time is: $zk_times"

echo ""

cd ${HADOOP_REPO} &> /dev/null
git checkout oopsla-2023-wpi-enabled &> /dev/null
git pull &> /dev/null

echo "Printing inference time for HADOOP:"
hadoop_times=$(grep " time: " ${HADOOP_REPO}/hadoop_inf_typecheck.out | sed "s/.* time: \(.*\)/\1/")
compute_total_time "$hadoop_times"

echo ""

git checkout oopsla-2023-verification-time &> /dev/null
git pull &> /dev/null
echo "Printing verification time for HADOOP:"
hadoop_times=$(grep " time: " ${HADOOP_REPO}/verification-perf.out | sed "s/.* time: \(.*\)/\1/")
echo "Total time is: $hadoop_times"

echo ""
echo "Printing inference time for HBASE:"

cd ${HBASE_REPO} &> /dev/null
git checkout oopsla-2023-wpi-enabled &> /dev/null
git pull &> /dev/null
hbase_times=$(grep " time: " ${HBASE_REPO}/hbase_inf_typecheck.out | sed "s/.* time: \(.*\)/\1/")
compute_total_time "$hbase_times"

echo ""

git checkout oopsla-2023-verification-time &> /dev/null
git pull &> /dev/null
echo "Printing verification time for HBASE:"
hbase_times=$(grep " time: " ${HBASE_REPO}/verification-perf.out | sed "s/.* time: \(.*\)/\1/")
echo "Total time is: $hbase_times"
