package com.project.uptime_status.schedule;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.uptime_status.queue.ProbeQueue;
import com.project.uptime_status.service.TargetService;
import com.project.uptime_status.service.TargetService.Target;

@ExtendWith(MockitoExtension.class)
class TargetProbeSchedulerTest {

	@Mock
	private TargetService targetService;

	@Mock
	private ProbeQueue probeQueue;

	@Test
	void probeAllTargets_enqueuesEveryCatalogSlug() {
		Map<String, Target> catalog = new LinkedHashMap<>();
		catalog.put("github", new Target("GitHub", "https://github.com"));
		catalog.put("google", new Target("Google", "https://google.com"));
		when(targetService.targetCatalog()).thenReturn(catalog);

		new TargetProbeScheduler(targetService, probeQueue).probeAllTargets();

		verify(probeQueue).enqueue("github");
		verify(probeQueue).enqueue("google");
	}
}
