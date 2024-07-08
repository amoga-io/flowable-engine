package org.flowable.temporal.workflows;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.cmmn.api.runtime.CaseInstance;

import java.util.Map;
public class CaseActivityImpl implements CaseActivities{

    protected CmmnRuntimeService runtimeService;
    protected CmmnTaskService taskService;
    public CaseActivityImpl(CmmnRuntimeService runtimeService, CmmnTaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

    @Override
    public String createCase(Map<String,Object> payload) {
//        System.out.println(Thread.currentThread().getName()+"=====new Thread");
        Object caseDefinitionKey = payload.get("caseDefinitionKey");
        Object username = payload.get("username");
        Map<String, Object> startVariables = (Map<String, Object>) payload.get("variables");
        CaseInstance createdCase = runtimeService.createCase(username.toString(), startVariables, caseDefinitionKey.toString(), "from temporal");
        return createdCase.getId();
    }

    @Override
    public String updateCase(Map<String, Object> payload) {
        String instanceId = (String)payload.get("workflow_instance_id");
        if(instanceId != null) {
            Map<String, Object> variablesToSet = (Map<String, Object>) payload.get("variables");
            runtimeService.setVariables(instanceId, variablesToSet);
        } else {
            return "workflow_instance_id required";
        }
        return "Case with id "+instanceId+" Updated!!!";
    }

    @Override
    public String updateAndCompleteTask(Map<String, Object> payload) {
        String workflowInstanceID = (String)payload.get("workflow_instance_id");
        String instanceId = (String)payload.get("workflow_id");
        if(workflowInstanceID != null) {
            Map<String, Object> variablesToSet = (Map<String, Object>) payload.get("variables");
            runtimeService.setVariables(workflowInstanceID, variablesToSet);
        } else {
            return "workflow_instance_id required";
        }
        if(instanceId != null) {
            taskService.complete(instanceId);
        } else {
            return "workflow_id required";
        }
        return "Task with id: "+instanceId+" completed.";
    }

    @Override
    public String deleteCase(String caseInstanceId) {
        if(caseInstanceId != null) {
            runtimeService.deleteCaseInstance(caseInstanceId);
        } else {
            return "workflow_instance_id required";
        }
        return "Case with id "+caseInstanceId+" deleted.";
    }
}
