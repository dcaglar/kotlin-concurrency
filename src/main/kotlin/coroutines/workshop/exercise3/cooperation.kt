package coroutines.workshop.exercise3

import coroutines.workshop.common.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

/*
 This is an exercise on cooperation between workers. At the same time, we'll
 see just how threads and coroutines have fundamental differences in what concerns
 concurrency.

 1. Run this code as is. Note down the result and the duration.

 2. Notice that the sumLeibniz() function has start and end arguments. Because it's a sum,
    sumLeibniz(0, 10) produces the same results as sumLeibniz(0, 5) + sumLeibniz(6, 10).
    Use this property to refactor the calculation into 10 separate factors.
    Check that you get approximately the same result as you did in 1). Note down the duration.
    sum(i,j) =  sum(i,j/2) + sum(j/2+1,j)
 3. Run each of those separate factors in its own thread. Note down the duration.
 4. Run each of those separate factors in its own coroutine. Note down the duration.

 5. Now experiment with yield() in the coroutines. Do the results differ?
 */

fun main() {
    logger.info("Warming up")
    warmUp() // this is so the JIT compiler will kick in, and we get more accurate measurements.
    logger.info("Warmup done")
    logger.info("Starting  singleCalculation")
    val singleDuration = measureTimeMillis {
        calculatePi()
    }
    logger.info("Starting  calculatePiInTenFactor_2")

    val durationIn10FactorLinearDuration_2 = measureTimeMillis {
        calculatePiInTenFactor_2()
    }

    logger.info("Starting       calculatePiIn10FactorBy10Thread_3()")

    val durationin10FactorBy10Thread_3 = measureTimeMillis {
        calculatePiIn10FactorBy10Thread_3()
    }

    logger.info("Starting       durationin10FactorBy10Coroutine_4()")
    val durationin10FactorBy10Coroutine_4 = measureTimeMillis {
        calculatePiIn10FactorBy10Coroutine_4()
    }


    logger.info("Starting       durationin10FactorBy10CoroutineOnDispatcherDefault()")
    val durationin10FactorBy10CoroutineOnDispatcherDefault = measureTimeMillis {
        calculatePiIn10FactorBy10Coroutine_OnDefaultDispatcher()
    }



    logger.info("Starting       calculatePiIn10FactorBy10CoroutineWithYield_5()")
    val durationin10FactorBy10CoroutineWithYield_5 = measureTimeMillis {
        calculatePiIn10FactorBy10CoroutineWithYield_5()
    }

    println("(single)     Calculation took $singleDuration ms")
    println("(10FactorInlinear)     Calculation took $durationIn10FactorLinearDuration_2 ms")
    println("( 10 threads)    Calculation took $durationin10FactorBy10Thread_3 ms")
    println("(10 coroutines) Calculation took $durationin10FactorBy10Coroutine_4 ms")
    println("(10 coroutines) on Default Dispatcher Calculation took $durationin10FactorBy10CoroutineOnDispatcherDefault ms")
    println("(10 coroutines + yield) Calculation took $durationin10FactorBy10CoroutineWithYield_5 ms")

    val duration = measureTimeMillis {
        /*
        13:03:50.995 [main] INFO  exercise - Warming up
13:03:50.997 [main] INFO  exercise - 0 .. 1000000
13:03:51.001 [main] INFO  exercise - 0 .. 1000000
13:03:51.002 [main] INFO  exercise - 0 .. 1000000
13:03:51.003 [main] INFO  exercise - 0 .. 1000000
13:03:51.005 [main] INFO  exercise - 0 .. 1000000
13:03:51.006 [main] INFO  exercise - Warmup done
13:03:51.026 [main @coroutine#1] INFO  exercise - 0 .. 1000000000
13:03:51.996 [main @coroutine#1] INFO  exercise - 3.1415926545880506
13:03:51.998 [main] INFO  exercise - Calculation took 979 ms
         */

     //   calculatePiWith10Factor_2()
        /*
        observarition almost same time with 1st method but difference is work slices 10 part, in the end its always same threadcoorutine does work sequentialy
        13:43:04.445 [main] INFO  exercise - Warming up
13:43:04.446 [main] INFO  exercise - 0 .. 1000000
13:43:04.450 [main] INFO  exercise - 0 .. 1000000
13:43:04.452 [main] INFO  exercise - 0 .. 1000000
13:43:04.453 [main] INFO  exercise - 0 .. 1000000
13:43:04.454 [main] INFO  exercise - 0 .. 1000000
13:43:04.455 [main] INFO  exercise - Warmup done
13:43:04.474 [main @coroutine#1] INFO  exercise - adding sum(0,100000000)
13:43:04.474 [main @coroutine#1] INFO  exercise - 0 .. 100000000
13:43:04.571 [main @coroutine#1] INFO  exercise - adding sum(100000001,200000000)
13:43:04.572 [main @coroutine#1] INFO  exercise - 100000001 .. 200000000
13:43:04.670 [main @coroutine#1] INFO  exercise - adding sum(200000001,300000000)
13:43:04.670 [main @coroutine#1] INFO  exercise - 200000001 .. 300000000
13:43:04.766 [main @coroutine#1] INFO  exercise - adding sum(300000001,400000000)
13:43:04.767 [main @coroutine#1] INFO  exercise - 300000001 .. 400000000
13:43:04.862 [main @coroutine#1] INFO  exercise - adding sum(400000001,500000000)
13:43:04.862 [main @coroutine#1] INFO  exercise - 400000001 .. 500000000
13:43:04.961 [main @coroutine#1] INFO  exercise - adding sum(500000001,600000000)
13:43:04.961 [main @coroutine#1] INFO  exercise - 500000001 .. 600000000
13:43:05.058 [main @coroutine#1] INFO  exercise - adding sum(600000001,700000000)
13:43:05.059 [main @coroutine#1] INFO  exercise - 600000001 .. 700000000
13:43:05.154 [main @coroutine#1] INFO  exercise - adding sum(700000001,800000000)
13:43:05.154 [main @coroutine#1] INFO  exercise - 700000001 .. 800000000
13:43:05.249 [main @coroutine#1] INFO  exercise - adding sum(800000001,900000000)
13:43:05.250 [main @coroutine#1] INFO  exercise - 800000001 .. 900000000
13:43:05.345 [main @coroutine#1] INFO  exercise - adding sum(900000001,1000000000)
13:43:05.345 [main @coroutine#1] INFO  exercise - 900000001 .. 1000000000
13:43:05.441 [main @coroutine#1] INFO  exercise - 3.1415926545893256
13:43:05.443 [main] INFO  exercise - Calculation took 984 ms
         */


     //  calculatePiWith10Threads_3()
        /*
        13:48:28.493 [main] INFO  exercise - Warming up
13:48:28.494 [main] INFO  exercise - 0 .. 1000000
13:48:28.499 [main] INFO  exercise - 0 .. 1000000
13:48:28.501 [main] INFO  exercise - 0 .. 1000000
13:48:28.503 [main] INFO  exercise - 0 .. 1000000
13:48:28.504 [main] INFO  exercise - 0 .. 1000000
13:48:28.505 [main] INFO  exercise - Warmup done
13:48:28.507 [Thread-0] INFO  exercise - adding sum(0,100000000)
13:48:28.507 [Thread-0] INFO  exercise - 0 .. 100000000
13:48:28.507 [Thread-3] INFO  exercise - adding sum(0,400000000)
13:48:28.507 [Thread-3] INFO  exercise - 0 .. 400000000
13:48:28.507 [Thread-2] INFO  exercise - adding sum(0,300000000)
13:48:28.507 [Thread-1] INFO  exercise - adding sum(0,200000000)
13:48:28.507 [Thread-2] INFO  exercise - 0 .. 300000000
13:48:28.507 [Thread-1] INFO  exercise - 0 .. 200000000
13:48:28.512 [Thread-4] INFO  exercise - adding sum(0,500000000)
13:48:28.512 [Thread-4] INFO  exercise - 0 .. 500000000
13:48:28.515 [Thread-6] INFO  exercise - adding sum(0,700000000)
13:48:28.516 [Thread-6] INFO  exercise - 0 .. 700000000
13:48:28.519 [Thread-5] INFO  exercise - adding sum(0,600000000)
13:48:28.519 [Thread-5] INFO  exercise - 0 .. 600000000
13:48:28.524 [Thread-7] INFO  exercise - adding sum(0,800000000)
13:48:28.524 [Thread-7] INFO  exercise - 0 .. 800000000
13:48:28.531 [Thread-8] INFO  exercise - adding sum(0,900000000)
13:48:28.531 [Thread-8] INFO  exercise - 0 .. 900000000
13:48:28.543 [main] INFO  exercise - 0.0
13:48:28.543 [main] INFO  exercise - Calculation took 38 ms
13:48:28.546 [Thread-9] INFO  exercise - adding sum(0,1000000000)
13:48:28.546 [Thread-9] INFO  exercise - 0 .. 1000000000
         */

     // calculatePiWith10Coroutine_4()


        /*
        13:51:41.410 [main] INFO  exercise - Warming up
13:51:41.412 [main] INFO  exercise - 0 .. 1000000
13:51:41.416 [main] INFO  exercise - 0 .. 1000000
13:51:41.418 [main] INFO  exercise - 0 .. 1000000
13:51:41.419 [main] INFO  exercise - 0 .. 1000000
13:51:41.420 [main] INFO  exercise - 0 .. 1000000
13:51:41.421 [main] INFO  exercise - Warmup done
13:51:41.443 [main @coroutine#1] INFO  exercise - 0.0
13:51:41.444 [main @coroutine#2] INFO  exercise - adding sum(0,100000000)
13:51:41.444 [main @coroutine#2] INFO  exercise - 0 .. 100000000
13:51:41.541 [main @coroutine#3] INFO  exercise - adding sum(100000001,200000000)
13:51:41.541 [main @coroutine#3] INFO  exercise - 100000001 .. 200000000
13:51:41.638 [main @coroutine#4] INFO  exercise - adding sum(200000001,300000000)
13:51:41.638 [main @coroutine#4] INFO  exercise - 200000001 .. 300000000
13:51:41.735 [main @coroutine#5] INFO  exercise - adding sum(300000001,400000000)
13:51:41.735 [main @coroutine#5] INFO  exercise - 300000001 .. 400000000
13:51:41.832 [main @coroutine#6] INFO  exercise - adding sum(400000001,500000000)
13:51:41.832 [main @coroutine#6] INFO  exercise - 400000001 .. 500000000
13:51:41.930 [main @coroutine#7] INFO  exercise - adding sum(500000001,600000000)
13:51:41.930 [main @coroutine#7] INFO  exercise - 500000001 .. 600000000
13:51:42.029 [main @coroutine#8] INFO  exercise - adding sum(600000001,700000000)
13:51:42.029 [main @coroutine#8] INFO  exercise - 600000001 .. 700000000
13:51:42.125 [main @coroutine#9] INFO  exercise - adding sum(700000001,800000000)
13:51:42.125 [main @coroutine#9] INFO  exercise - 700000001 .. 800000000
13:51:42.219 [main @coroutine#10] INFO  exercise - adding sum(800000001,900000000)
13:51:42.219 [main @coroutine#10] INFO  exercise - 800000001 .. 900000000
13:51:42.314 [main @coroutine#11] INFO  exercise - adding sum(900000001,1000000000)
13:51:42.314 [main @coroutine#11] INFO  exercise - 900000001 .. 1000000000
13:51:42.409 [main] INFO  exercise - Calculation took 988 ms
         */
    }



}

