package org.flowable.temporal.workflows;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
public class CaseActivityImpl implements CaseActivities{

    protected CmmnRuntimeService runtimeService;
    protected CmmnTaskService taskService;
    public CaseActivityImpl(CmmnRuntimeService runtimeService, CmmnTaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }

//    @Autowired
//    protected CmmnRuntimeService runtimeService;
    @Override
    public String createCase(Map<String,Object> payload) {
        System.out.println(Thread.currentThread().getName()+"=====new Thread");
        Object caseDefinationKey = payload.get("caseDefinitionKey");
//        String name = payload.get("name");
        Object username = payload.get("username");
        Map<String, Object> startVariables = (Map<String, Object>) payload.get("variables");
        CaseInstance createdCase = runtimeService.createCase(username.toString(), startVariables, caseDefinationKey.toString(), "from temporal");
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
    public String completeTask(String taskId) {
        if(taskId != null) {
            taskService.complete(taskId);
        } else {
            return "workflow_id required";
        }
        return "Task with id "+taskId+" completed.";
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
