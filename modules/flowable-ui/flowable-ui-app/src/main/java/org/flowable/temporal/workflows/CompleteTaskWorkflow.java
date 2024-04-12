package org.flowable.temporal.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.Map;

@WorkflowInterface
public interface CompleteTaskWorkflow {
    @WorkflowMethod
    String completeTask(Map<String, String> payload);
}
