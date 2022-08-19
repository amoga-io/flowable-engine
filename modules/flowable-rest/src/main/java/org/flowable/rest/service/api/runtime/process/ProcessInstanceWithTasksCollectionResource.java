package org.flowable.rest.service.api.runtime.process;

import io.swagger.annotations.*;
import org.flowable.common.rest.api.DataResponse;
import org.flowable.common.rest.api.RequestUtil;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.rest.service.api.runtime.task.TaskResponse;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = { "Process Instances With Tasks" }, description = "List Process Instances With Task", authorizations = { @Authorization(value = "basicAuth") })
public class ProcessInstanceWithTasksCollectionResource extends BaseProcessInstanceResource {
    @Autowired
    protected HistoryService historyService;

    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected TaskService taskService;

    @ApiOperation(value = "List process instances with Tasks", nickname ="listProcessInstancesWithTasks", tags = { "Process Instances With Tasks" })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", dataType = "string", value = "Only return models with the given version.", paramType = "query"),
            @ApiImplicitParam(name = "name", dataType = "string", value = "Only return models with the given name.", paramType = "query"),
            @ApiImplicitParam(name = "nameLike", dataType = "string", value = "Only return models like the given name.", paramType = "query"),
            @ApiImplicitParam(name = "nameLikeIgnoreCase", dataType = "string", value = "Only return models like the given name ignoring case.", paramType = "query"),
            @ApiImplicitParam(name = "processDefinitionKey", dataType = "string", value = "Only return process instances with the given process definition key.", paramType = "query"),
            @ApiImplicitParam(name = "processDefinitionId", dataType = "string", value = "Only return process instances with the given process definition id.", paramType = "query"),
            @ApiImplicitParam(name = "processDefinitionCategory", dataType = "string", value = "Only return process instances with the given process definition category.", paramType = "query"),
            @ApiImplicitParam(name = "processDefinitionVersion", dataType = "integer", value = "Only return process instances with the given process definition version.", paramType = "query"),
            @ApiImplicitParam(name = "processDefinitionEngineVersion", dataType = "string", value = "Only return process instances with the given process definition engine version.", paramType = "query"),
            @ApiImplicitParam(name = "businessKey", dataType = "string", value = "Only return process instances with the given businessKey.", paramType = "query"),
            @ApiImplicitParam(name = "businessKeyLike", dataType = "string", value = "Only return process instances with the businessKey like the given key.", paramType = "query"),
            @ApiImplicitParam(name = "startedBy", dataType = "string", value = "Only return process instances started by the given user.", paramType = "query"),
            @ApiImplicitParam(name = "startedBefore", dataType = "string", format = "date-time", value = "Only return process instances started before the given date.", paramType = "query"),
            @ApiImplicitParam(name = "startedAfter", dataType = "string", format = "date-time", value = "Only return process instances started after the given date.", paramType = "query"),
            @ApiImplicitParam(name = "activeActivityId", dataType = "string", value = "Only return process instances which have an active activity instance with the provided activity id.", paramType = "query"),
            @ApiImplicitParam(name = "involvedUser", dataType = "string", value = "Only return process instances in which the given user is involved.", paramType = "query"),
            @ApiImplicitParam(name = "suspended", dataType = "boolean", value = "If true, only return process instance which are suspended. If false, only return process instances which are not suspended (active).", paramType = "query"),
            @ApiImplicitParam(name = "superProcessInstanceId", dataType = "string", value = "Only return process instances which have the given super process-instance id (for processes that have a call-activities).", paramType = "query"),
            @ApiImplicitParam(name = "subProcessInstanceId", dataType = "string", value = "Only return process instances which have the given sub process-instance id (for processes started as a call-activity).", paramType = "query"),
            @ApiImplicitParam(name = "excludeSubprocesses", dataType = "boolean", value = "Return only process instances which are not sub processes.", paramType = "query"),
            @ApiImplicitParam(name = "includeProcessVariables", dataType = "boolean", value = "Indication to include process variables in the result.", paramType = "query"),
            @ApiImplicitParam(name = "callbackId", dataType = "string", value = "Only return process instances with the given callbackId.", paramType = "query"),
            @ApiImplicitParam(name = "callbackType", dataType = "string", value = "Only return process instances with the given callbackType.", paramType = "query"),
            @ApiImplicitParam(name = "tenantId", dataType = "string", value = "Only return process instances with the given tenantId.", paramType = "query"),
            @ApiImplicitParam(name = "tenantIdLike", dataType = "string", value = "Only return process instances with a tenantId like the given value.", paramType = "query"),
            @ApiImplicitParam(name = "withoutTenantId", dataType = "boolean", value = "If true, only returns process instances without a tenantId set. If false, the withoutTenantId parameter is ignored.", paramType = "query"),
            @ApiImplicitParam(name = "sort", dataType = "string", value = "Property to sort on, to be used together with the order.", allowableValues = "id,processDefinitionId,tenantId,processDefinitionKey", paramType = "query"),
    })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Indicates request was successful and the process-instances are returned"),
            @ApiResponse(code = 400, message = "Indicates a parameter was passed in the wrong format . The status-message contains additional information.")
    })
    @GetMapping(value = "/runtime/process-instances-with-tasks", produces = "application/json")
    public DataResponse<ProcessInstanceWithTasksResponse> getProcessInstances(@ApiParam(hidden = true) @RequestParam Map<String, String> allRequestParams, HttpServletRequest request) {
        // Populate query based on request
        ProcessInstanceQueryRequest queryRequest = new ProcessInstanceQueryRequest();

        if (allRequestParams.containsKey("id")) {
            queryRequest.setProcessInstanceId(allRequestParams.get("id"));
        }

        if (allRequestParams.containsKey("name")) {
            queryRequest.setProcessInstanceName(allRequestParams.get("name"));
        }

        if (allRequestParams.containsKey("nameLike")) {
            queryRequest.setProcessInstanceNameLike(allRequestParams.get("nameLike"));
        }

        if (allRequestParams.containsKey("nameLikeIgnoreCase")) {
            queryRequest.setProcessInstanceNameLikeIgnoreCase(allRequestParams.get("nameLikeIgnoreCase"));
        }

        if (allRequestParams.containsKey("processDefinitionKey")) {
            queryRequest.setProcessDefinitionKey(allRequestParams.get("processDefinitionKey"));
        }

        if (allRequestParams.containsKey("processDefinitionId")) {
            queryRequest.setProcessDefinitionId(allRequestParams.get("processDefinitionId"));
        }

        if (allRequestParams.containsKey("processDefinitionCategory")) {
            queryRequest.setProcessDefinitionCategory(allRequestParams.get("processDefinitionCategory"));
        }

        if (allRequestParams.containsKey("processDefinitionVersion")) {
            queryRequest.setProcessDefinitionVersion(Integer.valueOf(allRequestParams.get("processDefinitionVersion")));
        }

        if (allRequestParams.containsKey("processDefinitionEngineVersion")) {
            queryRequest.setProcessDefinitionEngineVersion(allRequestParams.get("processDefinitionEngineVersion"));
        }

        if (allRequestParams.containsKey("businessKey")) {
            queryRequest.setProcessBusinessKey(allRequestParams.get("businessKey"));
        }

        if (allRequestParams.containsKey("businessKeyLike")) {
            queryRequest.setProcessBusinessKeyLike(allRequestParams.get("businessKeyLike"));
        }

        if (allRequestParams.containsKey("startedBy")) {
            queryRequest.setStartedBy(allRequestParams.get("startedBy"));
        }

        if (allRequestParams.containsKey("startedBefore")) {
            queryRequest.setStartedBefore(RequestUtil.getDate(allRequestParams, "startedBefore"));
        }

        if (allRequestParams.containsKey("startedAfter")) {
            queryRequest.setStartedAfter(RequestUtil.getDate(allRequestParams, "startedAfter"));
        }

        if (allRequestParams.containsKey("activeActivityId")) {
            queryRequest.setActiveActivityId(allRequestParams.get("activeActivityId"));
        }

        if (allRequestParams.containsKey("involvedUser")) {
            queryRequest.setInvolvedUser(allRequestParams.get("involvedUser"));
        }

        if (allRequestParams.containsKey("suspended")) {
            queryRequest.setSuspended(Boolean.valueOf(allRequestParams.get("suspended")));
        }

        if (allRequestParams.containsKey("superProcessInstanceId")) {
            queryRequest.setSuperProcessInstanceId(allRequestParams.get("superProcessInstanceId"));
        }

        if (allRequestParams.containsKey("subProcessInstanceId")) {
            queryRequest.setSubProcessInstanceId(allRequestParams.get("subProcessInstanceId"));
        }

        if (allRequestParams.containsKey("excludeSubprocesses")) {
            queryRequest.setExcludeSubprocesses(Boolean.valueOf(allRequestParams.get("excludeSubprocesses")));
        }

        if (allRequestParams.containsKey("includeProcessVariables")) {
            queryRequest.setIncludeProcessVariables(Boolean.valueOf(allRequestParams.get("includeProcessVariables")));
        }

        if (allRequestParams.containsKey("callbackId")) {
            queryRequest.setCallbackId(allRequestParams.get("callbackId"));
        }

        if (allRequestParams.containsKey("callbackType")) {
            queryRequest.setCallbackType(allRequestParams.get("callbackType"));
        }

        if (allRequestParams.containsKey("tenantId")) {
            queryRequest.setTenantId(allRequestParams.get("tenantId"));
        }

        if (allRequestParams.containsKey("tenantIdLike")) {
            queryRequest.setTenantIdLike(allRequestParams.get("tenantIdLike"));
        }

        if (allRequestParams.containsKey("withoutTenantId")) {
            if (Boolean.parseBoolean(allRequestParams.get("withoutTenantId"))) {
                queryRequest.setWithoutTenantId(Boolean.TRUE);
            }
        }
        DataResponse<ProcessInstanceResponse> processQueryResponse =  getQueryResponse(queryRequest, allRequestParams);
        List<ProcessInstanceWithTasksResponse> data = new ArrayList<>();
        for (ProcessInstanceResponse processInstanceResponse: processQueryResponse.getData()){
            List<Task> tasksList = taskService.createTaskQuery().processInstanceId(processInstanceResponse.getId()).list();
            List<TaskResponse> taskResponses = restResponseFactory.createTaskResponseList(tasksList);

            ProcessInstanceWithTasksResponse processInstanceWithTasksResponse = new ProcessInstanceWithTasksResponse();
            processInstanceWithTasksResponse.setActivityId(processInstanceResponse.getActivityId());
            processInstanceWithTasksResponse.setStartUserId(processInstanceResponse.getStartUserId());
            processInstanceWithTasksResponse.setStartTime(processInstanceResponse.getStartTime());
            processInstanceWithTasksResponse.setBusinessKey(processInstanceResponse.getBusinessKey());
            processInstanceWithTasksResponse.setBusinessStatus(processInstanceResponse.getBusinessStatus());
            processInstanceWithTasksResponse.setId(processInstanceResponse.getId());
            processInstanceWithTasksResponse.setName(processInstanceResponse.getName());
            processInstanceWithTasksResponse.setProcessDefinitionId(processInstanceResponse.getProcessDefinitionId());
            processInstanceWithTasksResponse.setProcessDefinitionUrl(processInstanceResponse.getProcessDefinitionUrl());
            processInstanceWithTasksResponse.setEnded(processInstanceResponse.isEnded());
            processInstanceWithTasksResponse.setSuspended(processInstanceResponse.isSuspended());
            processInstanceWithTasksResponse.setUrl(processInstanceResponse.getUrl());
            processInstanceWithTasksResponse.setCallbackId(processInstanceResponse.getCallbackId());
            processInstanceWithTasksResponse.setCallbackType(processInstanceResponse.getCallbackType());
            processInstanceWithTasksResponse.setReferenceId(processInstanceResponse.getReferenceId());
            processInstanceWithTasksResponse.setReferenceType(processInstanceResponse.getReferenceType());
            processInstanceWithTasksResponse.setPropagatedStageInstanceId(processInstanceResponse.getPropagatedStageInstanceId());
            processInstanceWithTasksResponse.setTenantId(processInstanceResponse.getTenantId());
            processInstanceWithTasksResponse.setCompleted(processInstanceResponse.isCompleted());
            processInstanceWithTasksResponse.setVariables(processInstanceResponse.getVariables());
            processInstanceWithTasksResponse.setTasks(taskResponses);
            data.add(processInstanceWithTasksResponse);
        }

        DataResponse<ProcessInstanceWithTasksResponse> result = new DataResponse<>();
        result.setData(data);
        result.setOrder(processQueryResponse.getOrder());
        result.setSort(processQueryResponse.getSort());
        result.setSize(processQueryResponse.getSize());
        result.setTotal(processQueryResponse.getTotal());
        result.setStart(processQueryResponse.getStart());
        return result;
    }
}
