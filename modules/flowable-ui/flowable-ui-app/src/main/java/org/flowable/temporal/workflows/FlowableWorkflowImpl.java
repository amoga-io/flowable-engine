package org.flowable.temporal.workflows;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.Map;

public class FlowableWorkflowImpl implements FlowableWorkflow{

    RetryOptions retryOptions = RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(3))
            .setMaximumAttempts(3)
            .setMaximumInterval(Duration.ofSeconds(3))
            .setBackoffCoefficient(1.0)
            .build();
    ActivityOptions options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .setRetryOptions(retryOptions)
            .build();

    private final CaseActivities activities = Workflow.newActivityStub(CaseActivities.class, options);
    @Override
    public Object handleFlowData(Map<String,Object> payload) {
        String action = (String)payload.get("action");
        switch (action) {
            case "create": {
                return activities.createCase(payload);
            }
            case  "update": {
                return  activities.updateCase(payload);
            }
            case "delete" : {
                return  activities.deleteCase(payload.get("workflow_instance_id").toString());
            }
            case "update_complete": {
                return activities.updateAndCompleteTask(payload);
            }
            default: {
                return action + " Not Supported. supported actions are: create, update, delete, complete";
            }

        }
    }
}
