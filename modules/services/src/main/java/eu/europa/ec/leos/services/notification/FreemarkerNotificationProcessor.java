package eu.europa.ec.leos.services.notification;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.europa.ec.leos.model.notification.EmailNotification;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;


@Component
public class FreemarkerNotificationProcessor {

    @Value("${leos.freemarker.ftl.notifications}")
    private String notificationBodyTemplate;
    
    private final Configuration freemarkerConfiguration;
    
    public FreemarkerNotificationProcessor(Configuration freemarkerConfiguration) {
        this.freemarkerConfiguration = freemarkerConfiguration;
    }

    public String processTemplate(EmailNotification notification) {
        StringWriter outputWriter = new StringWriter();
        String result = new String();

        try {
            Template template = freemarkerConfiguration.getTemplate(notificationBodyTemplate);
            Map<String, Object> attributes = new HashMap<String, Object>();
            attributes.put("notification_name", notification.getNotificationName());
            attributes.put("notification", notification);
            template.process(attributes, outputWriter);
            result = outputWriter.getBuffer().toString();
        } catch (MalformedTemplateNameException malformedTemplateNameException) {
            throw new RuntimeException("Invalid Template", malformedTemplateNameException);
        } catch (TemplateNotFoundException templateNotFoundException) {
            throw new RuntimeException("Tempalte not found", templateNotFoundException);
        } catch (TemplateException templateException) {
            throw new RuntimeException("Error occured while Template processing", templateException);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
        return result;
    }
}