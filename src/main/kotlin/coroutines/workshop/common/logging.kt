package coroutines.workshop.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory

// The one logger of the exercises. Its level and the line format (time, thread, level) are set in src/main/resources/logback.xml.
// Use {} placeholders: logger.info("took {} ms", duration).
//
// Every line starts with WHO logged it, taken as is from the thread name:
//   plain thread:          [worker-3]
//   inside a coroutine:    [main @coroutine#2]      = coroutine#2 running on thread main
// The coroutine part is added by kotlinx.coroutines itself when the JVM runs with -Dkotlinx.coroutines.debug. The flag is
// set once for every run in IntelliJ's Kotlin run-configuration template (Run -> Edit Configurations -> Edit configuration templates -> Kotlin).
val logger: Logger = LoggerFactory.getLogger("exercise")
