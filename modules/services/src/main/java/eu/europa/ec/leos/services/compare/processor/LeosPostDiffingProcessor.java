package eu.europa.ec.leos.services.compare.processor;

import eu.europa.ec.leos.services.support.XercesUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

public class LeosPostDiffingProcessor {

    private final String CLASS_ATTR = "class";

    private Set<String> xmlIds;

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

    /**
     * LEOS-6162
     * Adjust diffing result for export
     * @param xmlContent
     * @param classRemoved
     * @param classAdded
     * @return diffing result adjusted
     */
    public String adjustDeletedElements(String xmlContent, String classRemoved, String classAdded) {
        Document document = createXercesDocument(xmlContent.getBytes(UTF_8));
        NodeList elements = document.getElementsByTagName("span");
        List<org.w3c.dom.Node> nodeList = XercesUtils.getNodesAsList(elements);

        if (nodeList.size() > 0) {
            for (int nodeIter = 0; nodeIter < nodeList.size(); nodeIter++) {
                final org.w3c.dom.Node node = nodeList.get(nodeIter);
                if(node.getAttributes().getNamedItem(CLASS_ATTR).getTextContent().equals(classAdded)) {
                    String textNode = node.getTextContent();
                    String parentId = XercesUtils.getParentId(node);
                    if(parentId != null && parentId.startsWith(SOFT_DELETE_PLACEHOLDER_ID_PREFIX)) {
                        xmlContent = xmlContent.replace("<span class=\"" + classAdded + "\">" + textNode, "<span class=\"" + classAdded + "\"><span class=\"" + classRemoved + "\">" + textNode + "</span>");
                    }
                }
            }
        }
        return xmlContent;
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

}