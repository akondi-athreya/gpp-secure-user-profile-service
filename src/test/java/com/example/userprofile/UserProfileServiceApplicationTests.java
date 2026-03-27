package com.example.userprofile;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.cloud.vault.enabled=false",
		"db.username=sa",
		"db.password=password123",
		"api.signing-key=abcdefghijklmnopqrstuvwxyz123456"
})
class UserProfileServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
