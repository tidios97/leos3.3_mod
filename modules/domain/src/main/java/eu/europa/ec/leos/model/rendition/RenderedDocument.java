package eu.europa.ec.leos.model.rendition;

import java.io.InputStream;

public class RenderedDocument {

    private String styleSheetName;
    private InputStream content;

    public String getStyleSheetName() {
        return styleSheetName;
    }

    public InputStream getContent() {
        return content;
    }

    public void setStyleSheetName(String styleSheetName) {
        this.styleSheetName = styleSheetName;
    }

    public void setContent(InputStream content) {
        this.content = content;
    }
}
