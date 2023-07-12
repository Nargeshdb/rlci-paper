#!/bin/bash

ZK_GENERATED="/home/oopsla/rlci-paper/experiments/inferred-annos-counter/inputExamples/zookeeper/generated"
ZK_HUMAN_WRITTEN="/home/oopsla/rlci-paper/experiments/inferred-annos-counter/inputExamples/zookeeper/human-written"

HBASE_GENERATED="/home/oopsla/rlci-paper/experiments/inferred-annos-counter/inputExamples/hbase/generated"
HBASE_HUMAN_WRITTEN="/home/oopsla/rlci-paper/experiments/inferred-annos-counter/inputExamples/hbase/human-written"

HADOOP_GENERATED="/home/oopsla/rlci-paper/experiments/inferred-annos-counter/inputExamples/hadoop/generated"
HADOOP_HUMAN_WRITTEN="/home/oopsla/rlci-paper/experiments/inferred-annos-counter/inputExamples/hadoop/human-written"

RUN_IAC_PATH="/home/oopsla/rlci-paper/experiments/inferred-annos-counter"

print_result() {
  grep_result="$1"
  anno="$2"

  sum_left=0
  sum_right=0

  i=1;
  while read n; do

    while IFS= read -r line; do
      # Extract the numerator and denominator from the line using awk
      numerator=$(echo "$line" | awk -F'[/ ]' '{print $(NF-1)}')
      denominator=$(echo "$line" | awk -F'[/ ]' '{print $NF}')

      # Update the sums
      sum_left=$((sum_left + numerator))
      sum_right=$((sum_right + denominator))
    done <<< "$n"

  i=$(($i+1)); 
  done <<< "$grep_result"

  echo "$anno: $sum_left/$sum_right" 
    
}

bash "${RUN_IAC_PATH}/run-iac.sh" "${ZK_HUMAN_WRITTEN}" "${ZK_GENERATED}" &> "${RUN_IAC_PATH}/zookeeper_count.out"
bash "${RUN_IAC_PATH}/run-iac.sh" "${HBASE_HUMAN_WRITTEN}" "${HBASE_GENERATED}" &> "${RUN_IAC_PATH}/hbase_count.out"
bash "${RUN_IAC_PATH}/run-iac.sh" "${HADOOP_HUMAN_WRITTEN}" "${HADOOP_GENERATED}" &> "${RUN_IAC_PATH}/hadoop_count.out"


echo "Printing result for ZOOKEEPER:"

zk_ownings=$(grep "@Owning" ${RUN_IAC_PATH}/zookeeper_count.out)

print_result "$zk_ownings" "@Owning"

zk_mca=$(grep "@MustCallAlias" ${RUN_IAC_PATH}/zookeeper_count.out)

print_result "$zk_mca" "@MustCallAlias"

zk_ecm=$(grep "@EnsuresCalledMethods " ${RUN_IAC_PATH}/zookeeper_count.out)

print_result "$zk_ecm" "@EnsuresCalledMethods"

zk_im=$(grep "@InheritableMustCall " ${RUN_IAC_PATH}/zookeeper_count.out)

print_result "$zk_im" "@InheritableMustCall"

zk_not_owning=$(grep "@NotOwning" ${RUN_IAC_PATH}/zookeeper_count.out)

print_result "$zk_not_owning" "@NotOwning"

echo ""
echo "Printing result for HADOOP:"

hadoop_ownings=$(grep "@Owning" ${RUN_IAC_PATH}/hadoop_count.out)

print_result "$hadoop_ownings" "@Owning"

hadoop_mca=$(grep "@MustCallAlias" ${RUN_IAC_PATH}/hadoop_count.out)

print_result "$hadoop_mca" "@MustCallAlias"

hadoop_ecm=$(grep "@EnsuresCalledMethods " ${RUN_IAC_PATH}/hadoop_count.out)

print_result "$hadoop_ecm" "@EnsuresCalledMethods"

hadoop_im=$(grep "@InheritableMustCall " ${RUN_IAC_PATH}/hadoop_count.out)

print_result "$hadoop_im" "@InheritableMustCall"

hadoop_not_owning=$(grep "@NotOwning" ${RUN_IAC_PATH}/hadoop_count.out)

print_result "$hadoop_not_owning" "@NotOwning"

echo ""

echo "Printing result for HBASE:"

hbase_ownings=$(grep "@Owning" ${RUN_IAC_PATH}/hbase_count.out)

print_result "$hbase_ownings" "@Owning"

hbase_mca=$(grep "@MustCallAlias" ${RUN_IAC_PATH}/hbase_count.out)

print_result "$hbase_mca" "@MustCallAlias"

hbase_ecm=$(grep "@EnsuresCalledMethods " ${RUN_IAC_PATH}/hbase_count.out)

print_result "$hbase_ecm" "@EnsuresCalledMethods"

hbase_im=$(grep "@InheritableMustCall " ${RUN_IAC_PATH}/hbase_count.out)

print_result "$hbase_im" "@InheritableMustCall"

hbase_not_owning=$(grep "@NotOwning" ${RUN_IAC_PATH}/hbase_count.out)

print_result "$hbase_not_owning" "@NotOwning"

