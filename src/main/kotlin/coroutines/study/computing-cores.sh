#!/bin/bash
# Computing on ONE core and on ALL cores: runs the SAME six programs (steps 24 to 29) both ways and prints the times.
#   one core:  in a container that gets exactly 1 core (macOS itself cannot give a program only 1 core)
#   all cores: directly on this machine, the same as running the step in IntelliJ
# Needs Docker (OrbStack or Docker Desktop) to be running.
set -e
cd "$(dirname "$0")/../../../../.."                       # the project root

STEPS="Step24BoringWork Step25ManyThreadsWork Step26RunBlockingWork Step27WorkInCoroutines Step28WorkWithYield Step29Dispatcher"
IMAGE=eclipse-temurin:21-jre

docker info > /dev/null 2>&1 || { echo "Docker is not running: start OrbStack or Docker Desktop first."; exit 1; }

mvn -q compile
mvn -q dependency:build-classpath -Dmdep.outputFile=target/classpath.txt > /dev/null
CP="$PWD/target/classes:$(cat target/classpath.txt)"

# the time is the last thing in the last log line of every step: "... 534 ms"
time_of() { tail -1 | grep -o '[0-9]* ms$'; }

on_all_cores() {
    java -Dkotlinx.coroutines.debug -cp "$CP" "coroutines.study.$1Kt" 2>&1
}

on_one_core() {
    docker run --rm --cpuset-cpus=0 \
        -v "$HOME/.m2/repository:$HOME/.m2/repository:ro" \
        -v "$PWD/target/classes:$PWD/target/classes:ro" \
        $IMAGE java -Dkotlinx.coroutines.debug -cp "$CP" "coroutines.study.$1Kt" 2>&1
}

printf "%-26s %10s %12s\n" "step" "1 core" "all cores"
for step in $STEPS; do
    printf "%-26s %10s %12s\n" "$step" "$(on_one_core "$step" | time_of)" "$(on_all_cores "$step" | time_of)"
done
