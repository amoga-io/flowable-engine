package org.flowable.ui.application;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import static org.flowable.ui.application.FlowableUiAppEventRegistryCondition.environmentMap;


public class TriggerTemporalFlow implements TaskListener{

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            String temporalServerAddress = environmentMap.get("temporalServerAddress");
            Map<String, Object> variables = delegateTask.getVariables();
            String temporal_flow_id = (String) variables.get("temporal_flow_id");
            String amogaEnv = (String) variables.get("amoga_env");
            if(amogaEnv == null) {
                return;
            }
            Random number = new Random();
            int random_int = number.nextInt(1000);
            String workflowId = "flowable_"+delegateTask.getEventName()+"_task_event_"+random_int+temporal_flow_id;

            TemporalClientSingleton clientSingleton = TemporalClientSingleton.getInstance();
            clientSingleton.getWorkflowClient(temporalServerAddress, amogaEnv);



            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
            df.setTimeZone(tz);
            String parentId = (delegateTask.getProcessInstanceId() != null) ? delegateTask.getProcessInstanceId() : ((TaskEntityImpl) delegateTask).getScopeId();
            String dueDate = (delegateTask.getDueDate() != null) ? df.format(delegateTask.getDueDate()) : null;
            String planItemId = "";
            String amo_state = (delegateTask.getEventName() == "create") ? "active" : "completed";


            String variable = new JSONObject(variables).toJSONString();

            try {
                planItemId = ((TaskEntityImpl) delegateTask).getSubScopeId();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            for (String keyName : delegateTask.getVariables().keySet()) {
                if (keyName.equals("parent_case_id")) {
                    parentId = delegateTask.getVariable(keyName).toString();
                }
            }
            Map<String, Object> data = new HashMap<>();
            data.put("id", delegateTask.getId());
            data.put("planitem_id", planItemId);
            data.put("priority", delegateTask.getPriority());
            data.put("task_type", delegateTask.getTaskDefinitionKey().replace("_", "").trim());
            data.put("tenantId", delegateTask.getTenantId());
            data.put("dueDate", dueDate);
            data.put("name", delegateTask.getName());
            data.put("amo_state", amo_state);
            data.put("parent_id", parentId);
            data.put("variables", variable);
            data.put("assignee", delegateTask.getAssignee());
            data.put("event", delegateTask.getEventName());

            clientSingleton.startWorkflowAsync(amogaEnv, workflowId, data);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
