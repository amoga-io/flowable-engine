package org.flowable.ui.application;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

public class MailService {

    public static void sendEmail(Object[] toAddress,String subject, String htmlBody)
    {
        try{
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> body = new HashMap<String,Object>();
            body.put("email_address", toAddress);
            body.put("subject", subject);
            body.put("html", htmlBody);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity(body, headers);
            String response = restTemplate.exchange("https://app1.amoga.io/api/v1/core/sendmail", HttpMethod.POST, entity, String.class, new Object[0]).getBody();
            System.out.println(response);
        } catch (Exception ex) {
            System.out.println("Error while sending mail"+ex);
        }
    }
}
