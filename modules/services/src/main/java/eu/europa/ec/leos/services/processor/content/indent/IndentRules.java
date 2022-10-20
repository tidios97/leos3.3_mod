package eu.europa.ec.leos.services.processor.content.indent;

public class IndentRules {
    public enum ChildrenRule {
        NONE,
        ALL,
        FIRST
    }

    public static ChildrenRule getIndentChildrenRule() {
        return ChildrenRule.NONE;
    }

    public static ChildrenRule getOutdentChildrenRule() {
        return ChildrenRule.FIRST;
    }
}
