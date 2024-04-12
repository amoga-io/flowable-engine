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
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.temporal.workflows.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

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
        String temporalServerAddress = "57.128.165.20:7233";
        WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
                .setTarget(temporalServerAddress)
                .build();
        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance(options);
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);

        // Specify the name of the Task Queue that this Worker should poll
        Worker worker = factory.newWorker("flowable_queue_local");

        // Specify which Workflow implementations this Worker will support
        worker.registerWorkflowImplementationTypes(CreateCaseWorkflowImpl.class, UpdateCaseWorkflowImpl.class, CompleteTaskWorkflowImpl.class, DeleteCaseWorkflowImpl.class);
        worker.registerActivitiesImplementations(new CaseActivityImpl(runtimeService, taskService));
        // Begin running the Worker
        factory.start();
    }
}
