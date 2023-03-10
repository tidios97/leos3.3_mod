/*
 * Copyright 2017 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.services.notification;

import eu.europa.ec.leos.integration.NotificationProvider;
import eu.europa.ec.leos.model.notification.EmailNotification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class NotificationServiceImpl implements NotificationService {

    private final NotificationProvider notificationProvider;
    private final EmailNotificationProcessorFactory emailNotificationProcessorFactory;

    @Autowired
    public NotificationServiceImpl(NotificationProvider notificationProvider, 
            EmailNotificationProcessorFactory emailNotificationProcessorFactory) {
        this.notificationProvider = notificationProvider;
        this.emailNotificationProcessorFactory = emailNotificationProcessorFactory;
    }
    
    @Override
    public void sendNotification(EmailNotification emailNotification) {
        EmailNotificationProcessor emailNotificationProcessor = emailNotificationProcessorFactory.getEmailNotificationProcessor(emailNotification);
        if(emailNotificationProcessor != null) {
            emailNotificationProcessor.process(emailNotification);
            notificationProvider.sendNotification(emailNotification);    
        }else {
            throw new RuntimeException("No processor found for this notification!");
        }
    }

    @Override
    public void sendNotification(String proposalRef, String exportPackageId) {
        notificationProvider.sendNotification(proposalRef, exportPackageId);
    }

}
