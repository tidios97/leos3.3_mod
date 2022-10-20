package eu.europa.ec.leos.services.compare;

public interface TextComparator {

    String compareTextNodeContents(String firstContent, String secondContent, String intermediateContent, ContentComparatorContext context);
    
    String[] twoColumnsCompareTextNodeContents(String firstContent, String secondContent);
}
