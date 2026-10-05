package com.project.uptime_status.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.project.uptime_status.exception.UnknownTargetException;
import com.project.uptime_status.service.TargetService.CheckResult;
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

	@Test
	void lastCheck_beforeAnyCheck_returnsNull() {
		assertNull(targetService.lastCheck("github"));
	}

	@Test
	void lastCheck_unknownKey_throwsUnknownTargetException() {
		assertThrows(UnknownTargetException.class, () -> targetService.lastCheck("nope"));
	}

	@Test
	void check_unknownKey_throwsUnknownTargetException() {
		assertThrows(UnknownTargetException.class, () -> targetService.check("nope"));
	}

	@Test
	void check_knownKey_storesResultForLastCheck() {
		CheckResult result = targetService.check("github");

		assertNotNull(result.observedAt());
		assertTrue(result.latencyMs() >= 0);
		if (result.statusCode() != null) {
			assertEquals(result.statusCode() < 400, result.up());
		} else {
			assertEquals(false, result.up());
		}
		assertEquals(result, targetService.lastCheck("github"));
	}
}
