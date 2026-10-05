package com.project.uptime_status.exception;

public class UnknownTargetException extends RuntimeException {

    public UnknownTargetException(String key) {
        
        super("Unknown target: " + key);
    }

}