fun calculatePi() = runBlocking {
    // approximate pi up to 9 digits: up to 1_000_000_000 terms
    val pi = sumLeibniz(0, 1_000_000_000) * 4

    logger.info("pi is $pi") // (correct value of pi to 16 digits: 3.1415926535897932)
}



fun calculatePiInTenFactor_2() = runBlocking {
    // approximate pi up to 9 digits: up to 1_000_000_000 terms
    var curStart = 0L
    val end = 1_000_000_000L
    val nrOfTimes = 10
    var pi = 0.toDouble()
    val factorArr = DoubleArray(nrOfTimes)
    //sumLeibniz(0,1_000_000_000) = sumLeibniz(0,end/nroftime) + sumLeibniz(end/nroftime+1,end/nroftime*2)...  sumLeibniz(end/nroftime+1,end/nroftime*10).
    val rangeLength = end/10
    for (count in 0 ..nrOfTimes-1) {
        val curStart = count * rangeLength
        val curEnd = (rangeLength * (count + 1))-1
        logger.info("adding sum($curStart,$curEnd)")
        val curResult =( (sumLeibniz(curStart, curEnd)) * 4)
        factorArr[count] = curResult
    }

    pi = factorArr.sum()
    logger.info("pi is $pi") // (correct value of pi to 16 digits: 3.1415926535897932)
}

