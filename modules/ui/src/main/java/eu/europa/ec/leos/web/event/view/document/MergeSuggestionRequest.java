package eu.europa.ec.leos.web.event.view.document;

public class MergeSuggestionRequest {
    private final String origText;
    private final String newText;
    private final String elementId;
    private final int startOffset;
    private final int endOffset;

    public MergeSuggestionRequest(String origText, String newText, String elementId, int startOffset, int endOffset) {
        this.origText = origText;
        this.elementId = elementId;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.newText = newText;
    }

    /**
     * @return the origText
     */
    public String getOrigText() {
        return origText;
    }

    /**
     * @return the newText
     */
    public String getNewText() {
        return newText;
    }

    /**
     * @return the elementId
     */
    public String getElementId() {
        return elementId;
    }

    /**
     * @return the startOffset
     */
    public int getStartOffset() {
        return startOffset;
    }

    /**
     * @return the endOffset
     */
    public int getEndOffset() {
        return endOffset;
    }
}
