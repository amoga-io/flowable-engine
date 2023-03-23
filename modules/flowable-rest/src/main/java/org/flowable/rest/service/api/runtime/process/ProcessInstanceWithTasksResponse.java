package org.flowable.rest.service.api.runtime.process;

import org.flowable.rest.service.api.runtime.task.TaskResponse;

import java.util.ArrayList;
import java.util.List;

public class ProcessInstanceWithTasksResponse extends ProcessInstanceResponse {
    protected List<TaskResponse> tasks = new ArrayList<>();

    public void setTasks(List<TaskResponse> tasks) { this.tasks = tasks; }
    public List<TaskResponse> getTasks() { return tasks; }
    public void addTasks(TaskResponse task) { tasks.add(task); }

}
