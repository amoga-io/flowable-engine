package org.flowable.ui.application.execution.listener;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

public class TerminateAll implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) {
        System.out.println("tested");
    }
}
