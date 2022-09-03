package org.flowable.ui.application.task.listener;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.ui.application.MailService;

public class AssignmentListener implements TaskListener {

    private MailService mailService = new MailService();

    @Override
    public void notify(DelegateTask delegateTask) {
        if ("assignment".equalsIgnoreCase(delegateTask.getEventName())) {
            String[] toAddress = {delegateTask.getAssignee()};
            mailService.sendEmail(toAddress, "New Task Assigned", String.format("New Task %s with ID %s assigned.",
                    delegateTask.getTaskDefinitionKey(), delegateTask.getId()));
        }
    }
}
