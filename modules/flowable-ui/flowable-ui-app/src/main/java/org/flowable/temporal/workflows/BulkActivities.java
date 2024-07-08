package org.flowable.temporal.workflows;

import io.temporal.activity.ActivityInterface;

import java.util.List;
import java.util.Map;
@ActivityInterface
public interface BulkActivities {
    public Map<String,Integer> createCaseBulk(Map<String,Object> payload);

    public Map<String,Integer> updateCaseBulk(Map<String,Object> payload);

    public Map<String,Integer> updateAndCompleteTaskBulk(Map<String, Object> payload);
}
