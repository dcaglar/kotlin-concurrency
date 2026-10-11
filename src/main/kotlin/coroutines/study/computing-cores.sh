#!/bin/bash
# Run study steps on ONE core and on ALL cores and print the times side by side.
#   one core:  in a container that gets exactly 1 core (macOS itself cannot pin a program to 1 core:
#              the OS places threads on cores, Java cannot; -XX:ActiveProcessorCount only changes what Java reports)
#   all cores: directly on this machine, the same as running the step in IntelliJ
# Needs Docker (OrbStack or Docker Desktop) to be running.
#
# Usage:  computing-cores.sh [options] [step ...]
#   step        a number (25), a range (24-29) or a class name (Step25ManyThreadsWork). Default: 24-29
#   -c N        give the container N cores instead of 1
#   -1          only the 1-core run       -a   only the all-cores run
#   -o          print the whole log output of every run, not just the time
#
# Examples:
#   computing-cores.sh                 steps 24-29, both ways, times in a table (as before)
#   computing-cores.sh 25 29           just those two
#   computing-cores.sh 10-15 -o        steps 10 to 15 with their full output
#   computing-cores.sh 29 -c 2 -1      step 29 on exactly 2 cores
set -e
cd "$(dirname "$0")/../../../../.."                       # the project root
STUDY=src/main/kotlin/coroutines/study
IMAGE=eclipse-temurin:21-jre

CORES=1; ONE=yes; ALL=yes; OUTPUT=no; STEPS=()
while [ $# -gt 0 ]; do
    case "$1" in
        -c) CORES="$2"; shift ;;
        -1) ALL=no ;;
        -a) ONE=no ;;
        -o) OUTPUT=yes ;;
        -h|--help) sed -n '2,19p' "$0"; exit 0 ;;
        *-*) for n in $(seq "${1%-*}" "${1#*-}"); do STEPS+=("$n"); done ;;
        *)  STEPS+=("$1") ;;
    esac
    shift
done
[ ${#STEPS[@]} -eq 0 ] && STEPS=($(seq 24 29))

# a step number becomes its class name: 25 -> Step25ManyThreadsWork (from the file name)
class_of() {
    case "$1" in
        Step*) echo "$1" ;;
        *) local f; f=$(ls "$STUDY"/Step$(printf '%02d' "$1")*.kt 2>/dev/null | head -1)
           [ -n "$f" ] || { echo "no step $1 in $STUDY" >&2; return 1; }
           basename "$f" .kt ;;
    esac
}

[ "$ONE" = yes ] && { docker info > /dev/null 2>&1 || { echo "Docker is not running: start OrbStack or Docker Desktop first."; exit 1; }; }

mvn -q compile
mvn -q dependency:build-classpath -Dmdep.outputFile=target/classpath.txt > /dev/null
CP="$PWD/target/classes:$(cat target/classpath.txt)"

on_all_cores() {
    java -Dkotlinx.coroutines.debug -cp "$CP" "coroutines.study.$1Kt" 2>&1
}

on_n_cores() {
    docker run --rm --cpuset-cpus="0-$((CORES - 1))" \
        -v "$HOME/.m2/repository:$HOME/.m2/repository:ro" \
        -v "$PWD/target/classes:$PWD/target/classes:ro" \
        $IMAGE java -Dkotlinx.coroutines.debug -cp "$CP" "coroutines.study.$1Kt" 2>&1
}

# the time is the last thing in the last log line of a computing step: "... 534 ms". Other steps show "-".
time_of() { local t; t=$(tail -1 | grep -o '[0-9]* ms$'); echo "${t:--}"; }

if [ "$OUTPUT" = yes ]; then
    for step in "${STEPS[@]}"; do
        class=$(class_of "$step")
        [ "$ONE" = yes ] && { echo "===== $class on $CORES core(s) ====="; on_n_cores "$class"; }
        [ "$ALL" = yes ] && { echo "===== $class on all cores =====";     on_all_cores "$class"; }
    done
else
    printf "%-28s %10s %12s\n" "step" "$CORES core(s)" "all cores"
    for step in "${STEPS[@]}"; do
        class=$(class_of "$step")
        one=; all=
        [ "$ONE" = yes ] && one=$(on_n_cores "$class" | time_of)
        [ "$ALL" = yes ] && all=$(on_all_cores "$class" | time_of)
        printf "%-28s %10s %12s\n" "$class" "$one" "$all"
    done
fi
