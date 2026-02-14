# 日志规范（Java 后端）

## 1. 基础原则
- 所有服务统一使用 `observability-logback-spring.xml`。
- 通过 `traceId` 进行跨服务日志关联。
- 日志必须可读、可检索、可追踪。

## 2. 级别规范
- `DEBUG`：开发调试信息，不应影响生产排障主路径。
- `INFO`：关键业务流程节点、访问日志。
- `WARN`：可恢复问题、业务异常（如参数错误、权限不足）。
- `ERROR`：系统异常、外部依赖失败、不可恢复错误。

## 3. 异常日志
- `BusinessException`：
  - 使用 `WARN`
  - 默认不打印堆栈
- 系统异常 / RPC 异常 / 外部调用异常：
  - 使用 `ERROR`
  - 打印堆栈

## 4. 敏感信息脱敏
- 禁止打印：
  - 明文密码
  - 完整 Token / Authorization
  - 完整身份证号 / 手机号 / 邮箱
  - 外部系统 secret / key
- 允许打印时需脱敏（如手机号 `138****0000`）。

## 5. 推荐日志写法
- 使用 key-value 风格，便于检索：
  - `action=...`
  - `userId=...`
  - `resourceId=...`
  - `traceId=...`
- 示例：
  - `log.info("action=create_user userId={} role={}", userId, role);`

## 6. SQL 日志策略
- 仅 `dev` 开启 MyBatis SQL 日志。
- `test/prod` 关闭 SQL 明细，避免日志噪音和敏感数据泄露。

## 7. 访问日志
- 统一由网关 `HttpTraceFilter` 输出单行访问日志，字段：
  - `method`
  - `path`
  - `status`
  - `durationMs`
  - `traceId`
  - `userId`
  - `clientIp`

