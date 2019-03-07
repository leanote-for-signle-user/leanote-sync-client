package net.g3tit.leanote.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LeanoteClientApplication

fun main(args: Array<String>) {
	runApplication<LeanoteClientApplication>(*args)
}
