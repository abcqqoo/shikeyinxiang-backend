package com.example.diet.observability.dubbo;

import com.example.diet.observability.trace.TraceIdGenerator;
import com.example.diet.observability.trace.TraceMdcKeys;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.MDC;

/**
 * Provider-side trace restore filter.
 */
@Activate(group = CommonConstants.PROVIDER, order = -12000)
public class DubboProviderTraceFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String previousTraceId = MDC.get(TraceMdcKeys.TRACE_ID);
        String previousUserId = MDC.get(TraceMdcKeys.USER_ID);

        String incomingTraceId = RpcContext.getServiceContext().getAttachment(TraceMdcKeys.TRACE_ID_ATTACHMENT);
        if (!hasText(incomingTraceId)) {
            incomingTraceId = TraceIdGenerator.generate();
        }
        MDC.put(TraceMdcKeys.TRACE_ID, incomingTraceId);

        String incomingUserId = RpcContext.getServiceContext().getAttachment(TraceMdcKeys.USER_ID_ATTACHMENT);
        if (hasText(incomingUserId)) {
            MDC.put(TraceMdcKeys.USER_ID, incomingUserId);
        } else {
            MDC.remove(TraceMdcKeys.USER_ID);
        }

        try {
            return invoker.invoke(invocation);
        } finally {
            restoreMdc(TraceMdcKeys.TRACE_ID, previousTraceId);
            restoreMdc(TraceMdcKeys.USER_ID, previousUserId);
        }
    }

    private void restoreMdc(String key, String value) {
        if (hasText(value)) {
            MDC.put(key, value);
        } else {
            MDC.remove(key);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
