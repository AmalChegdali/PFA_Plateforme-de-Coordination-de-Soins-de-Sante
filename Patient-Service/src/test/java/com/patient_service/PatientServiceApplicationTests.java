package com.patient_service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = PatientServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Disabled("Temporairement désactivé - bug connu avec Spring Boot 3.2.4 et JacksonJsonHttpMessageConverter")
class PatientServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
