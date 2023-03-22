package org.flowable.ui.application.task.listener;

import org.apache.tomcat.util.json.JSONParser;
import org.flowable.cmmn.api.CmmnHistoryService;
import org.flowable.cmmn.api.history.HistoricCaseInstanceQuery;
import org.flowable.common.engine.impl.el.FixedValue;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.flowable.ui.application.FlowableUiAppEventRegistryCondition.environmentMap;

public class ValidateTaskBeforeClose implements TaskListener {
    @Autowired
    protected CmmnHistoryService historyService;
    private FixedValue whenEvent;
    private FixedValue taskName;

    public FixedValue getWhenEvent() {
        return whenEvent;
    }

    public void setWhenEvent(FixedValue whenEvent) {
        this.whenEvent = whenEvent;
    }

    public FixedValue getTaskName() {
        return taskName;
    }

    public void setTaskName(FixedValue taskName) {
        this.taskName = taskName;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        try{
            RestTemplate restTemplate = new RestTemplate();
            String caseInstanceId = ((TaskEntityImpl) delegateTask).getScopeId();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth("abhi","password");
            HttpEntity<Map<String, Object>> entity = new HttpEntity(new HashMap<String,Object>(), headers);
            String baseUrl = "http://localhost:8080/flowable-ui/cmmn-api/cmmn-history/historic-planitem-instances?planItemDefinitionType=humantask&caseInstanceId="+caseInstanceId;

            String response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class, new Object[0]).getBody();
            JSONObject json = new JSONObject(response);
            //System.out.println(json);
            JSONArray arr=json.getJSONArray("data");
            String taskName=getTaskName().getExpressionText();
            for(int i=0; i<arr.length(); i++){
                String o = arr.getJSONObject(i).getString("planItemDefinitionId");
                String st = arr.getJSONObject(i).getString("state");
                if(o.equals(taskName)&&(getWhenEvent().getExpressionText()).equals("start")){
                    if(st.equals("available")){
                        throw new RuntimeException("Sorry!!can not close this task");
                    }
                }
                if(o.equals(taskName)&&(getWhenEvent().getExpressionText()).equals("complete")){
                    if(!st.equals("completed")){
                        throw new RuntimeException("Sorry!!can not close this task");
                    }
                }

            }



        } catch (RuntimeException ex) {
            throw new RuntimeException("outer exception");
        }
        catch (Exception ex) {
            System.out.println("Error"+ex);
        }
    }

}
