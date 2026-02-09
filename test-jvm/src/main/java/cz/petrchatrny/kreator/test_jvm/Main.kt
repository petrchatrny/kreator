package cz.petrchatrny.kreator.test_jvm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class TestJvm

fun main(args: Array<String>) {
    runApplication<TestJvm>(*args)
}
