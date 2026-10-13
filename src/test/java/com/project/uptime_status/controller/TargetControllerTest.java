package com.project.uptime_status.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.uptime_status.exception.ApiExceptionHandler;
import com.project.uptime_status.exception.UnknownTargetException;
import com.project.uptime_status.service.TargetService;
import com.project.uptime_status.service.TargetService.CheckResult;

@ExtendWith(MockitoExtension.class)
class TargetControllerTest {

	@Mock
	private TargetService targetService;

	@InjectMocks
	private TargetController controller;

	@Test
	void getLastCheck_returnsServiceResult() {
		CheckResult result = new CheckResult(true, 200, 10, Instant.parse("2026-10-12T10:00:00Z"));
		when(targetService.lastCheck("github")).thenReturn(result);

		assertSame(result, controller.getLastCheck("github"));
	}

	@Test
	void check_returnsFreshServiceResult() {
		CheckResult result = new CheckResult(false, null, 5000, Instant.parse("2026-10-12T10:00:00Z"));
		when(targetService.check("github")).thenReturn(result);

		assertSame(result, controller.check("github"));
	}

	@Test
	void apiExceptionHandler_unknownTarget_returnsGenericBody() {
		Map<String, String> body = new ApiExceptionHandler()
				.handleUnknownTargetException(new UnknownTargetException("nope"));

		assertEquals(Map.of("error", "Target not found"), body);
	}
}
