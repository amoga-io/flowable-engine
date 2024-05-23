package org.flowable.ui.application.lifecycle.listener;

import com.nimbusds.jose.shaded.json.JSONObject;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.flowable.cmmn.api.delegate.DelegatePlanItemInstance;
import org.flowable.cmmn.api.listener.PlanItemInstanceLifecycleListener;
import org.flowable.temporal.workflows.HandleFlowableData;
import org.flowable.ui.application.KafkaService;

import java.util.HashMap;
import java.util.Map;

import static org.flowable.ui.application.FlowableUiAppEventRegistryCondition.environmentMap;

public class TemporalListener implements PlanItemInstanceLifecycleListener {
    @Override
    public String getSourceState() {
        return null;
    }

    @Override
    public String getTargetState() {
        return null;
    }

    @Override
    public void stateChanged(DelegatePlanItemInstance planItemInstance, String oldState, String newState) {
        System.out.println(planItemInstance);
        if ("active".equals(newState) && "available".equals(oldState)) {
//            String temporalServerAddress = "57.128.165.20:7233";
            String temporalServerAddress = environmentMap.get("temporalServerAddress");


            WorkflowServiceStubsOptions options2 = WorkflowServiceStubsOptions.newBuilder()
                    .setTarget(temporalServerAddress)
                    .build();
            Map<String, Object> variables = planItemInstance.getVariables();
            String temporal_flow_id = (String) variables.get("temporal_flow_id");
            String amogaEnv = (String) variables.get("amoga_env");
            String queueName = "handle_flowable_queue_"+amogaEnv;

            WorkflowServiceStubs service = WorkflowServiceStubs.newInstance(options2);
            WorkflowClient workflowClient = WorkflowClient.newInstance(service);

            WorkflowOptions options = WorkflowOptions.newBuilder()
                    .setTaskQueue(queueName) // Specify the task queue name where your worker is listening
                    .setWorkflowId("flowable_create_event_"+temporal_flow_id)
                    .build();

            HandleFlowableData workflow = workflowClient.newWorkflowStub(HandleFlowableData.class, options);

            Map<String, Object> data = new HashMap<>();
            data.put("id", planItemInstance.getCaseInstanceId());
            data.put("task_type", planItemInstance.getName());
            data.put("tenantId", planItemInstance.getTenantId());
            data.put("variables", new JSONObject(planItemInstance.getVariables()).toJSONString());
            data.put("event", "create");
            try {
                workflow.handle_flowable_data(data);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
