package org.femto.jaggqlyapp;

import org.femto.aggqly.AggqlyInterfaceScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "org.femto.aggqly" })
@AggqlyInterfaceScan({})
public class JaggqlyappApplication {

	public static void main(String[] args) {
		SpringApplication.run(JaggqlyappApplication.class, args);
	}

}
