package eu.europa.ec.leos.services.notification;

import eu.europa.ec.leos.model.notification.EmailNotification;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationProcessorFactoryImpl implements EmailNotificationProcessorFactory {
    private List<EmailNotificationProcessor> emailNotificationProcessors;

    @Autowired
    EmailNotificationProcessorFactoryImpl(List<EmailNotificationProcessor> emailNotificationProcessors) {
        this.emailNotificationProcessors = emailNotificationProcessors;
    }

    public <T extends EmailNotification> EmailNotificationProcessor getEmailNotificationProcessor(T emailNotification) {
            for (EmailNotificationProcessor emailNotificationProcessor : emailNotificationProcessors) {
                if(emailNotificationProcessor.canProcess(emailNotification)) {
                    return emailNotificationProcessor;
                }
            }
            return null;
    }
}
