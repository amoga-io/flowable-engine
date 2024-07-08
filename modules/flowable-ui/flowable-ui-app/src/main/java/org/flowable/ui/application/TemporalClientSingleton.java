package org.flowable.ui.application;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.client.WorkflowClientOptions;
import org.flowable.temporal.workflows.HandleFlowableData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TemporalClientSingleton {
    private static TemporalClientSingleton instance;
    private final Map<String, WorkflowClient> workflowClientMap;
    private final Map<String, String> queueNameMap;

    private TemporalClientSingleton() {
        workflowClientMap = new ConcurrentHashMap<>();
        queueNameMap = new ConcurrentHashMap<>();
    }

    public static synchronized TemporalClientSingleton getInstance() {
        if (instance == null) {
            instance = new TemporalClientSingleton();
        }
        return instance;
    }

    public WorkflowClient getWorkflowClient(String temporalServerAddress, String namespace) {
        if (!workflowClientMap.containsKey(namespace)) {
            WorkflowServiceStubsOptions serviceStubsOptions = WorkflowServiceStubsOptions.newBuilder()
                    .setTarget(temporalServerAddress)
                    .build();
            WorkflowServiceStubs service = WorkflowServiceStubs.newInstance(serviceStubsOptions);

            WorkflowClientOptions clientOptions = WorkflowClientOptions.newBuilder()
                    .setNamespace(namespace)
                    .build();
            WorkflowClient workflowClient = WorkflowClient.newInstance(service, clientOptions);

            workflowClientMap.put(namespace, workflowClient);
            queueNameMap.put(namespace, "handle_flowable_queue_" + namespace);
        }
        return workflowClientMap.get(namespace);
    }

    public void startWorkflowAsync(String namespace, String workflowFlowId, Map<String, Object> eventData) {
        WorkflowClient workflowClient = workflowClientMap.get(namespace);
        String queueName = queueNameMap.get(namespace);

        WorkflowOptions options = WorkflowOptions.newBuilder()
                .setTaskQueue(queueName)
                .setWorkflowId(workflowFlowId)
                .build();

        HandleFlowableData workflow = workflowClient.newWorkflowStub(HandleFlowableData.class, options);

        // Asynchronously start the workflow
        WorkflowClient.start(workflow::handle_flowable_data, eventData);
    }
}
