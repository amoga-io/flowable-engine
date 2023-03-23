package org.flowable.ui.application.task.listener;

import org.flowable.common.engine.impl.el.FixedValue;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Iterator;
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
                JSONObject conditions=null;
                try {
                     conditions = json.getJSONObject(i).getJSONObject("conditions");
                }
                catch (Exception e){
                  //  JSONObject  conditions=null;
                }
                JSONArray outputArray = json.getJSONObject(i).getJSONArray("output");
                if (conditions != null) {

                    String c_key = conditions.getString("key");
                    String c_value = conditions.getString("value");
                    String c_op = conditions.getString("op");

                    if ( "eq".equals(c_op) && c_value.equals(delegateTask.getVariable(c_key))) {
                        for (int j=0; j< outputArray.length(); j++) {
                            String o_key = outputArray.getJSONObject(j).getString("key");
                            String o_value = outputArray.getJSONObject(j).getString("value");
                            delegateTask.setVariable(o_key, o_value);
                        }
                    }
                }
                else
                {
                    for (int j=0; j< outputArray.length(); j++) {
                        String o_key = outputArray.getJSONObject(j).getString("key");
                        String o_value = outputArray.getJSONObject(j).getString("value");
                        delegateTask.setVariable(o_key, o_value);
                    }
                }



            }
        } catch (Exception ex){
            System.out.print("this is exception");
        }

    }
}
