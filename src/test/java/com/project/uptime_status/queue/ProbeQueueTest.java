package com.project.uptime_status.queue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.project.uptime_status.exception.UnknownTargetException;
import com.project.uptime_status.service.TargetService;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@ExtendWith(MockitoExtension.class)
class ProbeQueueTest {

	private static final String QUEUE_KEY = "uptime:probe";

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ListOperations<String, String> listOps;

	@Mock
	private TargetService targetService;

	private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
	private ProbeQueue queue;

	@BeforeEach
	void setUp() {
		queue = new ProbeQueue(redisTemplate, targetService, registry);
	}

	@Test
	void enqueue_knownKey_pushesToLeftOfQueue() {
		when(redisTemplate.opsForList()).thenReturn(listOps);

		queue.enqueue("github");

		verify(listOps).leftPush(QUEUE_KEY, "github");
	}

	@Test
	void enqueue_unknownKey_throwsAndDoesNotPush() {
		when(targetService.getTarget("nope")).thenThrow(new UnknownTargetException("nope"));

		assertThrows(UnknownTargetException.class, () -> queue.enqueue("nope"));

		verify(redisTemplate, never()).opsForList();
	}

	@Test
	void dequeue_returnsRightPopValue() {
		when(redisTemplate.opsForList()).thenReturn(listOps);
		when(listOps.rightPop(QUEUE_KEY)).thenReturn("google");

		assertEquals("google", queue.dequeue());
	}

	@Test
	void dequeue_emptyQueue_returnsNull() {
		when(redisTemplate.opsForList()).thenReturn(listOps);
		when(listOps.rightPop(QUEUE_KEY)).thenReturn(null);

		assertEquals(null, queue.dequeue());
	}

	@Test
	void queueSizeGauge_reportsRedisListLength() {
		when(redisTemplate.opsForList()).thenReturn(listOps);
		when(listOps.size(QUEUE_KEY)).thenReturn(7L);

		assertEquals(7.0, registry.get("uptime.probe.queue.size").gauge().value());
	}

	@Test
	void queueSizeGauge_nullSize_reportsZero() {
		when(redisTemplate.opsForList()).thenReturn(listOps);
		when(listOps.size(QUEUE_KEY)).thenReturn(null);

		assertEquals(0.0, registry.get("uptime.probe.queue.size").gauge().value());
	}

	@Test
	void queueSizeGauge_redisFailure_reportsNaNInsteadOfThrowing() {
		when(redisTemplate.opsForList()).thenThrow(new RuntimeException("redis down"));

		assertTrue(Double.isNaN(registry.get("uptime.probe.queue.size").gauge().value()));
	}
}
