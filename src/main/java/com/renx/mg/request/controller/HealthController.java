package com.renx.mg.request.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

	/**
	 * Endpoint para health checks del balanceador (ALB, Elastic Beanstalk).
	 * Configurar el path como: /health
	 */
	@GetMapping("/health")
	public ResponseEntity<String> health() {
		return ResponseEntity.ok("ok");
	}
}
