package eu.europa.ec.leos.cmis.domain;

import eu.europa.ec.leos.domain.cmis.Content;

public class ContentImpl implements Content {

    private final String fileName;
    private final String mimeType;
    private final long length;
    private final Source source;

    public ContentImpl(String fileName, String mimeType, long length, Source source) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.length = length;
        this.source = source;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public long getLength() {
        return length;
    }

    @Override
    public Source getSource() {
        return source;
    }
}