fun calculatePiIn10FactorBy10Thread_3() {
    // approximate pi up to 9 digits: up to 1_000_000_000 terms
    val end = 1_000_000_000L
    val nrOfTimes = 10
    var pi = 0.toDouble()
    val factorArr = DoubleArray(nrOfTimes)
    //sum(0,1_000_000_000) = sum(0,end/nroftime) + sum(end/nroftime+1,end/nroftime*2)
    val rangeLength = end / 10
    val threads = mutableListOf<Thread>()          // a list to keep the threads
    for (count in 0..nrOfTimes - 1) {
       val t = thread {
            val curStart = count * rangeLength
            val curEnd = (rangeLength * (count + 1))-1
            logger.info("adding sum($curStart,$curEnd)")
            val curResult =( (sumLeibniz(curStart, curEnd)) * 4)
            factorArr[count] = curResult
        }

        threads.add(t);
    }

    threads.forEach { it.join() }
    pi = factorArr.sum()
    logger.info("pi is $pi") // (correct value of pi to 16 digits: 3.1415926535897932)
}


fun calculatePiIn10FactorBy10Coroutine_4() = runBlocking {
    // approximate pi up to 9 digits: up to 1_000_000_000 terms
    var curStart = 0L
    val end = 1_000_000_000L
    val nrOfTimes = 10
    var pi = 0.toDouble()
    val factorArr = DoubleArray(nrOfTimes)
    //sum(0,1_000_000_000) = sum(0,end/nroftime) + sum(end/nroftime+1,end/nroftime*2)
    val rangeLength = end/10
    val coroutines = mutableListOf<Job>()          // a list to keep the threads
    for (count in 0 ..nrOfTimes-1) {
    val coroutine =    launch {
            val curStart = count * rangeLength
            val curEnd = (rangeLength * (count + 1))-1
            logger.info("adding sum($curStart,$curEnd)")
            val curResult =( (sumLeibniz(curStart, curEnd)) * 4)
            factorArr[count] = curResult

        }
        coroutines.add(coroutine)
    }





    coroutines.joinAll()
    pi = factorArr.sum()
    logger.info("pi is $pi") // (correct value of pi to 16 digits: 3.1415926535897932)
}





