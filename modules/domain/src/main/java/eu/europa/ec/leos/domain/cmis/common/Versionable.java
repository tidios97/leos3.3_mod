package eu.europa.ec.leos.domain.cmis.common;

public interface Versionable {
    String getVersionSeriesId();

    String getCmisVersionLabel();
    
    String getVersionLabel();

    String getVersionComment();

    VersionType getVersionType();

    boolean isLatestVersion();
}
