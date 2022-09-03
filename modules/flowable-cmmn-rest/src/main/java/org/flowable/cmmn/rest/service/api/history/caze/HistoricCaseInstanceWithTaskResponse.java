package org.flowable.cmmn.rest.service.api.history.caze;

import org.flowable.cmmn.rest.service.api.history.task.HistoricTaskInstanceResponse;

import java.util.ArrayList;
import java.util.List;

public class HistoricCaseInstanceWithTaskResponse extends HistoricCaseInstanceResponse{
    protected List<HistoricTaskInstanceResponse> tasks = new ArrayList<>();

    public List<HistoricTaskInstanceResponse> getTasks() {
        return tasks;
    }

    public void setTasks(List<HistoricTaskInstanceResponse> tasks) {
        this.tasks = tasks;
    }
}
