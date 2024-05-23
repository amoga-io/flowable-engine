package org.flowable.ui.application;
import com.nimbusds.jose.shaded.json.JSONObject;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.flowable.temporal.workflows.HandleFlowableData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static org.flowable.ui.application.FlowableUiAppEventRegistryCondition.environmentMap;


public class TriggerTemporalFlow implements TaskListener{

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
//            String temporalServerAddress = "57.128.165.20:7233";
            String temporalServerAddress = environmentMap.get("temporalServerAddress");
            WorkflowServiceStubsOptions options2 = WorkflowServiceStubsOptions.newBuilder()
                    .setTarget(temporalServerAddress)
                    .build();
            Map<String, Object> variables = delegateTask.getVariables();
            String temporal_flow_id = (String) variables.get("temporal_flow_id");
            String amogaEnv = (String) variables.get("amoga_env");
            WorkflowServiceStubs service = WorkflowServiceStubs.newInstance(options2);

            WorkflowClientOptions clientOptions = WorkflowClientOptions.newBuilder()
                    .setNamespace(amogaEnv)
                    .build();
            WorkflowClient workflowClient = WorkflowClient.newInstance(service,clientOptions);


            String queueName = "handle_flowable_queue_"+amogaEnv;
            WorkflowOptions options = WorkflowOptions.newBuilder()
                    .setTaskQueue(queueName) // Specify the task queue name where your worker is listening
                    .setWorkflowId("flowable_"+delegateTask.getEventName()+"_task_event_"+temporal_flow_id)
                    .build();

            HandleFlowableData workflow = workflowClient.newWorkflowStub(HandleFlowableData.class, options);

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

            workflow.handle_flowable_data(data);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
