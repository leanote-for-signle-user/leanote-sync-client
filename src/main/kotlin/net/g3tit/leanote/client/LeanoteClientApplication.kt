package net.g3tit.leanote.client

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * @author zhixiao.mzx
 * @date 2019/03/08
 */
@SpringBootApplication
class LeanoteClientApplication

fun main(args: Array<String>) {
	runApplication<LeanoteClientApplication>(*args)
}
