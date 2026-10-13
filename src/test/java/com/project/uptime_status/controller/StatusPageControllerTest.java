package com.project.uptime_status.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;

import com.project.uptime_status.controller.StatusPageController.StatusRow;
import com.project.uptime_status.enums.State;
import com.project.uptime_status.service.TargetService;
import com.project.uptime_status.service.TargetService.CheckResult;
import com.project.uptime_status.service.TargetService.Target;

@ExtendWith(MockitoExtension.class)
class StatusPageControllerTest {

	private static final Instant NOW = Instant.parse("2026-10-12T10:00:00Z");

	@Mock
	private TargetService targetService;

	private StatusPageController controller;

	@BeforeEach
	void setUp() {
		controller = new StatusPageController(targetService, Clock.fixed(NOW, ZoneOffset.UTC));
	}

	@SuppressWarnings("unchecked")
	private List<StatusRow> rowsOf(ConcurrentModel model) {
		return (List<StatusRow>) model.getAttribute("rows");
	}

	@Test
	void getStatusPage_staleTarget_hasAgeTextBreakdown() {
		Target github = new Target("GitHub", "https://github.com");
		CheckResult old = new CheckResult(true, 200, 50, NOW.minusSeconds(3600 + 2 * 60 + 3));
		when(targetService.targetCatalog()).thenReturn(Map.of("github", github));
		when(targetService.lastCheck("github")).thenReturn(old);
		when(targetService.stateOf(old)).thenReturn(State.STALE);

		ConcurrentModel model = new ConcurrentModel();
		String view = controller.getStatusPage(model);

		assertEquals("status", view);
		StatusRow row = rowsOf(model).get(0);
		assertEquals(State.STALE, row.state());
		assertEquals("Checked 1 hours, 2 minutes, 3 seconds ago", row.ageText());
	}

	@Test
	void getStatusPage_freshAndNoDataTargets_haveNoAgeText() {
		Map<String, Target> catalog = new LinkedHashMap<>();
		catalog.put("github", new Target("GitHub", "https://github.com"));
		catalog.put("google", new Target("Google", "https://google.com"));
		CheckResult fresh = new CheckResult(true, 200, 50, NOW.minusSeconds(5));
		when(targetService.targetCatalog()).thenReturn(catalog);
		when(targetService.lastCheck("github")).thenReturn(fresh);
		when(targetService.lastCheck("google")).thenReturn(null);
		when(targetService.stateOf(fresh)).thenReturn(State.UP);
		when(targetService.stateOf(null)).thenReturn(State.NO_DATA);

		ConcurrentModel model = new ConcurrentModel();
		controller.getStatusPage(model);

		List<StatusRow> rows = rowsOf(model);
		assertEquals(2, rows.size());
		assertEquals(State.UP, rows.get(0).state());
		assertNull(rows.get(0).ageText());
		assertEquals(State.NO_DATA, rows.get(1).state());
		assertNull(rows.get(1).ageText());
	}

	@Test
	void checkNow_runsCheckAndRedirectsToStatus() {
		String view = controller.checkNow("github");

		verify(targetService).check("github");
		assertEquals("redirect:/status", view);
	}

	@Test
	void getHistoryPage_populatesModelAndReturnsHistoryView() {
		Target github = new Target("GitHub", "https://github.com");
		List<CheckResult> history = List.of(new CheckResult(true, 200, 50, NOW));
		when(targetService.getTarget("github")).thenReturn(github);
		when(targetService.recentHistory("github", 20)).thenReturn(history);

		ConcurrentModel model = new ConcurrentModel();
		String view = controller.getHistoryPage("github", model);

		assertEquals("status-history", view);
		assertEquals("github", model.getAttribute("key"));
		assertSame(github, model.getAttribute("target"));
		assertSame(history, model.getAttribute("history"));
	}
}
