package org.flowable.ui.application;

import com.nimbusds.jose.shaded.json.JSONObject;
import org.flowable.task.service.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

import static org.flowable.ui.application.FlowableUiAppEventRegistryCondition.environmentMap;

public class KafkaEventListener implements TaskListener{


    public void notify(DelegateTask delegateTask) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String parentId = (delegateTask.getProcessInstanceId() != null) ? delegateTask.getProcessInstanceId() : ((TaskEntityImpl) delegateTask).getScopeId();
        String dueDate = (delegateTask.getDueDate() != null) ? df.format(delegateTask.getDueDate()) : null;
        String planItemId = "";
        String amo_state= (delegateTask.getEventName() == "create")?"active":"completed";
        //String _outcomes="";
        Map<String,Object> variables = delegateTask.getVariables();
        if( "create".equals(delegateTask.getEventName())) {
            variables.put("_outcome", "toDo");
        }

        String variable = new JSONObject(variables).toJSONString();

        try {
            planItemId = ((TaskEntityImpl) delegateTask).getSubScopeId();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        for(String keyName : delegateTask.getVariables().keySet()){
            if(keyName.equals("parent_case_id")){
                parentId = delegateTask.getVariable(keyName).toString();
            }
        }

        try {
            String event = "{" +
                    "\"id\":\"" + delegateTask.getId() + "\"," +
                    "\"planitem_id\":\""+planItemId+"\","+
                    "\"priority\":\"" + delegateTask.getPriority() + "\"," +
                    "\"task_type\":\"" + delegateTask.getTaskDefinitionKey().replace("_","").trim()+ "\"," +
                    "\"tenantId\":\"" + delegateTask.getTenantId() + "\"," +
                    "\"dueDate\":\"" + dueDate + "\"," +
                    "\"name\":\"" + delegateTask.getName() + "\"," +
                    "\"amo_state\":\""+amo_state+"\","+
                    "\"parent_id\":\"" + parentId + "\"," +
                    "\"variables\":" + variable + "," +
                    "\"assignee\":\"" + delegateTask.getAssignee() + "\"," +
                    "\"event\":\"" + delegateTask.getEventName() + "\"" +
                    "}";

                KafkaService.kafkaTemplate().send(environmentMap.get("kafkaTopic"),parentId, event);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
