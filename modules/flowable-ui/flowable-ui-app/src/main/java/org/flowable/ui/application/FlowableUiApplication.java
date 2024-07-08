/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.flowable.ui.application;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.temporal.workflows.BulkActivitiesImpl;
import org.flowable.temporal.workflows.CaseActivityImpl;
import org.flowable.temporal.workflows.FlowableWorkflowBulkImpl;
import org.flowable.temporal.workflows.FlowableWorkflowImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import java.util.Arrays;
import java.util.List;

import static org.flowable.ui.application.FlowableUiAppEventRegistryCondition.environmentMap;

/**
 * @author Filip Hrisafov
 */
@SpringBootApplication
public class FlowableUiApplication extends SpringBootServletInitializer implements CommandLineRunner {

    @Autowired
    protected CmmnRuntimeService runtimeService;

    @Autowired
    protected CmmnTaskService taskService;

    public static void main(String[] args) {
        SpringApplication.run(FlowableUiApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(FlowableUiApplication.class);
    }

    @Override
    public void run(String... args) throws Exception {
        String temporalServerAddress = environmentMap.get("temporalServerAddress");
//        String temporalServerAddress = "57.128.165.20:7233";
        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalServerAddress)
                .build();
        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance(options);
        String temporalNamespaces = environmentMap.get("temporalNamespaces");
        List<String> temporalNamespacesList = Arrays.asList(temporalNamespaces.split(","));
        for (String namespace : temporalNamespacesList) {
            this.startWorker(namespace, service, false);
            this.startWorker(namespace, service, true);

//            WorkflowClientOptions clientOptions = WorkflowClientOptions.newBuilder().setNamespace(namespace).build();
//            WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);
//            WorkerFactory factory = WorkerFactory.newInstance(client);
//            Worker worker = factory.newWorker("flowable_bulk_queue_" + namespace);
//            worker.registerWorkflowImplementationTypes(FlowableWorkflowBulkImpl.class);
//            worker.registerActivitiesImplementations(new Object[]{new BulkActivitiesImpl(this.runtimeService, this.taskService)});
//            factory.start();
        }
//        this.startWorker((String)FlowableUiAppEventRegistryCondition.environmentMap.get("temporalBulkNamespace"), service, FlowableWorkflowBulkImpl.class);
    }

    private void startWorker(String namespace, WorkflowServiceStubs service, boolean is_bulk) throws Exception {
        WorkflowClientOptions clientOptions = WorkflowClientOptions.newBuilder().setNamespace(namespace).build();
        WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);
        WorkerFactory factory = WorkerFactory.newInstance(client);
        if (is_bulk) {
            Worker worker = factory.newWorker("flowable_bulk_queue_" + namespace);
            worker.registerWorkflowImplementationTypes(FlowableWorkflowBulkImpl.class);
            worker.registerActivitiesImplementations(new Object[]{new BulkActivitiesImpl(this.runtimeService, this.taskService)});
        } else {
            Worker worker = factory.newWorker("flowable_queue_" + namespace);
            worker.registerWorkflowImplementationTypes(FlowableWorkflowImpl.class);
            worker.registerActivitiesImplementations(new Object[]{new CaseActivityImpl(this.runtimeService, this.taskService)});
        }
        factory.start();
    }
}
