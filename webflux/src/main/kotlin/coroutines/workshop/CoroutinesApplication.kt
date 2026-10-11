package coroutines.workshop

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CoroutinesApplication

fun main(args: Array<String>) {
	// the coroutine's name next to the thread's in every log line: [reactor-http-nio-2 @coroutine#4]
	// (start with -Dkotlinx.coroutines.debug=off to switch it off)
	if (System.getProperty("kotlinx.coroutines.debug") == null) System.setProperty("kotlinx.coroutines.debug", "on")
	runApplication<CoroutinesApplication>(*args)
}
