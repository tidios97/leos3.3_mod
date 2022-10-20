package eu.europa.ec.leos.vo.toc.indent;

public enum IndentedItemType {
    POINT,                      // Point or indent with out any children
    FIRST_SUBPOINT,             // Point with children
    OTHER_SUBPOINT,             // Subpoint without any children
    PARAGRAPH,
    FIRST_SUBPARAGRAPH,
    OTHER_SUBPARAGRAPH,
    RESTORED
}
