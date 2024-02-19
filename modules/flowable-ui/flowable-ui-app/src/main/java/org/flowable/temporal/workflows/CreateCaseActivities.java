package org.flowable.temporal.workflows;

import io.temporal.activity.ActivityInterface;

import java.util.Map;

@ActivityInterface
public interface CreateCaseActivities {

    public String createCaseActivity(Map<String,String> payload);
}
