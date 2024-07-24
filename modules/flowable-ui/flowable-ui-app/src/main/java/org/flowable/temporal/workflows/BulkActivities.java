package org.flowable.temporal.workflows;

import io.temporal.activity.ActivityInterface;

import java.util.List;
import java.util.Map;
@ActivityInterface
public interface BulkActivities {
    public Map<String,Object> createCaseBulk(Map<String,Object> payload);

    public Map<String,Object> updateCaseBulk(Map<String,Object> payload);

    public Map<String,Object> updateAndCompleteTaskBulk(Map<String, Object> payload);
}
