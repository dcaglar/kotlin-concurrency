# Benchmark: blocking vs non-blocking with Gatling

Two servers with the same endpoint, `GET /names`, which waits 500 ms (fake I/O) and returns three names.
The only difference is **how they wait**:

| Server | Folder | Code | While waiting |
|---|---|---|---|
| Blocking | `reference-server/` (Spring MVC + Tomcat) | `Thread.sleep(500)` | holds one of 200 threads |
| Non-blocking | `webflux-server/` (Spring WebFlux + Netty) | `suspend fun` + `delay(500)` | gives its thread back (8 threads in total) |

Gatling plays the users: `workshop/NamesSimulation.scala` ramps up to 500 users in 10 s, then sends
500 new users per second for 30 s. That is 15,500 requests.

---

## Step 0: install Gatling (once)

```bash
cd ~
curl -fLO https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/3.10.5/gatling-charts-highcharts-bundle-3.10.5-bundle.zip
unzip -q gatling-charts-highcharts-bundle-3.10.5-bundle.zip
mv gatling-charts-highcharts-bundle-3.10.5 ~/gatling
rm gatling-charts-highcharts-bundle-3.10.5-bundle.zip
```

## Step 1: put the simulation into Gatling (once)

Gatling only runs simulations inside its own `user-files/simulations` folder. This copies the workshop
simulation there and makes the port configurable (see the note on port 8080 below):

```bash
mkdir -p ~/gatling/user-files/simulations/workshop
sed 's|http://localhost:8080|http://127.0.0.1:" + System.getProperty("port", "8081") + "|' \
  ~/IdeaProjects/kotlin-concurrency/gatling/workshop/NamesSimulation.scala \
  > ~/gatling/user-files/simulations/workshop/NamesSimulation.scala
```

## Step 2: build the two servers (once, or after changing their code)

```bash
cd ~/IdeaProjects/kotlin-concurrency/gatling
(cd reference-server && mvn -q -DskipTests package)
(cd webflux-server   && mvn -q -DskipTests package)
```

## Step 3: start the servers (terminal 1)

```bash
cd ~/IdeaProjects/kotlin-concurrency/gatling
java -jar reference-server/target/reference-0.0.1-SNAPSHOT.jar --server.port=8081 &   # blocking
java -jar webflux-server/target/webflux-0.0.1-SNAPSHOT.jar   --server.port=8082 &   # non-blocking
```

Check they answer:

```bash
curl 127.0.0.1:8081/names    # ["Mary","John","Anne"]  blocking
curl 127.0.0.1:8082/names    # ["Elle","Mike","John"]  non-blocking
```

> **Why not 8080?** On this Mac, OrbStack (Docker) already listens on `127.0.0.1:8080`, so `localhost:8080`
> reaches a container instead of the server. Hence 8081 and 8082.

## Step 4: run Gatling (terminal 2)

```bash
cd ~/gatling
ulimit -n 10240                                   # allow many open connections
export JAVA_HOME=$(/usr/libexec/java_home -v 21)  # Gatling 3.10 supports Java up to 21

# blocking server
JAVA_OPTS="-Dport=8081 -Xmx1g" bin/gatling.sh --run-mode local -s workshop.BasicSimulation -rd blocking

# non-blocking server
JAVA_OPTS="-Dport=8082 -Xmx1g" bin/gatling.sh --run-mode local -s workshop.BasicSimulation -rd non-blocking
```

Each run takes about 40 s. At the end it prints a summary and the path of an HTML report:

```
Reports generated, please open the following file: file:///Users/.../gatling/results/basicsimulation-.../index.html
```

Open it (`open <path>`) and look at **"Response Time Percentiles over Time"**.

## Step 5: stop the servers

```bash
pkill -f 0.0.1-SNAPSHOT.jar
```

---

## What you should see

Measured on this machine (8 cores) on 2026-10-12:

| | Blocking (Tomcat) | Non-blocking (WebFlux) |
|---|---|---|
| Requests / failed | 15,500 / 0 | 15,500 / 0 |
| Fastest | 501 ms | 500 ms |
| **Average** | **4,215 ms** | **501 ms** |
| 95th percentile | 7,849 ms | 505 ms |
| **Slowest** | **8,290 ms** | **528 ms** |
| Answered under 800 ms | 7 % | 100 % |

In the report the blocking line **climbs** during the test and the non-blocking line stays **flat** at 500 ms.

## Why

**Blocking.** 200 threads, each held for 500 ms per request, can serve at most 200 / 0.5 s = **400 requests/s**.
Gatling sends 500/s, so about 100 requests per second have no free thread and wait in a queue. The queue grows
for the whole 30 s, and response time grows with it: 0.5 s → 8 s. The CPU is idle the whole time; the threads
are just asleep.

**Non-blocking.** `delay` suspends the coroutine and frees the thread. Thousands of requests can be waiting at
the same time on 8 threads, so nobody queues and every answer takes the 500 ms the endpoint itself waits.

This is exercise 2 (`cost-overview.kt`: 100,000 coroutines are cheap, 10,000 threads are not) inside a real web
server.

## Things to try

- **Push harder.** In the simulation change `constantUsersPerSec(500)` to `1000`, redo step 1, and rerun.
  Blocking gets much worse (and starts timing out); non-blocking stays at about 500 ms.
- **Give Tomcat more threads.** Start it with `--server.tomcat.threads.max=1000`. It keeps up again, but at
  the cost of 1000 OS threads. That is the "just add threads" answer, and the 10,000-thread crash in exercise 2
  shows where it ends.
- **Reactor instead of coroutines.** The WebFlux server also has `/names_flux`, which waits with Reactor's
  `Flux.delaySequence`. Change `.get("/names")` to `.get("/names_flux")` in the simulation (redo step 1) and run
  against 8082: same result, different API.
- **Quick test without Gatling.** `ab` comes with macOS:
  `ab -n 4000 -c 1000 http://127.0.0.1:8081/names` (then `8082`) and compare "Time per request".
