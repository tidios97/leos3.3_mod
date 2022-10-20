package eu.europa.ec.leos.services.notification;

import eu.europa.ec.leos.model.notification.EmailNotification;

public interface EmailNotificationProcessorFactory {
    <T extends EmailNotification> EmailNotificationProcessor  getEmailNotificationProcessor(T emailNotification);
}
