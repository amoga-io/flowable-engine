package org.flowable.temporal.workflows;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.Map;

public class CreateCaseWorkflowImpl implements  CreateCaseWorkflow {

    ActivityOptions options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(500))
            .setRetryOptions(null)
            .build();

    private final CreateCaseActivities activities = Workflow.newActivityStub(CreateCaseActivities.class, options);
    @Override
    public String createCase(Map<String,String> payload) {
        return activities.createCaseActivity(payload);
    }
}
