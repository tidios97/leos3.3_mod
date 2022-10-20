package eu.europa.ec.leos.services.notification;

import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.notification.EmailNotification;
import eu.europa.ec.leos.model.notification.pdfGeneration.PDFGenerationEmailNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PDFGenerationNotificationProcessor implements EmailNotificationProcessor<PDFGenerationEmailNotification> {

    @Autowired
    private MessageHelper messageHelper;

    private final FreemarkerNotificationProcessor processor;

    @Autowired
    public PDFGenerationNotificationProcessor(FreemarkerNotificationProcessor processor, MessageHelper messageHelper) {
        this.processor = processor;
        this.messageHelper = messageHelper;
    }

    @Override
    public void process(PDFGenerationEmailNotification emailNotification) {
        buildEmailBody(emailNotification);
        buildEmailSubject(emailNotification);

    }

    @Override
    public boolean canProcess(EmailNotification emailNotification) {
        return PDFGenerationEmailNotification.class.isAssignableFrom(emailNotification.getClass());
    }

    private void buildEmailBody(PDFGenerationEmailNotification pdfGenerationEmailNotification) {
        pdfGenerationEmailNotification.setEmailBody(processor.processTemplate(pdfGenerationEmailNotification));
    }

    private void buildEmailSubject(PDFGenerationEmailNotification pdfGenerationEmailNotification) {
        String title = pdfGenerationEmailNotification.getTitle();
        pdfGenerationEmailNotification
                .setEmailSubject(messageHelper.getMessage(pdfGenerationEmailNotification.getEmailSubjectKey(), new Object[]{title}));
    }
}
