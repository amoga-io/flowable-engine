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
    public Map<String,Object> createCaseBulk(Map<String, Object> payloadObjectList) {
        int failedCount = 0;
        int successCount = 0;
        Map<String,Object> finalReport = new HashMap<>();
        List<String> createdCases = new ArrayList<String>();
        List<Map<String,Object>> failedCases = new ArrayList<>();
        List<Map<String,Object>> payloadList = (List<Map<String,Object>>)payloadObjectList.get("payloadList");
        for (Map<String,Object> payload: payloadList) {
            try {
                Object caseDefinitionKey = payload.get("caseDefinitionKey");
                Object username = payload.get("username");
                Map<String, Object> startVariables = (Map<String, Object>) payload.get("variables");
                CaseInstance createdCase = runtimeService.createCase(username.toString(), startVariables, caseDefinitionKey.toString(), "from temporal");
                createdCases.add(createdCase.getId());
                Thread.sleep(100);
            } catch (Exception exp) {
                Map<String,Object> failed_data = new HashMap<>();
                failed_data.put("data",payload);
                failed_data.put("error",exp.getMessage());
                failedCases.add(failed_data);
                failedCount = failedCount+1;
                continue;
            }
            successCount = successCount +1;
        }
        finalReport.put("successCount",successCount);
        finalReport.put("successData",createdCases);
        finalReport.put("failedCount",failedCount);
        finalReport.put("failedData",failedCases);
        return finalReport;
    }

    @Override
    public Map<String,Object> updateCaseBulk(Map<String, Object> payloadObjectList) {
        int failedCount = 0;
        int successCount = 0;
        Map<String,Object> finalReport = new HashMap<>();
        List<String> successUpdates = new ArrayList<String>();
        List<Map<String,Object>> failedUpdates = new ArrayList<>();
        List<Map<String,Object>> payloadList = (List<Map<String,Object>>)payloadObjectList.get("payloadList");
        for (Map<String,Object> payload: payloadList) {
            String instanceId = (String) payload.get("workflow_instance_id");
            if (instanceId != null) {
                try {
                    Map<String, Object> variablesToSet = (Map<String, Object>) payload.get("variables");
                    runtimeService.setVariables(instanceId, variablesToSet);
                    successUpdates.add(instanceId);
                    Thread.sleep(100);
                } catch (Exception exp) {
                    Map<String,Object> failed_data = new HashMap<>();
                    failed_data.put("data",payload);
                    failed_data.put("error",exp.getMessage());
                    failedUpdates.add(failed_data);
                    failedCount = failedCount+1;
                    continue;
                }
            } else {
                Map<String,Object> failed_data = new HashMap<>();
                failed_data.put("data",payload);
                failed_data.put("error","workflow_instance_id missing in payload");
                failedUpdates.add(failed_data);
                failedCount = failedCount+1;
                continue;
            }
            successCount = successCount+1;
        }
        finalReport.put("successCount",successCount);
        finalReport.put("successData",successUpdates);
        finalReport.put("failedCount",failedCount);
        finalReport.put("failedData",failedUpdates);
        return finalReport;
    }

    @Override
    public Map<String,Object> updateAndCompleteTaskBulk(Map<String, Object> payloadObjectList) {
        int failedCount = 0;
        int successCount = 0;
        Map<String,Object> finalReport = new HashMap<>();
        List<String> successUpdates = new ArrayList<String>();
        List<Map<String,Object>> failedUpdates = new ArrayList<>();
        List<Map<String,Object>> payloadList = (List<Map<String,Object>>)payloadObjectList.get("payloadList");
        for (Map<String,Object> payload: payloadList) {
            try {
                String workflowInstanceID = (String) payload.get("workflow_instance_id");
                String instanceId = (String) payload.get("workflow_id");
                if (workflowInstanceID != null) {
                    Map<String, Object> variablesToSet = (Map<String, Object>) payload.get("variables");
                    runtimeService.setVariables(workflowInstanceID, variablesToSet);
                } else {
                    Map<String,Object> failed_data = new HashMap<>();
                    failed_data.put("data",payload);
                    failed_data.put("error","workflow_instance_id missing in payload");
                    failedUpdates.add(failed_data);
                    failedCount = failedCount + 1;
                    continue;
                }
                if (instanceId != null) {
                    taskService.complete(instanceId);
                    Thread.sleep(100);
                } else {
                    Map<String,Object> failed_data = new HashMap<>();
                    failed_data.put("data",payload);
                    failed_data.put("error","instance_id missing in payload");
                    failedUpdates.add(failed_data);
                    failedCount = failedCount + 1;
                    continue;
                }
                successUpdates.add(instanceId);
                successCount = successCount+1;
            } catch (Exception exp){
                Map<String,Object> failed_data = new HashMap<>();
                failed_data.put("data",payload);
                failed_data.put("error",exp.getMessage());
                failedUpdates.add(failed_data);
                failedCount = failedCount+1;
            }
        }
        finalReport.put("successCount",successCount);
        finalReport.put("successData",successUpdates);
        finalReport.put("failedCount",failedCount);
        finalReport.put("failedData",failedUpdates);
        return finalReport;
    }
}
