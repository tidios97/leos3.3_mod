package eu.europa.ec.leos.services.numbering.depthBased;

import java.util.Arrays;

public enum ClassToDepthType {
    TYPE_1(1),
    TYPE_2(2),
    TYPE_3(3),
    TYPE_4(4);

    private Integer depth;

    ClassToDepthType(Integer depth) {
        this.depth = depth;
    }

    public Integer getDepth() {
        return depth;
    }

    public static ClassToDepthType of(String type) {
        return Arrays.asList(ClassToDepthType.values()).stream()
                .filter(x -> x.name().equalsIgnoreCase(type)).findFirst().orElse(null);
    }

    public static ClassToDepthType ofDepth(int depth) {
        for (ClassToDepthType e : values()) {
            if (e.getDepth().equals(depth)) {
                return e;
            }
        }
        return null;
    }
}
