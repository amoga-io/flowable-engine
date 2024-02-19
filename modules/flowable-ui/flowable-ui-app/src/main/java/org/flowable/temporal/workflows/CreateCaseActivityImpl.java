package org.flowable.temporal.workflows;
import org.flowable.cmmn.api.CmmnRuntimeService;
import org.flowable.cmmn.api.runtime.CaseInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
public class CreateCaseActivityImpl implements CreateCaseActivities{

    protected CmmnRuntimeService runtimeService;
    public CreateCaseActivityImpl(CmmnRuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

//    @Autowired
//    protected CmmnRuntimeService runtimeService;
    @Override
    public String createCaseActivity(Map<String,String> payload) {
        System.out.println(Thread.currentThread().getName()+"=====new Thread");
        String caseDefinationKey = payload.get("caseDefinationKey");
        String name = payload.get("name");
        String username = payload.get("username");
        Map<String, Object> startVariables = new HashMap<>();
        startVariables.put("myVar12","newvariable");
        startVariables.put("_status","accept");
        CaseInstance createdCase = runtimeService.createCase(username, startVariables, caseDefinationKey, name);
        return createdCase.getId();
    }
}
