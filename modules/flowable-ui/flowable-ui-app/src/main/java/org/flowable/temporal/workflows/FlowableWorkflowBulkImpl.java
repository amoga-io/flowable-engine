package org.flowable.temporal.workflows;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.Map;

public class FlowableWorkflowBulkImpl implements FlowableWorkflowBulk{
    RetryOptions retryOptions = RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(3))
            .setMaximumAttempts(3)
            .setMaximumInterval(Duration.ofMinutes(1))
            .setBackoffCoefficient(2.0)
            .build();
    ActivityOptions options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(300))
            .setRetryOptions(retryOptions)
            .build();

    private final BulkActivities activities = Workflow.newActivityStub(BulkActivities.class, options);
    @Override
    public Map<String,Object> handleFlowBulkData(Map<String, Object> payload) {
        String action = (String)payload.get("action");
        switch (action) {
            case "create_bulk": {
                return activities.createCaseBulk(payload);
            }
            case "update_bulk" : {
                return activities.updateCaseBulk(payload);
            }
            case "complete_bulk": {
                return activities.updateAndCompleteTaskBulk(payload);
            }
        }
        return null;
    }
}
