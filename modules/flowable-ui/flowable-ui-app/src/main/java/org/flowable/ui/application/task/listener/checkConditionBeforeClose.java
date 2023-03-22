package org.flowable.ui.application.task.listener;

import org.flowable.common.engine.impl.el.FixedValue;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.json.JSONArray;
import org.json.JSONObject;

public class checkConditionBeforeClose implements TaskListener {
    private FixedValue condition;

    public FixedValue getCondition() {
        return condition;
    }

    public void setCondition(FixedValue condition) {
        this.condition = condition;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            JSONObject json = new JSONObject(getCondition().getExpressionText());


                JSONArray conditinArray=json.getJSONArray("condition");
                String operator=json.getString("operator");
                for(int j=0;j<conditinArray.length();j++){
                    String c_key = conditinArray.getJSONObject(j).getString("key");
                    String c_value = conditinArray.getJSONObject(j).getString("value");
                    String c_op=conditinArray.getJSONObject(j).getString("op");
                    if(operator.equals("and")) {
                        if ("eq".equals(c_op) && !c_value.equals(delegateTask.getVariable(c_key))) {
                            throw new RuntimeException("condition is not true");
                        }
                    }
                    else if(operator.equals("or")){
                        if ("eq".equals(c_op) && c_value.equals(delegateTask.getVariable(c_key))) {
                            break;
                        }
                    }
                }

        }
        catch (RuntimeException e){
            throw new RuntimeException("condition is not true");
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
}
