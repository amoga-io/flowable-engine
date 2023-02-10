package org.flowable.ui.application.task.listener;

import org.flowable.common.engine.impl.el.FixedValue;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
//import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.json.JSONArray;

public class SetVarriable implements TaskListener {
    private FixedValue variables;

    public FixedValue getVariables() {
        return variables;
    }

    public void setVariables(FixedValue variables) {
        this.variables = variables;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            JSONArray json = new JSONArray(getVariables().getExpressionText());
            for(int i=0;i<json.length();i++){
                String key = json.getJSONObject(i).getString("key");
                String value = json.getJSONObject(i).getString("value");
                delegateTask.setVariable(key, value);
            }

        } catch (Exception ex){

        }

    }
}
