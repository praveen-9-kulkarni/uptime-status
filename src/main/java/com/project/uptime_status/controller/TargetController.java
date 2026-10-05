package com.project.uptime_status.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.project.uptime_status.service.TargetService;
import com.project.uptime_status.service.TargetService.CheckResult;

@RestController
@RequestMapping("/targets")
public class TargetController {

    private final TargetService targetService;

    public TargetController(TargetService targetService) {

        this.targetService = targetService;
    }

    @GetMapping("/{key}")
    public CheckResult getLastCheck(@PathVariable String key) {

        return targetService.lastCheck(key);
    }
}
