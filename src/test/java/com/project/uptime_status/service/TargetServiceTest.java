package com.project.uptime_status.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.project.uptime_status.exception.UnknownTargetException;
import com.project.uptime_status.service.TargetService.Target;

class TargetServiceTest {

	private final TargetService targetService = new TargetService();

	@Test
	void getTarget_knownKey_returnsCatalogEntry() {
		Target target = targetService.getTarget("github");

		assertEquals("GitHub", target.name());
		assertEquals("https://github.com", target.url());
	}

	@Test
	void getTarget_unknownKey_throwsUnknownTargetException() {
		assertThrows(UnknownTargetException.class, () -> targetService.getTarget("nope"));
	}
}
