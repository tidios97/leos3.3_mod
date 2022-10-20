package eu.europa.ec.leos.model.notification.pdfGeneration;

public class PDFGenerationNotification extends PDFGenerationEmailNotification {

    public PDFGenerationNotification(String recipient, String title, byte[] attachmentContent, String legFileName) {
        super(recipient, title, attachmentContent, legFileName);
    }

    @Override
    public String getEmailSubjectKey() {
        return "notification.pdf.generation.subject";
    }

    @Override
    public String getAttachmentName() {
        return "proposal.zip";
    }
    
    @Override
    public String getMimeType() {
        return "application/zip";
    }
    
    @Override
    public boolean withAttachment() {
        return true;
    }
}
