package com.project.uptime_status.controller;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.project.uptime_status.service.TargetService;
import com.project.uptime_status.service.TargetService.CheckResult;
import com.project.uptime_status.service.TargetService.Target;

import com.project.uptime_status.enums.State;

@Controller
@RequestMapping("/status")
public class StatusPageController {

    public record StatusRow(String key, Target target, CheckResult lastCheck, State state, String ageText) {}

    private final TargetService targetService;
    private final Clock clock;
    
    public StatusPageController(TargetService targetService, Clock clock) {

        this.targetService = targetService;
        this.clock = clock;
    }

    @GetMapping
    public String getStatusPage(Model model) {
        List<StatusRow> rows = new ArrayList<>();
        for (Map.Entry<String, Target> entry : targetService.targetCatalog().entrySet()) {
            String key = entry.getKey();
            CheckResult lastCheck = targetService.lastCheck(key);
            State state = targetService.stateOf(lastCheck);
            String ageText = state == State.STALE ? age(lastCheck.observedAt(), clock.instant()) : null;
            rows.add(new StatusRow(key, entry.getValue(), lastCheck, state, ageText));
        }
        model.addAttribute("rows", rows);
        return "status";
    }

    @PostMapping("/{key}/check")
    public String checkNow(@PathVariable String key) {
        targetService.check(key);
        return "redirect:/status";
    }

    @GetMapping("/{key}/history")
    public String getHistoryPage(@PathVariable String key, Model model) {

        Target target = targetService.getTarget(key);
        List<CheckResult> history = targetService.recentHistory(key, 20);
        model.addAttribute("key", key);
        model.addAttribute("target", target);
        model.addAttribute("history", history);
        return "status-history";
    }

    private String age(Instant observedAt, Instant now) {
        
        Duration d = Duration.between(observedAt, now);
        return "Checked %d hours, %d minutes, %d seconds ago"
               .formatted(d.toHours(), d.toMinutesPart(), d.toSecondsPart());
    }
}
