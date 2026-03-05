package com.example.diet.user.domain.event;

import com.example.diet.shared.ddd.DomainEvent;
import com.example.diet.user.domain.model.UserId;
import lombok.Getter;

/**
 * 用户禁用事件
 */
@Getter
public class UserDisabledEvent extends DomainEvent {

    private final UserId userId;

    public UserDisabledEvent(UserId userId) {
        super();
        this.userId = userId;
    }
}
