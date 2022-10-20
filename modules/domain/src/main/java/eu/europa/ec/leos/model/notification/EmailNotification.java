package eu.europa.ec.leos.model.notification;

import java.util.List;

public interface EmailNotification {
    List<String> getRecipients();

    String getNotificationName();

    String getEmailSubject();

    String getEmailBody();
    
    byte[] getAttachmentContent();
    
    String getAttachmentName();
    
    String getMimeType();
    
    boolean withAttachment();
}
