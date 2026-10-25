package com.bequian.gamerzone;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "gamerzone.user.password=test-user",
    "gamerzone.admin.password=test-admin",
    "gamerzone.organizador.password=test-org"
})

class GamerzoneApplicationTests {

	@Test
	void contextLoads() {
	}

}
