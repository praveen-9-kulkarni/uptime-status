package com.project.uptime_status.queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.uptime_status.service.TargetService;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
class ProbeWorkerTest {

	@Mock
	private ProbeQueue probeQueue;

	@Mock
	private TargetService targetService;

	private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
	private ProbeWorker worker;

	@BeforeEach
	void setUp() {
		worker = new ProbeWorker(probeQueue, targetService, registry);
	}

	@Test
	void work_emptyQueue_doesNothing() {
		when(probeQueue.dequeue()).thenReturn(null);

		worker.work();

		assertEquals(0.0, registry.get("uptime.worker.errors").counter().count());
	}

	@Test
	void work_checkThrows_incrementsErrorCounter() {
		when(probeQueue.dequeue()).thenReturn("github");
		doThrow(new RuntimeException("db down")).when(targetService).check("github");

		worker.work();

		assertEquals(1.0, registry.get("uptime.worker.errors").counter().count());
	}

	@Test
	void work_checkSucceeds_doesNotIncrementErrorCounter() {
		when(probeQueue.dequeue()).thenReturn("github");

		worker.work();

		verify(targetService).check("github");
		assertEquals(0.0, registry.get("uptime.worker.errors").counter().count());
	}
}
