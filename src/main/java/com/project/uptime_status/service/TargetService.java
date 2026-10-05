package com.project.uptime_status.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.project.uptime_status.exception.UnknownTargetException;

@Service
public class TargetService {

    public record Target(String name, String url) {}

    private final Map<String, Target> TARGETS = Map.of(
        "github", new Target("GitHub", "https://github.com")
    );

    public Target getTarget(String key) {

        Target target = resolveTargetOrThrow(key);
        return target;
    }

    private Target resolveTargetOrThrow(String key) {
    
        Target target = TARGETS.get(key);
        if (target == null) {
            throw new UnknownTargetException(key);
        }
        return target;
    }
}
