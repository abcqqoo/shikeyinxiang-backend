package com.example.diet.shared.ddd;

/**
 * 实体标记接口
 * 实体通过唯一标识符来区分
 *
 * @param <ID> 实体的 ID 类型
 */
public interface Entity<ID extends Identifier<?>> {

    /**
     * 获取实体的唯一标识符
     */
    ID getId();
}
