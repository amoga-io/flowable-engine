package org.flowable.ui.application.service.task;

import liquibase.repackaged.org.apache.commons.text.StringSubstitutor;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.el.FixedValue;
import org.flowable.ui.application.MailService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@Scope("prototype")
public class MailTask implements JavaDelegate {

    private MailService mailService = new MailService();
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
        String sendToList = StringSubstitutor.replace(to.getExpressionText(),variables,"${","}");
        String mailHeader = StringSubstitutor.replace(this.header.getExpressionText(),variables,"${","}");
        String[] _sendToArray = sendToList.split(",");
        List<String> sentToArray = new ArrayList<String>();
        for (String to : _sendToArray) {
            if (to.trim().startsWith("${") || to.trim().equals("")) {
                continue;
            }
            sentToArray.add(to.trim());
        }
        mailService.sendEmail((String[]) sentToArray.toArray(),mailHeader,mailBody);
    }
}