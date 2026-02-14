# 后端日志统一实施说明

## 目标
- 统一日志输出格式与日志级别策略。
- 建立 HTTP -> Dubbo 全链路 `traceId` 透传。
- 开发环境保留可读文本日志，非开发环境输出文本 + JSON 双通道。
- SQL 日志仅在开发环境开启。

## 已落地内容
1. 新增模块：`observability-starter`
- 自动配置：`ObservabilityAutoConfiguration`
- 配置项：`app.logging.*`
- HTTP 追踪过滤器：`HttpTraceFilter`
- Dubbo trace 过滤器：
  - `DubboConsumerTraceFilter`
  - `DubboProviderTraceFilter`
- logback 模板：`observability-logback-spring.xml`

2. 统一 trace 规范
- HTTP Header：
  - 入站支持：`X-Trace-Id`
  - 出站透传：`X-Trace-Id`
- MDC Keys：
  - `traceId`
  - `userId`
  - `path`
  - `method`
- Dubbo attachment keys：
  - `traceId`
  - `userId`

3. 网关访问日志
- 记录单行 INFO 访问日志：
  - `method`
  - `path`
  - `status`
  - `durationMs`
  - `traceId`
  - `userId`
  - `clientIp`

4. 应用配置统一
- 所有 app 模块启用：
  - `logging.config: classpath:observability-logback-spring.xml`
- 默认级别：
  - `root=INFO`
  - `com.example.diet=INFO`
  - `org.apache.dubbo=WARN`
  - `org.mybatis=WARN`
- `dev` 覆盖：
  - `com.example.diet=DEBUG`
  - `org.apache.dubbo=INFO`
  - `org.mybatis=DEBUG`
- `dev` 中开启 MyBatis SQL 输出：
  - `mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.slf4j.Slf4jImpl`

## 验证建议
1. 请求任意网关接口，检查响应头 `X-Trace-Id`。
2. 自定义传入 `X-Trace-Id`，确认网关与下游服务日志一致。
3. 触发 4xx/5xx，检查日志级别与堆栈行为符合规范。
4. 使用 `dev` 启动确认 SQL 日志可见，`prod/test` 不输出 SQL 明细。

