package com.example.diet.record.infrastructure.event;

import com.example.diet.record.application.event.DietRecordChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Publish diet record changes to Redis Streams after the DB transaction commits.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DietRecordChangedRedisStreamPublisher {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.events.diet-record-changed.stream:diet_record_changed}")
    private String streamKey;

    @Value("${app.events.diet-record-changed.maxlen:10000}")
    private long streamMaxLen;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDietRecordChanged(DietRecordChangedEvent event) {
        Map<String, String> body = new HashMap<>();
        body.put("eventType", "diet_record_changed");
        body.put("userId", String.valueOf(event.getUserId()));
        body.put("action", event.getAction());
        if (event.getOldDate() != null) {
            body.put("oldDate", event.getOldDate().toString());
        }
        if (event.getNewDate() != null) {
            body.put("newDate", event.getNewDate().toString());
        }
        if (event.getRecordId() != null) {
            body.put("recordId", String.valueOf(event.getRecordId()));
        }
        if (StringUtils.hasText(event.getTraceId())) {
            body.put("traceId", event.getTraceId());
        }
        body.put("occurredAt", Instant.now().toString());

        try {
            RecordId messageId = redisTemplate.opsForStream()
                    .add(StreamRecords.newRecord().in(streamKey).ofMap(body));

            // Stream does not support per-message TTL; use XTRIM MAXLEN (~) to control growth.
            if (streamMaxLen > 0) {
                redisTemplate.opsForStream().trim(streamKey, streamMaxLen, true);
            }
            log.debug("action=publish_diet_record_changed stream={} messageId={} userId={} action={}",
                    streamKey, messageId, event.getUserId(), event.getAction());
        } catch (Exception e) {
            // Do not fail the business flow when the event bus is temporarily unavailable.
            log.warn("action=publish_diet_record_changed_failed stream={} userId={} action={} message={}",
                    streamKey, event.getUserId(), event.getAction(), e.getMessage());
        }
    }
}
