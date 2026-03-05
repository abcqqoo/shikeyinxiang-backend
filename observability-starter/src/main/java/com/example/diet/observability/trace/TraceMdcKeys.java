package com.example.diet.observability.trace;

/**
 * MDC and trace-related key constants.
 */
public final class TraceMdcKeys {

    public static final String TRACE_ID = "traceId";
    public static final String USER_ID = "userId";
    public static final String PATH = "path";
    public static final String METHOD = "method";

    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_ATTACHMENT = "traceId";
    public static final String USER_ID_ATTACHMENT = "userId";
    public static final String REQUEST_USER_ID_ATTRIBUTE = "CURRENT_USER_ID";

    private TraceMdcKeys() {
    }
}
