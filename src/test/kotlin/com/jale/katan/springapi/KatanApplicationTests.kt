package com.jale.katan.springapi

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KatanApplicationTests(@Autowired val restTemplate: TestRestTemplate) {

	@Test
	fun testCreateAndRemoveGameEndpoint() {
		val result = restTemplate.postForEntity<String>("/games")

		restTemplate.delete("/game/${result.body}")
	}
}
