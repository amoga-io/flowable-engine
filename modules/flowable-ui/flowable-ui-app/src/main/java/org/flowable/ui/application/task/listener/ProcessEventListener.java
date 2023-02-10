package org.flowable.ui.application.task.listener;

import org.flowable.common.engine.impl.el.FixedValue;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;

public class ProcessEventListener implements TaskListener {

    private FixedValue outcome;
    private FixedValue event;

    public FixedValue getOutcome() {
        return outcome;
    }

    public void setOutcome(FixedValue outcome) {
        this.outcome = outcome;
    }

    public FixedValue getEvent(){return event;}

    public void setEvent(FixedValue event) {
        this.event = event;
    }
    @Override
    public void notify(DelegateTask delegateTask) {
        //delegateTask.setVariable("_outcome",getOutcome().getExpressionText());
        //throw new RuntimeException("task close nahi kna hai");
        if(getOutcome()!=null){
            delegateTask.setVariable("_outcome",getOutcome().getExpressionText());
        } else {
            delegateTask.setVariable("_outcome",delegateTask.getEventName());
        }
        if(getEvent()!=null){
            delegateTask.setVariable("event",getEvent().getExpressionText());
        }
        else {
            delegateTask.setVariable("event",delegateTask.getEventName());
        }
        delegateTask.setVariable("current_task",delegateTask.getTaskDefinitionKey());
    }
}