fun calculatePiIn10FactorBy10Coroutine_OnDefaultDispatcher() = runBlocking {
    // approximate pi up to 9 digits: up to 1_000_000_000 terms
    var curStart = 0L
    val end = 1_000_000_000L
    val nrOfTimes = 10
    var pi = 0.toDouble()
    val factorArr = DoubleArray(nrOfTimes)
    //sum(0,1_000_000_000) = sum(0,end/nroftime) + sum(end/nroftime+1,end/nroftime*2)
    val rangeLength = end / 10
    val coroutines = mutableListOf<Job>()          // a list to keep the threads
    for (count in 0..nrOfTimes - 1) {
        val coroutine = launch(Dispatchers.Default) {
            val curStart = count * rangeLength
            val curEnd = (rangeLength * (count + 1)) - 1
            logger.info("adding sum($curStart,$curEnd)")
            val curResult = ((sumLeibniz(curStart, curEnd)) * 4)
            factorArr[count] = curResult

        }
        coroutines.add(coroutine)
    }
}


fun calculatePiIn10FactorBy10CoroutineWithYield_5() = runBlocking {
    // approximate pi up to 9 digits: up to 1_000_000_000 terms
    val end = 1_000_000_000L
    val nrOfTimes = 10
    var pi = 0.toDouble()
    val factorArr = DoubleArray(nrOfTimes)
    val rangeLength = end/10
    val coroutines = mutableListOf<Job>()
    for (count in 0 ..nrOfTimes-1) {
        val coroutine = launch {
            val curStart = count * rangeLength
            val curEnd = (rangeLength * (count + 1))-1
            logger.info("adding sum($curStart,$curEnd)")
            yield()   // give the thread to the other coroutines before starting the sum
            val curResult =( (sumLeibniz(curStart, curEnd)) * 4)
            factorArr[count] = curResult
        }
        coroutines.add(coroutine)
    }

    coroutines.joinAll()
    pi = factorArr.sum()
    logger.info("pi is $pi") // (correct value of pi to 16 digits: 3.1415926535897932)
}

