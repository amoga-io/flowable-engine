package org.flowable.ui.application;

import org.flowable.cmmn.api.CmmnTaskService;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class AutoCompleteTaskListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        ApplicationContext context = ApplicationContextProvider.getApplicationContext();
        CmmnTaskService taskService = context.getBean(CmmnTaskService.class);
        taskService.complete(delegateTask.getId());
    }
}
