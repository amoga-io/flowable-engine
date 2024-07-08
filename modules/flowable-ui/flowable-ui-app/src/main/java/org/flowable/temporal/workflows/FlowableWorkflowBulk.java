package org.flowable.temporal.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;
import java.util.Map;

@WorkflowInterface
public interface FlowableWorkflowBulk {
    @WorkflowMethod
    Map<String,Integer> handleFlowBulkData(Map<String, Object> payload);
}
