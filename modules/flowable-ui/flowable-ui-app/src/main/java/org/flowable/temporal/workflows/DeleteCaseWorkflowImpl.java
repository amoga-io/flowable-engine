package org.flowable.temporal.workflows;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.Map;

public class DeleteCaseWorkflowImpl implements DeleteCaseWorkflow{
    ActivityOptions options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(500))
            .setRetryOptions(null)
            .build();

    private final CaseActivities activities = Workflow.newActivityStub(CaseActivities.class, options);
    @Override
    public String deleteCase(Map<String, String> payload) {
        String caseInstanceId = payload.get("workflow_instance_id");
        return activities.deleteCase(caseInstanceId);
    }
}
