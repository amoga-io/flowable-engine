package org.flowable.ui.application;

import com.nimbusds.jose.shaded.json.JSONObject;
import org.flowable.task.service.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;

public class KafkaEventListener implements TaskListener{

    KafkaService kafkaService = new KafkaService();

    public void notify(DelegateTask delegateTask) {
        String parentId = (delegateTask.getProcessInstanceId() != null) ? delegateTask.getProcessInstanceId() : ((TaskEntityImpl) delegateTask).getScopeId();
        try {
            String event = "{" +
                    "\"id\":\"" + delegateTask.getId() + "\"," +
                    "\"priority\":\"" + delegateTask.getPriority() + "\"," +
                    "\"task_type\":\"" + delegateTask.getTaskDefinitionKey() + "\"," +
                    "\"tenantId\":\"" + delegateTask.getTenantId() + "\"," +
                    "\"dueDate\":\"" + delegateTask.getDueDate() + "\"," +
                    "\"name\":\"" + delegateTask.getName() + "\"," +
                    "\"parent_id\":\"" + parentId + "\"," +
                    "\"variables\":" + new JSONObject(delegateTask.getVariables()).toJSONString() + "," +
                    "\"assignee\":\"" + delegateTask.getAssignee() + "\"," +
                    "\"event\":\"" + delegateTask.getEventName() + "\"" +
                    "}";

                kafkaService.kafkaTemplate().send("amoga-task-event-topic",parentId, event);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
