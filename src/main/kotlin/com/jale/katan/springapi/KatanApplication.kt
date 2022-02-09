package com.jale.katan.springapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KatanApplication

fun main(args: Array<String>) {
	runApplication<KatanApplication>(*args)
}
