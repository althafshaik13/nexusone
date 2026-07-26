package dev.nexusone.ticket_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class TicketServiceApplication {

	static {
		// The JVM's default timezone on this host resolves to the deprecated
		// alias "Asia/Calcutta", which the Postgres JDBC driver sends verbatim
		// as a session TimeZone on connect — Postgres rejects it outright.
		// Run in UTC to sidestep host-timezone-dependent connection failures.
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	public static void main(String[] args) {
		SpringApplication.run(TicketServiceApplication.class, args);
	}

}
