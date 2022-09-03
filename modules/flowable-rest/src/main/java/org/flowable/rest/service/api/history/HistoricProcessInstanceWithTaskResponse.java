package org.flowable.rest.service.api.history;

import java.util.ArrayList;
import java.util.List;

public class HistoricProcessInstanceWithTaskResponse extends HistoricProcessInstanceResponse{
    protected List<HistoricTaskInstanceResponse> tasks = new ArrayList<>();

    public void addTask(HistoricTaskInstanceResponse task) {
        tasks.add(task);
    }

    public List<HistoricTaskInstanceResponse> getTasks() {
        return tasks;
    }

    public void setTasks(List<HistoricTaskInstanceResponse> tasks) {
        this.tasks = tasks;
    }
}
