package com.example.diet.shared.ddd;

import java.time.LocalDateTime;

/**
 * 领域事件基类
 * 领域事件表示领域中发生的重要事情
 */
public abstract class DomainEvent {

    private final LocalDateTime occurredOn;

    protected DomainEvent() {
        this.occurredOn = LocalDateTime.now();
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
