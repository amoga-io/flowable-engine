package org.flowable.ui.application.lifecycle.listener;

import com.nimbusds.jose.shaded.json.JSONObject;
import org.flowable.cmmn.api.delegate.DelegatePlanItemInstance;
import org.flowable.cmmn.api.listener.PlanItemInstanceLifecycleListener;
import org.flowable.ui.application.KafkaService;

import static org.flowable.ui.application.FlowableUiAppEventRegistryCondition.environmentMap;


public class AmogaLifecycleListener implements PlanItemInstanceLifecycleListener {

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
            String event = "{" +
                    "\"id\":\"" + planItemInstance.getCaseInstanceId() + "\"," +
                    "\"task_type\":\"" + planItemInstance.getName() + "\"," +
                    "\"tenantId\":\"" + planItemInstance.getTenantId() + "\"," +
                    "\"variables\":" + new JSONObject(planItemInstance.getVariables()).toJSONString() + "," +
                    "\"event\":\" create \"" +
                    "}";

            KafkaService.kafkaTemplate().send(environmentMap.get("kafkaTopic"), planItemInstance.getCaseInstanceId(),event);
        }
    }
}
