package org.femto.jaggqlyapp;

import org.femto.jaggqlyapp.aggqly.AggqlyInterfaceScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@AggqlyInterfaceScan({})
public class JaggqlyappApplication {

	public static void main(String[] args) {
		SpringApplication.run(JaggqlyappApplication.class, args);
	}

}
