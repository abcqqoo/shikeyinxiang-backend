package com.example.diet.observability.trace;

import java.util.UUID;

/**
 * Trace ID generator.
 */
public final class TraceIdGenerator {

    private TraceIdGenerator() {
    }

    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
