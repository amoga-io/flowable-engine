package org.flowable.temporal.workflows;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.Map;

public class UpdateCaseWorkflowImpl implements UpdateCaseWorkflow{
    ActivityOptions options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(500))
            .setRetryOptions(null)
            .build();

    private final CaseActivities activities = Workflow.newActivityStub(CaseActivities.class, options);
    @Override
    public String updateCase(Map<String,Object> payload) {
        return activities.updateCase(payload);
    }
}
