package com.example.diet.shared.ddd;

import java.io.Serializable;
import java.util.Objects;

/**
 * ID 值对象基类
 * 所有实体的 ID 都应该继承此类
 *
 * @param <T> ID 的实际类型 (Long, String, UUID 等)
 */
public abstract class Identifier<T> implements ValueObject, Serializable {

    private final T value;

    protected Identifier(T value) {
        if (value == null) {
            throw new IllegalArgumentException("Identifier value cannot be null");
        }
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier<?> that = (Identifier<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
