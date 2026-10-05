package com.project.uptime_status.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.project.uptime_status.service.TargetService;
import com.project.uptime_status.service.TargetService.CheckResult;
import com.project.uptime_status.service.TargetService.Target;

@Controller
@RequestMapping("/status")
public class StatusPageController {

    public record StatusRow(String key, Target target, CheckResult lastCheck) {}

    private final TargetService targetService;

    public StatusPageController(TargetService targetService) {
        this.targetService = targetService;
    }

    @GetMapping
    public String getStatusPage(Model model) {
        List<StatusRow> rows = new ArrayList<>();
        for (Map.Entry<String, Target> entry : targetService.targetCatalog().entrySet()) {
            String key = entry.getKey();
            rows.add(new StatusRow(key, entry.getValue(), targetService.lastCheck(key)));
        }
        model.addAttribute("rows", rows);
        return "status";
    }
}
