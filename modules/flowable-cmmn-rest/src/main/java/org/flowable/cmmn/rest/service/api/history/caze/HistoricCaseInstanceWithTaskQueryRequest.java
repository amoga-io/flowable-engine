package org.flowable.cmmn.rest.service.api.history.caze;

import java.util.ArrayList;
import java.util.List;

public class HistoricCaseInstanceWithTaskQueryRequest extends HistoricCaseInstanceQueryRequest{
    private List<String> taskAssigneeList = new ArrayList<>();

    public List<String> getTaskAssigneeList() {
        return taskAssigneeList;
    }

    public void setTaskAssigneeList(List<String> taskAssigneeList) {
        this.taskAssigneeList = taskAssigneeList;
    }
}
