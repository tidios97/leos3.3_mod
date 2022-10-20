package eu.europa.ec.leos.services.compare.processor;

import eu.europa.ec.leos.services.compare.ContentComparatorContext;
import eu.europa.ec.leos.services.support.ByteArrayBuilder;
import eu.europa.ec.leos.services.support.XercesUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_REMOVED_CLASS_CN;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_SOFT_ADDED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.CONTENT_SOFT_REMOVED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_ADDED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_REMOVED_CLASS;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_INTERMEDIATE_STYLE;
import static eu.europa.ec.leos.services.compare.ContentComparatorService.DOUBLE_COMPARE_ORIGINAL_STYLE;
import static eu.europa.ec.leos.services.support.XercesUtils.createXercesDocument;
import static eu.europa.ec.leos.services.support.XercesUtils.nodeToByteArray;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_ACTION_ROOT_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.UTF_8;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.getClosingTag;
import static eu.europa.ec.leos.services.support.XmlHelper.getOpeningTag;

public class PostDiffingProcessor {

    private final String INSERT = "ins";
    private final String DELETE = "del";
    private final String REVISED = "revised";
    private final String VERIFICATION = "verification";
    private final String CLASS_ATTR = "class";

    private Set<String> xmlIds;

    public boolean identicalContentOriginalCurrent;


    /**
     * Adjust identical removed text between v1 and v2 (happens with footnote between)
     * @param content
     * @param context
     * @return diffing result adjusted
     */
    public String adjustIdenticalRemovedTextIntermediateOriginal(String content, ContentComparatorContext context) {

        content = content.replace("<span class=\"" + context.getRemovedIntermediateValue() + "\"></span>", "");
        content = content.replace("<span class=\"" + context.getRemovedOriginalValue() + "\"></span>", "");

        List<String> removedIntermediateList = getRemovedSpansWithClass(content, context.getRemovedIntermediateValue());
        List<String> removedOriginalList = getRemovedSpansWithClass(content, context.getRemovedOriginalValue());

        for(String removedIntermediateText:removedIntermediateList) {
            for(String removedOriginalText:removedOriginalList) {
                if(removedOriginalText.contains(removedIntermediateText)) {
                    content = content.replaceFirst("<span class=\"" + context.getRemovedOriginalValue() + "\">" + removedOriginalText + "</span>", "");
                    content = content.replaceFirst("<span class=\"" + context.getRemovedIntermediateValue() + "\">" + removedIntermediateText + "</span>", "<span class=\"" + context.getRemovedOriginalValue() + " " + context.getRemovedIntermediateValue()+ "\">" + removedIntermediateText + "</span>");
                }
            }
        }
        return content;
    }

    /**
     * Adjust softaction for deleted elements on simple diffing
     * @param content
     * @return diffing result adjusted
     */
    public String adjustSoftActionDiffing(String content) {

        content = content.replaceAll("-removed\" leos:softaction=\"add\"","-removed\" leos:softaction=\"del\"");
        content = content.replaceAll( "<span class=\"" + DOUBLE_COMPARE_ADDED_CLASS + "\"><authorialNote class=\"leos-content-soft-removed\"", "<span><authorialNote class=\"leos-content-soft-removed\"");

        Pattern pattern = Pattern.compile("-removed\" (.*?) leos:softaction=\"add\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {

            String attr = matcher.group(1);
            if(!attr.contains("</") && !attr.contains(">")) {
                content = content.replace("-removed\" " + attr + " leos:softaction=\"add\"","-removed\" " + attr + " leos:softaction=\"del\"");
            }

        }
        return content;

    }

