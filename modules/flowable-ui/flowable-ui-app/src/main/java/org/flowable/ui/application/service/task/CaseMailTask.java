package org.flowable.ui.application.service.task;

import liquibase.repackaged.org.apache.commons.text.StringSubstitutor;
import org.flowable.cmmn.api.delegate.DelegatePlanItemInstance;
import org.flowable.cmmn.engine.impl.behavior.CmmnActivityBehavior;
import org.flowable.common.engine.impl.el.FixedValue;
import org.flowable.ui.application.MailService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CaseMailTask implements CmmnActivityBehavior {

    private FixedValue body;
    private FixedValue header;
    private FixedValue to;
    private FixedValue cc;
    private FixedValue bcc;
    private FixedValue mailTemplate;

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

    public FixedValue getMailTemplate() {
        return mailTemplate;
    }

    public void setMailTemplate(FixedValue mailTemplate) {
        this.mailTemplate = mailTemplate;
    }

    @Override
    public void execute(DelegatePlanItemInstance delegatePlanItemInstance) {
        Map<String, Object> variables = delegatePlanItemInstance.getVariables();
        String mailBody = StringSubstitutor.replace(body.getExpressionText(),variables,"${","}");
        String sendToList = StringSubstitutor.replace(to.getExpressionText(),variables,"${","}");
        String mailHeader = StringSubstitutor.replace(this.header.getExpressionText(),variables,"${","}");
        String[] _sendToArray = sendToList.split(",");
        List<String> sentToArray = new ArrayList<>();
        for (String to : _sendToArray) {
            if (to.trim().startsWith("${") || to.trim().equals("")) {
                continue;
            }
            sentToArray.add(to.trim());
        }
        MailService.sendEmail(sentToArray.toArray(),mailHeader,mailBody);
    }
}
