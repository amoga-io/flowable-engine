package org.flowable.temporal.workflows;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.Map;

@WorkflowInterface
public interface DeleteCaseWorkflow {
    @WorkflowMethod
    String deleteCase(Map<String, String> payload);
}
