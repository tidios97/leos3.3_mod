package eu.europa.ec.leos.domain.cmis;

import java.io.InputStream;

public interface Content {

    String getFileName();

    String getMimeType();

    long getLength();

    Content.Source getSource();

    interface Source {
        InputStream getInputStream();

        byte[] getBytes();
    }
}