    /**
     * Adjust softaction for deleted elements on double diffing
     * @param content
     * @return diffing result adjusted
     */
    public static String adjustSoftActionDoubleDiffing(String content) {

        content = content.replaceAll(DOUBLE_COMPARE_REMOVED_CLASS + DOUBLE_COMPARE_INTERMEDIATE_STYLE + "\" leos:softaction=\"add\"",DOUBLE_COMPARE_REMOVED_CLASS + DOUBLE_COMPARE_INTERMEDIATE_STYLE + "\" leos:softaction=\"del\"");
        content = content.replaceAll(DOUBLE_COMPARE_REMOVED_CLASS + DOUBLE_COMPARE_ORIGINAL_STYLE + "\" leos:softaction=\"add\"",DOUBLE_COMPARE_REMOVED_CLASS + DOUBLE_COMPARE_ORIGINAL_STYLE + "\" leos:softaction=\"del\"");

        content = content.replaceAll( "<span class=\"" + DOUBLE_COMPARE_REMOVED_CLASS + DOUBLE_COMPARE_INTERMEDIATE_STYLE + "\"><authorialNote class=\"leos-content-soft-removed\"", "<span><authorialNote class=\"leos-content-soft-removed\"");
        content = content.replaceAll( "<span class=\"" + DOUBLE_COMPARE_REMOVED_CLASS + DOUBLE_COMPARE_ORIGINAL_STYLE + "\"><authorialNote class=\"leos-content-soft-removed\"", "<span><authorialNote class=\"leos-content-soft-removed\"");

        Pattern pattern = Pattern.compile("-removed-(.*?)\" (.*?) leos:softaction=\"add\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {

            String version = matcher.group(1);
            String attr = matcher.group(2);
            if(!version.contains("</") && !attr.contains("</") && !version.contains(">") && !attr.contains(">")) {
                content = content.replaceAll("-removed-" + version + "\" " + attr + " leos:softaction=\"add\"","-removed-" + version + "\" " + attr + " leos:softaction=\"del\"");
            }

        }
        return content;

    }

    /**
     * Adjust softaction for deleted elements on double diffing
     * @param content
     * @return diffing result adjusted
     */
    public static String adjustTagsDiffing(String content) {

        content = content.replaceAll("<ins=\"\">", "");
        content = content.replaceAll("<ins=\"\" class=\"revised\">", "");
        content = content.replaceAll("<ins=\"\" class=\"verification\">", "");

        Pattern p = Pattern.compile("<del[^>]*>(.*?)</del>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        content = p.matcher(content).replaceAll("");

        Pattern pattern = Pattern.compile("<del>(.*?)</del>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {

            String version = matcher.group(1);

            if(!version.contains("</") && !version.contains(">")) {
                content = content.replaceAll("<del>" + version + "</del>", "");
            }

        }
        content = content.replaceAll("&gt;", ">");
        return content;

    }

    /**
     * Adjust diffing result on authorial notes for deleted elements
     * - only show one marker
     * - put the right style
     * @param content
     * @return diffing result adjusted
     */
    public String adjustMarkersAuthorialNotes(String content) {
        xmlIds = new HashSet<>();
        content = adjustMarkersAuthorialNotesPattern(content, "<authorialNote id=\"(.*?)\" leos:origin=\"(.*?)\" marker=\"(.*?)\"");
        content = adjustMarkersAuthorialNotesPattern(content, "<authorialNote class=\"" + CONTENT_SOFT_REMOVED_CLASS +"\" id=\"(.*?)\" leos:origin=\"(.*?)\" marker=\"(.*?)\"");
        content = adjustMarkersAuthorialNotesPattern(content, "<authorialNote class=\"" + CONTENT_SOFT_REMOVED_CLASS +"\" id=\"(.*?)\" marker=\"(.*?)\" ");
        content = adjustMovedFootnotes(content);
        return content;
    }

    public String adjustMarkersAuthorialNotesPattern(String content, String regex) {

        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            Integer groupCount = matcher.groupCount();
            String id = matcher.group(1);
            String origin = matcher.group(groupCount - 1);
            String marker = matcher.group(groupCount);
            id = id.replace("doubleCompare-", "");

            if(xmlIds.contains(marker + id)) {
                content = replaceMarkers(content, marker, id, origin);
            } else {
                xmlIds.add(marker + id);
            }

            //Adjust endNote style for live diffing
            if(regex.contains(CONTENT_SOFT_REMOVED_CLASS)) {
                content = content.replace("<span class=\"leos-authnote\" id=\"endNote_" +id,"<span class=\"" + CONTENT_SOFT_REMOVED_CLASS + " leos-authnote\" id=\"endNote_" +id);
                content = content.replace("<span class=\"leos-authnote\" id=\"doubleCompare-endNote_" +id,"<span class=\"" + CONTENT_SOFT_REMOVED_CLASS+ " leos-authnote\" id=\"doubleCompare-endNote_" +id);
                content = content.replace("<span class=\"leos-authnote " + CONTENT_SOFT_ADDED_CLASS + "\" id=\"endNote_" +id,"<span class=\"" + CONTENT_SOFT_REMOVED_CLASS + " leos-authnote\" id=\"endNote_" +id);
                content = content.replace("<span class=\"leos-authnote " + CONTENT_SOFT_ADDED_CLASS + "\" id=\"doubleCompare-endNote_" +id,"<span class=\"" + CONTENT_SOFT_REMOVED_CLASS+ " leos-authnote\" id=\"doubleCompare-endNote_" +id);
            }
        }

        return content;
    }

