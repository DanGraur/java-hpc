#!/usr/bin/env bash

if [[ $# -ne 1 && $# -ne 2 ]]
then
    echo "Usage: ./execute_experiment.sh <experiment_size: (10000|1000000|10000000)> [<experiment_name>]"
    exit 255
fi

# Set up the environment
export JAVA_HOME=/data/software/sys/jdk-13.0.1
export PATH=$JAVA_HOME/bin:$PATH
which java

# Build the application
mvn clean compile assembly:single

# Create an array which will hold the experiment names
if [[ $# -eq 1 ]]
then
    declare -a test_cases=("GET_GENERIC_TL" "GET_GENERIC_U" "GET_GENERIC_L" "GET_HARDCODED_TL" "GET_HARDCODED_U" "GET_HARDCODED_L")
else
    declare -a test_cases=(${2})
fi

# Declare an array where we will collect the .csv files, and make a result directory
declare -a csv_files

rm -Rf results
mkdir results

# Iterate through the experiments and record their results
for i in "${test_cases[@]}"
do
    file_name="${i}_output.csv"
#    rm -f ${file_name}
    csv_files+=${file_name}
    java -jar target/java-hpc-1.0-SNAPSHOT-jar-with-dependencies.jar ${i} ${1} >> results/${file_name}
    wait
done

rm -R target

# Iterate through the csv files and print them
for i in "${csv_files[@]}"
do
    echo ${i}
done