package com.example.diet.record.application.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Integration event published within diet-service after a diet record change is committed.
 * It will be forwarded to Redis Streams for cross-service consumption.
 */
@Getter
@ToString
@AllArgsConstructor
public class DietRecordChangedEvent {

    private final Long userId;
    private final LocalDate oldDate;
    private final LocalDate newDate;
    private final String action;
    private final Long recordId;
    private final String traceId;
}

