package com.example.diet.shared.cqrs;

import java.io.Serializable;

/**
 * 命令标记接口
 * 命令表示写操作，会改变系统状态
 * 命名规范: 动词 + 名词 + Command (如 CreateUserCommand)
 */
public interface Command extends Serializable {
}
