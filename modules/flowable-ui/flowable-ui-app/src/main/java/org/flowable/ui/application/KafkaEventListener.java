package org.flowable.ui.application;

import com.nimbusds.jose.shaded.json.JSONObject;
import org.flowable.task.service.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;

import static org.flowable.ui.application.FlowableUiAppEventRegistryCondition.environmentMap;

public class KafkaEventListener implements TaskListener{


    public void notify(DelegateTask delegateTask) {
        String parentId = (delegateTask.getProcessInstanceId() != null) ? delegateTask.getProcessInstanceId() : ((TaskEntityImpl) delegateTask).getScopeId();
        String dueDate = (delegateTask.getDueDate() != null) ? delegateTask.getDueDate().toString() : null;
        for(String keyName : delegateTask.getVariables().keySet()){
            if(keyName.equals("parent_case_id")){
                parentId = delegateTask.getVariable(keyName).toString();
            }
        }
        try {
            String event = "{" +
                    "\"id\":\"" + delegateTask.getId() + "\"," +
                    "\"priority\":\"" + delegateTask.getPriority() + "\"," +
                    "\"task_type\":\"" + delegateTask.getTaskDefinitionKey() + "\"," +
                    "\"tenantId\":\"" + delegateTask.getTenantId() + "\"," +
                    "\"dueDate\":" + dueDate + "," +
                    "\"name\":\"" + delegateTask.getName() + "\"," +
                    "\"parent_id\":\"" + parentId + "\"," +
                    "\"variables\":" + new JSONObject(delegateTask.getVariables()).toJSONString() + "," +
                    "\"assignee\":\"" + delegateTask.getAssignee() + "\"," +
                    "\"event\":\"" + delegateTask.getEventName() + "\"" +
                    "}";

                KafkaService.kafkaTemplate().send(environmentMap.get("kafkaTopic"),parentId, event);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
