package io.github.gabrielwederson.pay_scheduler_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
public class PaySchedulerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaySchedulerApiApplication.class, args);
	}

}
