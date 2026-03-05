package com.example.diet.shared.ddd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类
 * 聚合根是领域模型的入口点，负责维护聚合内的一致性
 *
 * @param <ID> 聚合根的 ID 类型
 */
public abstract class AggregateRoot<ID extends Identifier<?>> implements Entity<ID> {

    /**
     * 聚合根 ID
     */
    protected ID id;

    /**
     * 领域事件列表（不参与序列化）
     */
    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    protected AggregateRoot() {
    }

    protected AggregateRoot(ID id) {
        this.id = id;
    }

    @Override
    public ID getId() {
        return id;
    }

    /**
     * 注册一个领域事件
     */
    protected void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * 获取所有未发布的领域事件
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 清空领域事件（在事件发布后调用）
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
