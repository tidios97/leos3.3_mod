package eu.europa.ec.leos.services.notification;

import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.notification.EmailNotification;
import eu.europa.ec.leos.model.notification.cloneProposal.RevisionDoneNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RevisionDoneNotificationProcessor implements EmailNotificationProcessor<RevisionDoneNotification> {

    private final FreemarkerNotificationProcessor processor;
    private MessageHelper messageHelper;

    @Autowired
    public RevisionDoneNotificationProcessor(FreemarkerNotificationProcessor processor, MessageHelper messageHelper) {
        this.processor = processor;
        this.messageHelper = messageHelper;
    }

    @Override
    public void process(RevisionDoneNotification emailNotification) {
        buildEmailBody(emailNotification);
        buildEmailSubject(emailNotification);
    }

    @Override
    public boolean canProcess(EmailNotification emailNotification) {
        return RevisionDoneNotification.class.isAssignableFrom(emailNotification.getClass());
    }

    private void buildEmailBody(RevisionDoneNotification emailNotification) {
        emailNotification.setEmailBody(processor.processTemplate(emailNotification));
    }

    private void buildEmailSubject(RevisionDoneNotification emailNotification) {
        String title = emailNotification.getTitle();
        emailNotification
                .setEmailSubject(messageHelper.getMessage(emailNotification.getEmailSubjectKey(), title));
    }
}
