package org.flowable.ui.application.lifecycle.listener;

import com.nimbusds.jose.shaded.json.JSONObject;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.flowable.cmmn.api.delegate.DelegatePlanItemInstance;
import org.flowable.cmmn.api.listener.PlanItemInstanceLifecycleListener;
import org.flowable.temporal.workflows.HandleFlowableData;
import org.flowable.ui.application.KafkaService;
import org.flowable.ui.application.TemporalClientSingleton;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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

            Map<String, Object> variables = planItemInstance.getVariables();
            String temporal_flow_id = (String) variables.get("temporal_flow_id");
            String amogaEnv = (String) variables.get("amoga_env");
            if(amogaEnv == null) {
                return;
            }

            Random number = new Random();
            int random_int = number.nextInt(1000);
            int random_int2 = number.nextInt(1000);
            String workflowId = "flowable_create_case_event_"+random_int+"_"+random_int2+"_"+ temporal_flow_id;
            TemporalClientSingleton clientSingleton = TemporalClientSingleton.getInstance();

            clientSingleton.getWorkflowClient(temporalServerAddress, amogaEnv);

            Map<String, Object> data = new HashMap<>();
            data.put("id", planItemInstance.getCaseInstanceId());
            data.put("task_type", planItemInstance.getName());
            data.put("tenantId", planItemInstance.getTenantId());
            data.put("variables", new JSONObject(planItemInstance.getVariables()).toJSONString());
            data.put("event", "create");
            try {
                clientSingleton.startWorkflowAsync(amogaEnv, workflowId, data);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
