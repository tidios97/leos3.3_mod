package eu.europa.ec.leos.integration.rest;

import eu.europa.ec.leos.integration.NotificationProvider;
import eu.europa.ec.leos.model.notification.EmailNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationProviderImplOs implements NotificationProvider {
    private static Logger LOG = LoggerFactory.getLogger(NotificationProviderImplOs.class);
    
    public void sendNotification(EmailNotification emailNotification) {
        LOG.info("This feature is not available for this distribution");
    }

    public void sendNotification(String proposalRef, String exportPackageId) {
        LOG.info("This feature is not available for this distribution");
    }
}
