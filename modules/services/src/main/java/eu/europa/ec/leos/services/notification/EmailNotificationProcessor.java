package eu.europa.ec.leos.services.notification;

import eu.europa.ec.leos.model.notification.EmailNotification;

public interface EmailNotificationProcessor<T extends EmailNotification> {

    void process(T emailNotification);
    
    boolean canProcess(EmailNotification emailNotification);
}
