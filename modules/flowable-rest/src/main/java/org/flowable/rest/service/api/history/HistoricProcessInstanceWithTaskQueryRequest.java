package org.flowable.rest.service.api.history;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.flowable.idm.api.Group;
import org.flowable.rest.service.api.engine.variable.QueryVariable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class HistoricProcessInstanceWithTaskQueryRequest extends HistoricProcessInstanceQueryRequest {

    private String taskCandidateGroup;
    private List<String> taskAssigneeList = new ArrayList<>();

    public List<String> getTaskAssigneeList() {
        return taskAssigneeList;
    }

    public void setTaskAssigneeList(List<String> taskAssigneeList) {
        this.taskAssigneeList = taskAssigneeList;
    }

    public String getTaskCandidateGroup() {
        return taskCandidateGroup;
    }

    public void setTaskCandidateGroup(String taskCandidateGroup) {
        this.taskCandidateGroup = taskCandidateGroup;
    }
}
