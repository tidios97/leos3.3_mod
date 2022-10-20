package eu.europa.ec.leos.services.notification;

import eu.europa.ec.leos.i18n.MessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.europa.ec.leos.model.notification.EmailNotification;
import eu.europa.ec.leos.model.notification.validation.ValidationEmailNotification;

@Component
public class ValidationNotificationProcessor implements EmailNotificationProcessor<ValidationEmailNotification> {

    @Autowired
    private MessageHelper messageHelper;

    private final FreemarkerNotificationProcessor processor;

    @Autowired
    public ValidationNotificationProcessor(FreemarkerNotificationProcessor processor, MessageHelper messageHelper) {
        this.processor = processor;
        this.messageHelper = messageHelper;
    }

    @Override
    public void process(ValidationEmailNotification emailNotification) {
        buildEmailBody(emailNotification);
        buildEmailSubject(emailNotification);

    }

    @Override
    public boolean canProcess(EmailNotification emailNotification) {
        if (ValidationEmailNotification.class.isAssignableFrom(emailNotification.getClass())) {
            return true;
        } else {
            return false;
        }
    }

    private void buildEmailBody(ValidationEmailNotification validationEmailNotification) {
        validationEmailNotification.setEmailBody(processor.processTemplate(validationEmailNotification));
    }

    private void buildEmailSubject(ValidationEmailNotification validationEmailNotification) {
        String title = validationEmailNotification.getTitle();
        validationEmailNotification
                .setEmailSubject(messageHelper.getMessage(validationEmailNotification.getEmailSubjectKey(), new Object[]{title}));
    }
}
