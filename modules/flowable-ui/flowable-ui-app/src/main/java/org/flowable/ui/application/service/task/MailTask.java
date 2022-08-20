package org.flowable.ui.application.service.task;

import liquibase.repackaged.org.apache.commons.text.StringSubstitutor;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.el.FixedValue;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Scope("prototype")
public class MailTask implements JavaDelegate {
    private FixedValue body;
    private FixedValue header;
    private FixedValue to;
    private FixedValue cc;
    private FixedValue bcc;

    public FixedValue getBody() {
        return body;
    }

    public void setBody(FixedValue body) {
        this.body = body;
    }

    public FixedValue getHeader() {
        return header;
    }

    public void setHeader(FixedValue header) {
        this.header = header;
    }

    public FixedValue getTo() {
        return to;
    }

    public void setTo(FixedValue to) {
        this.to = to;
    }

    public FixedValue getCc() {
        return cc;
    }

    public void setCc(FixedValue cc) {
        this.cc = cc;
    }

    public FixedValue getBcc() {
        return bcc;
    }

    public void setBcc(FixedValue bcc) {
        this.bcc = bcc;
    }

    @Override
    public void execute(DelegateExecution execution) {

        System.out.println(execution.getProcessDefinitionId());
        Map<String, Object> variables = execution.getVariables();
        String mailBody = StringSubstitutor.replace(body.getExpressionText(),variables,"${","}");
        sendEmail(mailBody);
    }

    private void sendEmail(String mailBody){
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> body = new HashMap<String, String>();
        body.put("email_address", to.getExpressionText());
        body.put("subject", this.header.getExpressionText());
        body.put("body", mailBody);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<Map<String, String>>(body,headers);
        restTemplate.exchange("https://dev.amoga.io/api/v1/core/sendmail", HttpMethod.POST, entity, String.class).getBody();
    }
}