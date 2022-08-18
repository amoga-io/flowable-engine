package org.flowable.ui.application.lifecycle.listener;

import org.flowable.cmmn.api.delegate.DelegatePlanItemInstance;
import org.flowable.cmmn.api.listener.PlanItemInstanceLifecycleListener;

public class AmogaLifecycleListener implements PlanItemInstanceLifecycleListener {

    @Override
    public String getSourceState() {
        return null;
    }

    @Override
    public String getTargetState() {
        return null;
    }

    @Override
    public void stateChanged(DelegatePlanItemInstance planItemInstance, String oldState, String newState) {
        System.out.println(planItemInstance);
    }
}