    /**
     * LEOS-5763
     * Adjust softactionroot false on the diffing result
     * - if subparagraph check previous (not the num)
     * - if previous without softactionroot true, update false to true on softactionroot
     * @param xmlContent
     * @return diffing result adjusted
     * TODO generic function to check parent of child nodes instead of only SUBPARAGRAPH
     */
    public String adjustSoftRootSubParagraph(String xmlContent) {
        Document document = createXercesDocument(xmlContent.getBytes(UTF_8));
        NodeList elements = document.getElementsByTagName(SUBPARAGRAPH);
        List<org.w3c.dom.Node> nodeList = XercesUtils.getNodesAsList(elements);

        if (nodeList.size() > 0) {
            for (int nodeIter = 0; nodeIter < nodeList.size(); nodeIter++) {
                final org.w3c.dom.Node node = nodeList.get(nodeIter);
                org.w3c.dom.Node previous = XercesUtils.getPrevSibling(node);
                if(previous != null && previous.getNodeName().equals(NUM)) {
                    previous = XercesUtils.getPrevSibling(previous);
                }
                org.w3c.dom.Node softActionRootAttr = node.getAttributes().getNamedItem(LEOS_SOFT_ACTION_ROOT_ATTR);
                if (softActionRootAttr != null) {
                    if(softActionRootAttr.getNodeValue().equals("false")) {
                        if (previous != null) {
                            org.w3c.dom.Node softActionRootAttrPrevious = previous.getAttributes().getNamedItem(LEOS_SOFT_ACTION_ROOT_ATTR);
                            if (softActionRootAttrPrevious == null) {
                                XercesUtils.insertOrUpdateAttributeValue(node, LEOS_SOFT_ACTION_ROOT_ATTR, "true");
                            }
                        }
                    }
                }
            }
        }
        return new String(nodeToByteArray(document), UTF_8);
    }

