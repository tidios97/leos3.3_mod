package eu.europa.ec.leos.model.action;

public class CheckinCommentVO {
    
    private String title;
    private String description;
    
    private CheckinElement checkinElement;
    
    // Empty constructor needed for jackson ObjectMapper
    public CheckinCommentVO() {}
    
    public CheckinCommentVO(String title) {
        this.title = title;
    }
    
    public CheckinCommentVO(String title, String description) {
        this.title = title;
        this.description = description;
    }
    
    public CheckinCommentVO(String title, String description, CheckinElement checkinElement) {
        this.title = title;
        this.description = description;
        this.checkinElement = checkinElement;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public CheckinElement getCheckinElement() {
        return checkinElement;
    }
    
    public void setCheckinElement(CheckinElement checkinElement) {
        this.checkinElement = checkinElement;
    }
    
    @Override
    public String toString() {
        return "[title: " + title + ", description: " + description + ", checkinElement: " + checkinElement + "]";
    }
    
}
