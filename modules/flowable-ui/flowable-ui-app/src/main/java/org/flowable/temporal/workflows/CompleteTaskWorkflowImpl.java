package org.flowable.temporal.workflows;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.Map;

public class CompleteTaskWorkflowImpl implements CompleteTaskWorkflow{
    ActivityOptions options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(500))
            .setRetryOptions(null)
            .build();

    private final CaseActivities activities = Workflow.newActivityStub(CaseActivities.class, options);
    @Override
    public String completeTask(Map<String, String> payload) {
        String taskId = payload.get("workflow_id");
        return activities.completeTask(taskId);
    }
}