    private String adjustMovedFootnotes(String content) {

        String markerMovedAuthNotes = "<span class=\"leos-authnote " + CONTENT_SOFT_ADDED_CLASS + "\" id=\"endNote_moved";
        String markerAuthNoteTable = "<span class=\"leos-authnote-table\" id=\"leos-authnote-table-id\">";
        String closingSpan = "</span>";

        //LEOS-5168 replace for version compare
        content = content.replace("<span class=\"leos-authnote leos-content-new-cn\" id=\"endNote_moved", "<span class=\"leos-authnote " + CONTENT_REMOVED_CLASS_CN + "\" id=\"endNote_moved");
        content = content.replace("<span class=\"leos-authnote\" id=\"doubleCompare-endNote_moved", "<span class=\"leos-authnote " + CONTENT_REMOVED_CLASS_CN + "\" id=\"doubleCompare-endNote_moved");

        if(!content.contains(markerMovedAuthNotes) ) {
            return content;
        }

        int tableEndIdx = content.lastIndexOf(closingSpan);
        int tableStartIdx = content.indexOf(markerAuthNoteTable);
        String authNoteContent = content.substring(tableStartIdx, tableEndIdx);

        while(authNoteContent.contains(markerMovedAuthNotes)) {

            int movedNoteStartIndex = authNoteContent.indexOf(markerMovedAuthNotes);
            String movedFootNote = authNoteContent.substring(movedNoteStartIndex, authNoteContent.length());
            int idxEndOfAuthNote = StringUtils.ordinalIndexOf(movedFootNote, closingSpan, 3);
            idxEndOfAuthNote += 7; //7 is the length of the </span>
            movedFootNote = movedFootNote.substring(0, idxEndOfAuthNote);

            String endingAuthNotes = authNoteContent.substring(movedNoteStartIndex + movedFootNote.length(), authNoteContent.length());
            movedFootNote = movedFootNote.replaceAll(CONTENT_SOFT_ADDED_CLASS, CONTENT_SOFT_REMOVED_CLASS);

            //Put it back together ( startAuthNotes + changed authNote + the rest of the authNotes)
            String beginningAuthNotes = authNoteContent.substring(0, movedNoteStartIndex);
            beginningAuthNotes = beginningAuthNotes + movedFootNote;
            authNoteContent = beginningAuthNotes + endingAuthNotes;
        }

        String beginningContent = content.substring(0, tableStartIdx);
        String endingContent = content.substring(tableEndIdx, content.length());
        content = beginningContent + authNoteContent + endingContent;

        return content;
    }

    private String replaceMarkers(String content, String marker, String id, String origin) {

        if(!id.equals(origin)) {
            origin = "leos:origin=\"" + origin + "\"";
        } else {
            origin = "";
        }
        String search = "id=\"" + id + "\" " + origin + " marker=\"" + marker +"\"";
        String replace =  "id=\"" + id + "\" " + origin + " marker=\"" +"\"";
        content = content.replaceFirst(search, replace);

        search = "id=\"doubleCompare-" + id + "\" " + origin + " marker=\"doubleCompare-" + id +"\"";
        replace =  "id=\"doubleCompare-" + id + "\" " + origin + " marker=\"" +"\"";
        content = content.replaceFirst(search, replace);
        return content;
    }

    /**
     * Add style to authorial note when deleting an element with
     * @param content
     * @return auhtorial note with correct style
     */
    public String adjustDeleteAuthorialNotes(String content) {
        AuthorialNoteProcessor authorialNoteProcessor = new AuthorialNoteProcessor();
        List<AuthorialNoteProcessor.AuthorialNote> authorialNotes = authorialNoteProcessor.getAuthorialNotes(content);

        for (AuthorialNoteProcessor.AuthorialNote note : authorialNotes ) {
            String id = note.getXmlId();
            content = content.replace("<span id=\"endNote_" + id + "\" class=\"leos-authnote", "<span id=\"endNote_" + id + "\" class=\"leos-authnote" + " " + CONTENT_SOFT_REMOVED_CLASS);
        }

        return content;
    }

    public ByteArrayBuilder softDeleteAuthorialNote(ByteArrayBuilder content) {
        String xmlContent = new String(content.getContent(), UTF_8);
        xmlContent = xmlContent.replace("<authorialNote " , "<authorialNote class=\"" + CONTENT_SOFT_REMOVED_CLASS +"\" ");
        xmlContent = xmlContent.replace(" class=\"" + CONTENT_SOFT_REMOVED_CLASS +"\" class=\"" + CONTENT_SOFT_REMOVED_CLASS + "\" " , " class=\"" + CONTENT_SOFT_REMOVED_CLASS +"\" ");
        return new ByteArrayBuilder(xmlContent.getBytes());
    }

    public ByteArrayBuilder softUndeleteAuthorialNote(ByteArrayBuilder content) {
        String xmlContent = new String(content.getContent(), UTF_8);
        xmlContent = xmlContent.replace("<authorialNote class=\"" + CONTENT_SOFT_REMOVED_CLASS +"\" ", "<authorialNote ");
        return new ByteArrayBuilder(xmlContent.getBytes());
    }

