package com.example.diet.nutrition.infrastructure.event;

import com.example.diet.nutrition.application.NutritionApplicationService;
import com.example.diet.nutrition.support.WeekStartUtils;
import com.example.diet.observability.trace.TraceMdcKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Locale;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Consume diet record change events from Redis Streams and refresh cached health reports.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DietRecordChangedStreamConsumer {

    private static final int BATCH_SIZE = 20;

    private final StringRedisTemplate redisTemplate;
    private final NutritionApplicationService nutritionApplicationService;

    @Value("${app.events.diet-record-changed.stream:diet_record_changed}")
    private String streamKey;

    @Value("${spring.application.name:nutrition-service}")
    private String group;

    private volatile boolean groupInitialized = false;
    private final Object groupInitLock = new Object();

    @Scheduled(fixedDelayString = "${app.events.diet-record-changed.poll-delay-ms:1000}")
    public void poll() {
        try {
            ensureGroup();

            StreamOperations<String, String, String> ops = redisTemplate.opsForStream();
            Consumer consumer = Consumer.from(group, group);

            // 1) Pending first (retry un-acked messages for this consumer).
            List<MapRecord<String, String, String>> pending = ops.read(
                    consumer,
                    StreamReadOptions.empty().count(BATCH_SIZE),
                    StreamOffset.create(streamKey, ReadOffset.from("0-0"))
            );
            handle(pending, ops);

            // 2) New messages (block briefly).
            List<MapRecord<String, String, String>> messages = ops.read(
                    consumer,
                    StreamReadOptions.empty().count(BATCH_SIZE).block(Duration.ofSeconds(1)),
                    StreamOffset.create(streamKey, ReadOffset.lastConsumed())
            );
            handle(messages, ops);
        } catch (Exception e) {
            log.warn("action=poll_diet_record_changed_failed stream={} group={} message={}",
                    streamKey, group, e.getMessage());
        }
    }

    private void handle(List<MapRecord<String, String, String>> records, StreamOperations<String, String, String> ops) {
        if (records == null || records.isEmpty()) {
            return;
        }

        LocalDate currentWeekStart = WeekStartUtils.resolveWeekStart(LocalDate.now());

        for (MapRecord<String, String, String> record : records) {
            Map<String, String> body = record.getValue();

            String userIdStr = body.get("userId");
            if (!StringUtils.hasText(userIdStr)) {
                ackSafely(ops, record.getId());
                continue;
            }

            Long userId;
            try {
                userId = Long.parseLong(userIdStr.trim());
            } catch (Exception e) {
                log.warn("action=consume_diet_record_changed_invalid_user_id stream={} messageId={} userId={} message={}",
                        streamKey, record.getId(), userIdStr, e.getMessage());
                ackSafely(ops, record.getId());
                continue;
            }

            LocalDate oldDate = parseDate(body.get("oldDate"));
            LocalDate newDate = parseDate(body.get("newDate"));

            Set<LocalDate> affectedWeekStarts = new HashSet<>();
            addAffectedWeeks(affectedWeekStarts, oldDate, currentWeekStart);
            addAffectedWeeks(affectedWeekStarts, newDate, currentWeekStart);

            String traceId = body.get("traceId");
            try {
                if (StringUtils.hasText(traceId)) {
                    MDC.put(TraceMdcKeys.TRACE_ID, traceId);
                }
                MDC.put(TraceMdcKeys.USER_ID, String.valueOf(userId));

                for (LocalDate weekStart : affectedWeekStarts) {
                    nutritionApplicationService.refreshHealthReport(userId, weekStart);
                }

                ackSafely(ops, record.getId());
            } catch (Exception e) {
                // Keep the message pending for retry.
                log.warn("action=consume_diet_record_changed_failed stream={} messageId={} userId={} message={}",
                        streamKey, record.getId(), userId, e.getMessage());
            } finally {
                MDC.remove(TraceMdcKeys.TRACE_ID);
                MDC.remove(TraceMdcKeys.USER_ID);
            }
        }
    }

    private void addAffectedWeeks(Set<LocalDate> affectedWeekStarts, LocalDate date, LocalDate currentWeekStart) {
        if (date == null) {
            return;
        }
        LocalDate weekStart = WeekStartUtils.resolveWeekStart(date);
        affectedWeekStarts.add(weekStart);

        // scoreChange depends on last week; so week N change affects week N and week N+1.
        // We skip future-week refresh to avoid unnecessary work.
        LocalDate nextWeekStart = weekStart.plusWeeks(1);
        if (!nextWeekStart.isAfter(currentWeekStart)) {
            affectedWeekStarts.add(nextWeekStart);
        }
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void ackSafely(StreamOperations<String, String, String> ops, RecordId id) {
        try {
            ops.acknowledge(streamKey, group, id);
        } catch (Exception e) {
            log.warn("action=ack_diet_record_changed_failed stream={} group={} messageId={} message={}",
                    streamKey, group, id, e.getMessage());
        }
    }

    private void ensureGroup() {
        if (groupInitialized) {
            return;
        }
        synchronized (groupInitLock) {
            if (groupInitialized) {
                return;
            }

            StreamOperations<String, String, String> ops = redisTemplate.opsForStream();

            try {
                ops.createGroup(streamKey, ReadOffset.latest(), group);
                groupInitialized = true;
                return;
            } catch (Exception e) {
                if (isBusyGroup(e)) {
                    groupInitialized = true;
                    return;
                }
            }

            try {
                Map<String, String> initBody = new HashMap<>();
                initBody.put("eventType", "init");
                RecordId initId = ops.add(StreamRecords.newRecord().in(streamKey).ofMap(initBody));
                ops.createGroup(streamKey, ReadOffset.latest(), group);
                ops.delete(streamKey, initId);
                groupInitialized = true;
            } catch (Exception e) {
                if (isBusyGroup(e)) {
                    groupInitialized = true;
                    return;
                }
                log.warn("action=init_diet_record_changed_group_failed stream={} group={} message={}",
                        streamKey, group, buildExceptionMessage(e));
            }
        }
    }

    private boolean isBusyGroup(Throwable e) {
        Throwable current = e;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String upper = message.toUpperCase(Locale.ROOT);
                if (upper.contains("BUSYGROUP")
                        || upper.contains("CONSUMER GROUP NAME ALREADY EXISTS")
                        || upper.contains("GROUP NAME ALREADY EXISTS")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }

    private String buildExceptionMessage(Throwable e) {
        if (e == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        Throwable current = e;
        int depth = 0;
        while (current != null && depth < 4) {
            if (builder.length() > 0) {
                builder.append(" <- ");
            }
            builder.append(current.getClass().getSimpleName()).append(":");
            builder.append(current.getMessage() != null ? current.getMessage() : "");
            current = current.getCause();
            depth++;
        }
        return builder.toString();
    }

}
