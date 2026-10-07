package com.project.uptime_status.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.uptime_status.exception.UnknownTargetException;
import com.project.uptime_status.persistence.CheckResultEntity;
import com.project.uptime_status.persistence.CheckResultHistoryEntity;
import com.project.uptime_status.repository.CheckResultHistoryRepository;
import com.project.uptime_status.repository.CheckResultRepository;
import com.project.uptime_status.service.TargetService.CheckResult;
import com.project.uptime_status.service.TargetService.Target;

@ExtendWith(MockitoExtension.class)
class TargetServiceTest {

	@Mock
	private CheckResultRepository checkResultRepository;

	@Mock
	private CheckResultHistoryRepository checkResultHistoryRepository;

	@InjectMocks
	private TargetService targetService;

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
		when(checkResultRepository.findById("github")).thenReturn(Optional.empty());

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
		when(checkResultRepository.save(any(CheckResultEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(checkResultHistoryRepository.save(any(CheckResultHistoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CheckResult result = targetService.check("github");

		assertNotNull(result.observedAt());
		assertTrue(result.latencyMs() >= 0);
		if (result.statusCode() != null) {
			assertEquals(result.statusCode() < 400, result.up());
		} else {
			assertEquals(false, result.up());
		}

		ArgumentCaptor<CheckResultEntity> captor = ArgumentCaptor.forClass(CheckResultEntity.class);
		verify(checkResultRepository).save(captor.capture());
		CheckResultEntity saved = captor.getValue();
		assertEquals("github", saved.getSlug());

		ArgumentCaptor<CheckResultHistoryEntity> historyCaptor = ArgumentCaptor.forClass(CheckResultHistoryEntity.class);
		verify(checkResultHistoryRepository).save(historyCaptor.capture());
		assertEquals("github", historyCaptor.getValue().getSlug());

		when(checkResultRepository.findById("github")).thenReturn(Optional.of(saved));
		assertEquals(result, targetService.lastCheck("github"));
	}

	@Test
	void checkAll_storesLastCheckForCatalogTargets() {
		when(checkResultRepository.save(any(CheckResultEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(checkResultHistoryRepository.save(any(CheckResultHistoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

		targetService.checkAll();

		ArgumentCaptor<CheckResultEntity> captor = ArgumentCaptor.forClass(CheckResultEntity.class);
		verify(checkResultRepository, times(2)).save(captor.capture());
		var slugs = captor.getAllValues().stream().map(CheckResultEntity::getSlug).toList();
		assertTrue(slugs.contains("github"));
		assertTrue(slugs.contains("google"));

		verify(checkResultHistoryRepository, times(2)).save(any(CheckResultHistoryEntity.class));
	}
}
