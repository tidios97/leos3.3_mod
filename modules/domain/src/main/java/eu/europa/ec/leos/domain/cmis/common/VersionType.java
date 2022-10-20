package eu.europa.ec.leos.domain.cmis.common;

public enum VersionType {

    MAJOR(1), INTERMEDIATE(2), MINOR(3);
    private final int value;

    VersionType(int v) {
        value = v;
    }

    public int value() {
        return value;
    }

    public static VersionType fromValue(int v) {
        for (VersionType c : VersionType.values()) {
            if (c.value == v) {
                return c;
            }
        }
        throw new IllegalArgumentException(String.valueOf(v));
    }

}
