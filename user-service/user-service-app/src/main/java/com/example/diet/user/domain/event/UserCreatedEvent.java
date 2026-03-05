package com.example.diet.user.domain.event;

import com.example.diet.shared.ddd.DomainEvent;
import com.example.diet.user.domain.model.Email;
import com.example.diet.user.domain.model.UserId;
import com.example.diet.user.domain.model.Username;
import lombok.Getter;

/**
 * 用户创建事件
 */
@Getter
public class UserCreatedEvent extends DomainEvent {

    private final UserId userId;
    private final Username username;
    private final Email email;

    public UserCreatedEvent(UserId userId, Username username, Email email) {
        super();
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}