    public String softDeleteAuthorialNote(String xmlContent) {
        xmlContent = xmlContent.replace("<authorialNote " , "<authorialNote class=\"" + CONTENT_SOFT_REMOVED_CLASS +"\" ");
        xmlContent = xmlContent.replace(" class=\"" + CONTENT_SOFT_REMOVED_CLASS +"\" class=\"" + CONTENT_SOFT_REMOVED_CLASS + "\" " , " class=\"" + CONTENT_SOFT_REMOVED_CLASS +"\" ");
        return xmlContent;
    }

    public String processDuplicatedXmlIds(String diffResult) {
        Set<String> xmlIds = new HashSet<>();
        Pattern patternXmlIds = Pattern.compile("xml:id=\"([^\"]*)\"");
        Matcher matcherXmlIds = patternXmlIds.matcher(diffResult);

        while(matcherXmlIds.find()) {
            if(xmlIds.contains(matcherXmlIds.group(1))) {
                diffResult = prefixToXmlIds(diffResult, matcherXmlIds, "");
            } else {
                xmlIds.add(matcherXmlIds.group(1));
            }
        }
        return diffResult;
    }

    public String processRevisedHtml(String diffResult, ContentComparatorContext context) {
        List<Node> contentNodes = new LinkedList<>(Parser.parseXmlFragment(diffResult, ""));
        contentNodes.removeIf(node -> isEmptyNode(node));
        Integer sizeContentNodes = contentNodes.size();
        List<Node> processedContentNodes = processNodesIdenticalContentOriginalCurrent(contentNodes);
        if(sizeContentNodes > processedContentNodes.size()) {
            //identicalContentOriginalCurrent = true;
        }
        return processChildElements(contentNodes, context);
    }
    
	private boolean isEmptyNode(Node node) {
		if (!node.nodeName().equals(DELETE) && !node.nodeName().equals(INSERT))
			return false;

		if (node.getClass().isAssignableFrom(TextNode.class))
			return StringUtils.isEmpty(((TextNode) node).text());

		if (node.getClass().isAssignableFrom(Element.class)) {
			StringBuilder result = new StringBuilder();
			node.childNodes().forEach(childNode -> {
				if (childNode.getClass().isAssignableFrom(Element.class)) {
					result.append(childNode.outerHtml());
				} else {
					result.append(((TextNode) childNode).text());
				}
			});
			return StringUtils.isEmpty(result.toString());
		}

		return false;
	}

    private String processChildElements(List<Node> elements, ContentComparatorContext context) {
        return elements.stream().map(node -> {
            StringBuilder result = new StringBuilder();
            if (node.getClass().isAssignableFrom(Element.class)) {
                node.childNodes().forEach(childNode -> {
                    if(childNode.getClass().isAssignableFrom(Element.class)) {
                        result.append(childNode.outerHtml());
                    } else {
                        TextNode textNode = (TextNode) childNode;
                        result.append(textNode.text());
                    }
                });
                switch (node.nodeName()) {
                    case INSERT:
                        return prepareResultString(node, result.toString(), context, true);
                    case DELETE:
                        //add prefix for deleted inline elements to avoid duplicate ids in Docuwrite export
                        String processedResult = processDeletedXmlIds(result.toString());
                        return prepareResultString(node, processedResult, context, false);
                    default: // some other html tag
                        return node.outerHtml();
                }
            } else {
                return ((TextNode) node).text();
            }
        }).collect(Collectors.joining());
    }

    private String prefixToXmlIds(String diffResult, Matcher matcherXmlIds, String prefix) {
        String[] result = diffResult.split("xml:id=\"" + matcherXmlIds.group(1) + "\"");
        for (int count = 0; count < (result.length - 1); count++) {
            result[count] += "xml:id=\"" + prefix + matcherXmlIds.group(1);
            if (count > 0) {
                result[count] += "_" + count;
            }
            result[count] += "\"";
        }
        diffResult = String.join("", result);
        return diffResult;
    }

