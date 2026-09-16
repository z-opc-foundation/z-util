package com.zifang.util.core.resilience;

/**
 * 熔断器开启。
 */
public class CircuitOpenException extends ResilienceException {
    public CircuitOpenException(String message) {
        super(message);
    }
}
