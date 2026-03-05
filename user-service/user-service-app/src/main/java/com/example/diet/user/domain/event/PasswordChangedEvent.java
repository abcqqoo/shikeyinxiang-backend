package com.example.diet.user.domain.event;

import com.example.diet.shared.ddd.DomainEvent;
import com.example.diet.user.domain.model.UserId;
import lombok.Getter;

/**
 * 密码修改事件
 */
@Getter
public class PasswordChangedEvent extends DomainEvent {

    private final UserId userId;

    public PasswordChangedEvent(UserId userId) {
        super();
        this.userId = userId;
    }
}