fun sumLeibniz(start: Long, end: Long): Double { // Leibniz series: 1 - 1/3 + 1/5 - 1/7 + 1/9 - .... ~ pi / 4

    logger.info("sumLeibniz  from $start to $end")

    var result = 0.0
    for (factor in start..end) {
        result += (if (factor % 2 == 0L) 1.0 else -1.0) / (1 + 2 * factor)
    }
    return result
}

fun warmUp( ){

    repeat(5) {
        sumLeibniz(0, 1_000_000)
    }

}



/*
package coroutines.workshop.exercise3

import coroutines.workshop.common.logger
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

/*
 This is an exercise on cooperation between workers. At the same time, we'll
 see just how threads and coroutines have fundamental differences in what concerns
 concurrency.

 1. Run this code as is. Note down the result and the duration.

 2. Notice that the sumLeibniz() function has start and end arguments. Because it's a sum,
    sumLeibniz(0, 10) produces the same results as sumLeibniz(0, 5) + sumLeibniz(6, 10).
    Use this property to refactor the calculation into 10 separate factors.
    Check that you get approximately the same result as you did in 1). Note down the duration.

 3. Run each of those separate factors in its own thread. Note down the duration.

 4. Run each of those separate factors in its own coroutine. Note down the duration.

 5. Now experiment with yield() in the coroutines. Do the results differ?
 */

fun main() {

    warmUp() // this is so the JIT compiler will kick in, and we get more accurate measurements.

    val linearDuration = measureTimeMillis {
        calculatePiLinearly()
    }
    val threadsDuration = measureTimeMillis {
        calculatePiWithThreads()
    }
    val coroutinesDuration = measureTimeMillis {
        calculatePiWithCoroutines()
    }

    println("(linear)     Calculation took $linearDuration ms")
    println("(threads)    Calculation took $threadsDuration ms")
    println("(coroutines) Calculation took $coroutinesDuration ms")
}

fun calculatePiLinearly() = runBlocking {
    // approximate pi up to 9 digits: up to 1_000_000_000 terms
    val pi = sumLeibniz(0, 999_999_999) * 4
    println(pi) // (correct value of pi to 16 digits: 3.1415926535897932)
}

fun calculatePiWithThreads() {
    // approximate pi up to 9 digits: up to 1_000_000_000 terms
    val termCount = 1_000_000_000L
    val iterations = 10
    val interval = termCount / iterations

    val intermediateResults = DoubleArray(iterations)
    val threads = Array(iterations) {
        thread { intermediateResults[it] = runBlocking { sumLeibniz(it * interval, (it + 1) * interval - 1) } }
    }
    threads.forEach { it.join() }
    val pi = intermediateResults.sum() * 4

    println(pi) // (correct value of pi to 16 digits: 3.1415926535897932)
}

fun calculatePiWithCoroutines() = runBlocking {
    // approximate pi up to 9 digits: up to 1_000_000_000 terms
    val termCount = 1_000_000_000L
    val iterations = 10
    val interval = termCount / iterations

    val intermediateResults = DoubleArray(iterations)
    val coroutines = Array(iterations) {
        launch { intermediateResults[it] = sumLeibniz(it * interval, (it + 1) * interval - 1) }
    }
    coroutines.forEach { it.join() }
    val pi = intermediateResults.sum() * 4

    println(pi) // (correct value of pi to 16 digits: 3.1415926535897932)
}

suspend fun sumLeibniz(start: Long, end: Long): Double { // Leibniz series: 1 - 1/3 + 1/5 - 1/7 + 1/9 - .... ~ pi / 4

    logger.info("{} .. {}", start, end)

    var result = 0.0
    for (factor in start..end) {
        result += (if (factor % 2 == 0L) 1.0 else -1.0) / (1 + 2 * factor)
        if (factor % 100_000 == 0L) yield()
    }
    return result
}

fun warmUp() {

    repeat(5) {
        runBlocking { sumLeibniz(0, 1_000_000) }
    }

}
 */