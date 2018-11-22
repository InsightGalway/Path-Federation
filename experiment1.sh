#!/bin/bash
BASE=`pwd`
TASKS=$BASE/benchmark/tasks/
CONFIG=$BASE/benchmark/config

RUNS=10 
RESULTS=$BASE/experiment1_results
LOGS=$BASE/experiment1_logs
mkdir -p $RESULTS $LOGS
cd $BASE

export MAVEN_OPTS="-Xmx20G"
for algo in bibfs bfs;
do
  for config in `ls $CONFIG/*`;
  do
    echo ">>RUNNING $algo with $config"
    run=$config-$algo
    LOG=$LOGS/$run.out
    cmd="mvn exec:java -e -Dexec.mainClass=\"Benchmark\" -Dexec.args=\"-tasks $TASKS -config $CONFIG/$config -algo $algo -runs $RUNS -out $RESULTS/$run-results.csv -results $RESULTS \" 1>>$LOG"
    echo $cmd
    echo $cmd > $LOG
    eval $cmd
  done
done