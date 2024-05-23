package org.flowable.temporal.workflows;

import io.temporal.activity.ActivityInterface;

import java.util.Map;

@ActivityInterface
public interface CaseActivities {

    public String createCase(Map<String,Object> payload);

    public String updateCase(Map<String,Object> payload);

    public String updateAndCompleteTask(Map<String, Object> payload);

    public String deleteCase(String caseInstanceId);


}
