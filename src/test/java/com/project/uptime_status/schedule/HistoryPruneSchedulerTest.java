package com.project.uptime_status.schedule;

import static org.mockito.Mockito.verify;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.uptime_status.service.TargetService;
import com.project.uptime_status.service.TargetService.Target;

@ExtendWith(MockitoExtension.class)
class HistoryPruneSchedulerTest {

	@Mock
	private TargetService targetService;

	@Test
	void pruneHistory_callsPruneForEachCatalogSlugWithConfiguredKeep() {
		Map<String, Target> catalog = new LinkedHashMap<>();
		catalog.put("github", new Target("GitHub", "https://github.com"));
		catalog.put("google", new Target("Google", "https://google.com"));
		org.mockito.Mockito.when(targetService.targetCatalog()).thenReturn(catalog);

		HistoryPruneScheduler scheduler = new HistoryPruneScheduler(targetService, 3);
		scheduler.pruneHistory();

		verify(targetService).pruneHistory("github", 3);
		verify(targetService).pruneHistory("google", 3);
	}
}
