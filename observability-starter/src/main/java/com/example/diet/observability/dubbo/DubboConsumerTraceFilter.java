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
 * Consumer-side trace propagation filter.
 */
@Activate(group = CommonConstants.CONSUMER, order = -12000)
public class DubboConsumerTraceFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String traceId = MDC.get(TraceMdcKeys.TRACE_ID);
        boolean generatedByCurrentThread = false;
        if (!hasText(traceId)) {
            traceId = TraceIdGenerator.generate();
            MDC.put(TraceMdcKeys.TRACE_ID, traceId);
            generatedByCurrentThread = true;
        }

        RpcContext.getServiceContext().setAttachment(TraceMdcKeys.TRACE_ID_ATTACHMENT, traceId);
        String userId = MDC.get(TraceMdcKeys.USER_ID);
        if (hasText(userId)) {
            RpcContext.getServiceContext().setAttachment(TraceMdcKeys.USER_ID_ATTACHMENT, userId);
        }

        try {
            return invoker.invoke(invocation);
        } finally {
            if (generatedByCurrentThread) {
                MDC.remove(TraceMdcKeys.TRACE_ID);
            }
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
