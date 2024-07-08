package org.flowable.temporal.workflows;

import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.cmmn.api.runtime.CaseInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BulkActivitiesImpl implements BulkActivities {
    protected CmmnRuntimeService runtimeService;
    protected CmmnTaskService taskService;
    public BulkActivitiesImpl(CmmnRuntimeService runtimeService, CmmnTaskService taskService) {
        this.runtimeService = runtimeService;
        this.taskService = taskService;
    }
    @Override
    public Map<String,Integer> createCaseBulk(Map<String, Object> payloadObjectList) {
        int failedCount = 0;
        int successCount = 0;
        Map<String,Integer> finalReport = new HashMap<>();
        List<String> createdCases = new ArrayList<String>();
        List<Map<String,Object>> payloadList = (List<Map<String,Object>>)payloadObjectList.get("payloadList");
        for (Map<String,Object> payload: payloadList) {
            try {
                Object caseDefinitionKey = payload.get("caseDefinitionKey");
                Object username = payload.get("username");
                Map<String, Object> startVariables = (Map<String, Object>) payload.get("variables");
                CaseInstance createdCase = runtimeService.createCase(username.toString(), startVariables, caseDefinitionKey.toString(), "from temporal");
                createdCases.add(createdCase.getId());
            } catch (Exception exp) {
                failedCount = failedCount+1;
            }
            successCount = successCount +1;
        }
        finalReport.put("successCount",successCount);
        finalReport.put("failedCount",failedCount);
        return finalReport;
    }

    @Override
    public Map<String,Integer> updateCaseBulk(Map<String, Object> payloadObjectList) {
        int failedCount = 0;
        int successCount = 0;
        Map<String,Integer> finalReport = new HashMap<>();
        List<Map<String,Object>> payloadList = (List<Map<String,Object>>)payloadObjectList.get("payloadList");
        for (Map<String,Object> payload: payloadList) {
            String instanceId = (String) payload.get("workflow_instance_id");
            if (instanceId != null) {
                try {
                    Map<String, Object> variablesToSet = (Map<String, Object>) payload.get("variables");
                    runtimeService.setVariables(instanceId, variablesToSet);
                } catch (Exception exp) {
                    failedCount = failedCount+1;
                }
            } else {
                failedCount = failedCount+1;
            }
            successCount = successCount+1;
        }
        finalReport.put("successCount",successCount);
        finalReport.put("failedCount",failedCount);
        return finalReport;
    }

    @Override
    public Map<String,Integer> updateAndCompleteTaskBulk(Map<String, Object> payloadObjectList) {
        int failedCount = 0;
        int successCount = 0;
        Map<String,Integer> finalReport = new HashMap<>();
        List<Map<String,Object>> payloadList = (List<Map<String,Object>>)payloadObjectList.get("payloadList");
        for (Map<String,Object> payload: payloadList) {
            try {
                String workflowInstanceID = (String) payload.get("workflow_instance_id");
                String instanceId = (String) payload.get("workflow_id");
                if (workflowInstanceID != null) {
                    Map<String, Object> variablesToSet = (Map<String, Object>) payload.get("variables");
                    runtimeService.setVariables(workflowInstanceID, variablesToSet);
                } else {
                    failedCount = failedCount + 1;
                }
                if (instanceId != null) {
                    taskService.complete(instanceId);
                } else {
                    failedCount = failedCount + 1;
                }
            } catch (Exception exp){
                failedCount = failedCount+1;
            }
            successCount = successCount+1;
        }
        finalReport.put("successCount",successCount);
        finalReport.put("failedCount",failedCount);
        return finalReport;
    }
}
