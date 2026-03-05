package com.example.diet.observability.http;

import com.example.diet.observability.config.ObservabilityLoggingProperties;
import com.example.diet.observability.trace.TraceIdGenerator;
import com.example.diet.observability.trace.TraceMdcKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * HTTP trace filter for request-scoped traceId and access logs.
 */
public class HttpTraceFilter extends OncePerRequestFilter {

    private static final Logger ACCESS_LOG = LoggerFactory.getLogger("http.access");
    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{8,64}$");

    private final ObservabilityLoggingProperties properties;

    public HttpTraceFilter(ObservabilityLoggingProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String traceId = resolveTraceId(request.getHeader(TraceMdcKeys.TRACE_ID_HEADER));
        long start = System.currentTimeMillis();

        response.setHeader(TraceMdcKeys.TRACE_ID_HEADER, traceId);
        MDC.put(TraceMdcKeys.TRACE_ID, traceId);
        MDC.put(TraceMdcKeys.PATH, path);
        MDC.put(TraceMdcKeys.METHOD, method);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            String userId = resolveUserId(request);
            if (hasText(userId)) {
                MDC.put(TraceMdcKeys.USER_ID, userId);
            }

            if (shouldLogRequest(path)) {
                ACCESS_LOG.info(
                        "access method={} path={} status={} durationMs={} traceId={} userId={} clientIp={}",
                        method,
                        path,
                        response.getStatus(),
                        durationMs,
                        traceId,
                        hasText(userId) ? userId : "-",
                        resolveClientIp(request)
                );
            }

            MDC.remove(TraceMdcKeys.USER_ID);
            MDC.remove(TraceMdcKeys.METHOD);
            MDC.remove(TraceMdcKeys.PATH);
            MDC.remove(TraceMdcKeys.TRACE_ID);
        }
    }

    private boolean shouldLogRequest(String path) {
        if (!properties.getRequestLog().isEnabled()) {
            return false;
        }
        for (String excluded : properties.getRequestLog().getExcludePaths()) {
            if (!hasText(excluded)) {
                continue;
            }
            if (path.equals(excluded) || path.startsWith(excluded.endsWith("/") ? excluded : excluded + "/")) {
                return false;
            }
        }
        return true;
    }

    private String resolveTraceId(String incomingTraceId) {
        if (!hasText(incomingTraceId)) {
            return TraceIdGenerator.generate();
        }
        if (!TRACE_ID_PATTERN.matcher(incomingTraceId).matches()) {
            return TraceIdGenerator.generate();
        }
        return incomingTraceId;
    }

    private String resolveUserId(HttpServletRequest request) {
        Object userId = request.getAttribute(TraceMdcKeys.REQUEST_USER_ID_ATTRIBUTE);
        if (userId != null) {
            return String.valueOf(userId);
        }
        return MDC.get(TraceMdcKeys.USER_ID);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (hasText(forwardedFor)) {
            int commaIndex = forwardedFor.indexOf(',');
            return commaIndex > 0 ? forwardedFor.substring(0, commaIndex).trim() : forwardedFor.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