    private List<Node> processNodesIdenticalContentOriginalCurrent(List<Node> contentNodes){
        List<String> deletedRevisedText = new ArrayList<>();
        for(int nodeId = 0; nodeId < contentNodes.size(); nodeId++) {
            Node node = contentNodes.get(nodeId);
            //LEOS-4699 GSC-598 if entire text old(original) and new(current) not identical, keep deleted revised text in list
            if(node.nodeName().equals(DELETE) && node.attr(CLASS_ATTR) != null && node.attr(CLASS_ATTR).equals(REVISED)) {
                if( node.childNodeSize() > 0) {
                    deletedRevisedText.add(node.childNode(0).toString().trim());
                }
            }
            //LEOS-4699 GSC-598 if entire text old(original) and new(current) not identical, compare insert verification text to deleted revised text
            //if exist then remember it in variable identicalContentOriginalCurrent and remove previous node which contains the deleted revised text
            if(node.nodeName().equals(INSERT) && node.attr(CLASS_ATTR) != null && node.attr(CLASS_ATTR).equals(VERIFICATION)) {
                if( node.childNodeSize() > 0) {
                    String nodeChildText = node.childNode(0).toString();
                    if(deletedRevisedText.contains(nodeChildText.trim())) {
                        contentNodes.remove(nodeId-1);
                    }
                }
            }
        }
        return contentNodes;
    }

    private String processDeletedXmlIds(String diffResult) {
        Pattern patternXmlIds = Pattern.compile("xml:id=\"([^\"]*)\"");
        Matcher matcherXmlIds = patternXmlIds.matcher(diffResult);

        while (matcherXmlIds.find()) {
            diffResult = prefixToXmlIds(diffResult, matcherXmlIds, SOFT_DELETE_PLACEHOLDER_ID_PREFIX);
        }
        return diffResult;
    }

    private String prepareResultString(Node node, String text, ContentComparatorContext context, boolean isInsert) {
        String className = "";
        if(context.getThreeWayDiff()) {
            String attrVal = node.attr(CLASS_ATTR);
            if(!StringUtils.isEmpty(attrVal)) {
                switch(attrVal) {
                    case REVISED:
                        className = isInsert ? context.getAddedOriginalValue() : context.getRemovedOriginalValue();
                        break;
                    case VERIFICATION:
                        className = isInsert ? context.getAddedIntermediateValue() : context.getRemovedIntermediateValue();
                        break;
                    default:
                        className = "";
                }
            }
        } else {
            className = isInsert ? context.getAddedValue() : context.getRemovedValue();
        }

        //LEOS-4699 GSC-598 if docuwrite and added intermediate and identical text between old and new, add u html to remove bold
        if (className.equals(context.getAddedIntermediateValue()) && context.getIsDocuwriteExport() && identicalContentOriginalCurrent) {
            return "<u>" + text + "</u>";
        }
        //LEOS-4699 GSC-598 if added intermediate and identical text between old and new, add style to span to remove bold
        else if (className.equals(context.getAddedIntermediateValue()) && !context.getIsDocuwriteExport() && identicalContentOriginalCurrent) {
            return getOpeningTag(context.getAttrName(), context.getRetainOriginalValue()) + text + getClosingTag();
        } else {
            return getOpeningTag(context.getAttrName(), className) + text + getClosingTag();
        }
    }

    /**
     * get Spans in String with specific class
     * @param content
     * @param removedClass
     * @return list of spans
     */
    private List<String> getRemovedSpansWithClass(String content, String removedClass) {

        List<String> removedTextList = new ArrayList<>();
        Pattern patternSpans = Pattern.compile("<span class=\"" + removedClass + "\">(.*?)<\\/span>");
        Matcher matcherSpans = patternSpans.matcher(content);

        while(matcherSpans.find()) {
            String removedText = matcherSpans.group(1);
            removedTextList.add(removedText);
        }
        return removedTextList;
    }

}