package com.example.diet.observability.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.Locale;

/**
 * Applies app.logging.mode to root appenders at runtime.
 */
public class LogModeCustomizer implements ApplicationRunner {

    private static final String MODE_TEXT = "text";
    private static final String MODE_JSON = "json";
    private static final String MODE_DUAL = "dual";

    private final ObservabilityLoggingProperties loggingProperties;

    public LogModeCustomizer(ObservabilityLoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!(LoggerFactory.getILoggerFactory() instanceof LoggerContext context)) {
            return;
        }

        Logger root = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        String mode = normalizeMode(loggingProperties.getMode());
        switch (mode) {
            case MODE_TEXT -> root.detachAppender("JSON_FILE");
            case MODE_JSON -> root.detachAppender("CONSOLE");
            default -> {
                // dual mode keeps both appenders.
            }
        }
    }

    private String normalizeMode(String mode) {
        if (mode == null) {
            return MODE_DUAL;
        }
        String normalized = mode.trim().toLowerCase(Locale.ROOT);
        if (MODE_TEXT.equals(normalized) || MODE_JSON.equals(normalized) || MODE_DUAL.equals(normalized)) {
            return normalized;
        }
        return MODE_DUAL;
    }
}
