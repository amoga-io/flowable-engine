package org.flowable.temporal.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.Map;

@WorkflowInterface
public interface UpdateCaseWorkflow {
    @WorkflowMethod
    String updateCase(Map<String, Object> payload);
}
