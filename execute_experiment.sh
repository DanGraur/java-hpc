#!/usr/bin/env bash

# Build the application
mvn clean compile assembly:single

# Create an array which will iterate through the experiments

declare -a test_cases=("GET_GENERIC_TL" "GET_GENERIC_U" "GET_GENERIC_L" "GET_HARDCODED_TL" "GET_HARDCODED_U" "GET_HARDCODED_L")

for i in "${test_cases[@]}"
do
    file_name="${i}_output.txt"
    rm -f ${file_name}
    java -jar target/java-hpc-1.0-SNAPSHOT-jar-with-dependencies.jar ${i} >> ${file_name}
done

rm -R target