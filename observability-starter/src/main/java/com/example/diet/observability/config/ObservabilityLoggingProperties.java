package com.example.diet.observability.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Unified logging properties.
 */
@ConfigurationProperties(prefix = "app.logging")
public class ObservabilityLoggingProperties {

    /**
     * text | json | dual
     */
    private String mode = "dual";

    private Json json = new Json();

    private RequestLog requestLog = new RequestLog();

    public static class Json {
        private String path = "logs";

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class RequestLog {
        private boolean enabled = true;
        private List<String> excludePaths = new ArrayList<>(List.of("/actuator/health", "/actuator/info"));

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getExcludePaths() {
            return excludePaths;
        }

        public void setExcludePaths(List<String> excludePaths) {
            this.excludePaths = excludePaths;
        }
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Json getJson() {
        return json;
    }

    public void setJson(Json json) {
        this.json = json;
    }

    public RequestLog getRequestLog() {
        return requestLog;
    }

    public void setRequestLog(RequestLog requestLog) {
        this.requestLog = requestLog;
    }
}
