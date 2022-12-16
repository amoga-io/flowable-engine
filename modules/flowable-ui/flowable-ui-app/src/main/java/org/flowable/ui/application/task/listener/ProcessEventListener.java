package org.flowable.ui.application.task.listener;

import org.flowable.common.engine.impl.el.FixedValue;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;

public class ProcessEventListener implements TaskListener {

    //    private FixedValue event;
//    public FixedValue getEvent() {
//        return event;
//    }
//
//    public void setEvent(FixedValue event) {
//        this.event = event;
//    }//

    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.setVariable("_outcome","toDo");
        delegateTask.setVariable("event","start");
        //delegateTask.setVariable("event",event);
        delegateTask.setVariable("current_task",delegateTask.getTaskDefinitionKey());
    }
}
