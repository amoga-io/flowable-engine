package org.flowable.temporal.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

@WorkflowInterface
public interface HandleFlowableData {
    @WorkflowMethod
    @Async
    String handle_flowable_data(Map<String, Object> payload);
}
