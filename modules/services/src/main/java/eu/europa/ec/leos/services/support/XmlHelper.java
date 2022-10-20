/*
 * Copyright 2019 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.services.support;

import com.google.common.collect.ImmutableMap;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.vo.toc.OptionsType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItem;
import io.atlassian.fugue.Pair;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlHelper {
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final String OPEN_TAG = "<";
    public static final String CLOSE_TAG = ">";
    public static final String OPEN_END_TAG = "</";
    public static final String CLOSE_END_TAG = "/>";
    public static final String XML_DOC_EXT = ".xml";

    public static final String MARKER_ATTRIBUTE = "marker";

    public static final String AKNBODY = "aknbody";
    public static final String DOC = "doc";
    public static final String BILL = "bill";
    public static final String MEMORANDUM = "memorandum";
    public static final String PROPOSAL = "proposal";
    public static final String AKOMANTOSO = "akomaNtoso";
    public static final String META = "meta";
    public static final String BLOCKCONTAINER = "blockContainer";
    public static final String AUTHORIAL_NOTE = "authorialNote";
    public static final String MATHJAX = "mathjax";
    public static final String MREF = "mref";
    public static final String REF = "ref";
    public static final String HREF = "href";
    public static final String BOLD = "b";
    public static final String ITALICS = "i";
    public static final String UNDERLINE = "u";
    public static final String SUP = "sup";
    public static final String SUB = "sub";
    public static final String INLINE = "inline";
    public static final String FORMULA = "formula";
    public static final String INTRO = "intro";
    public static final String HEADING = "heading";
    public static final String NUM = "num";
    public static final String P = "p";
    public static final String COVERPAGE = "coverPage";

    public static final String EC = "ec";
    public static final String CN = "cn";
    public static final String LS = "ls";

    public static final String PREFACE = "preface";
    public static final String PREAMBLE = "preamble";
    public static final String CITATIONS = "citations";
    public static final String CITATION = "citation";
    public static final String RECITALS = "recitals";
    public static final String RECITAL = "recital";
    public static final String BODY = "body";
    public static final String PART = "part";
    public static final String TITLE = "title";
    public static final String DIVISION = "division";
    public static final String CHAPTER = "chapter";
    public static final String SECTION = "section";
    public static final String ARTICLE = "article";
    public static final String PARAGRAPH = "paragraph";
    public static final String SUBPARAGRAPH = "subparagraph";
    public static final String LIST = "list";
    public static final String POINT = "point";
    public static final String INDENT = "indent";
    public static final String SUBPOINT = "alinea";
    public static final String SUBPOINT_LABEL = "subparagraph";
    public static final String CLAUSE = "clause";
    public static final String CONCLUSIONS = "conclusions";
    public static final String MAIN_BODY = "mainBody";
    public static final String TBLOCK = "tblock";
    public static final String LEVEL = "level";
    public static final String CONTENT = "content";
    public static final String CROSSHEADING = "crossHeading";
    public static final String BLOCK = "block";
    public static final String EXPL_COUNCIL = "EXPL_COUNCIL";

    public static final String ID = "id";
    public static final String XMLID = "xml:id";
    public static final String LEOS_REF = "leos:ref";
    public static final String INLINE_NUM = "crossHnum";
    public static final String INDENT_LEVEL_PROPERTY = "--indent-level";
    public static final String INLINE_NUM_PROPERTY = "--inline-num";
    public static final String STYLE = "style";
    public static final String WHITESPACE = " ";

    public static final String LEOS_ORIGIN_ATTR = "leos:origin";
    public static final String LEOS_DELETABLE_ATTR = "leos:deletable";
    public static final String LEOS_EDITABLE_ATTR = "leos:editable";
    public static final String LEOS_AFFECTED_ATTR = "leos:affected";
    public static final String LEOS_CROSS_HEADING_BLOCK_NAME = "leos:name";
    public static final String LEOS_REF_BROKEN_ATTR = "leos:broken";
    public static final String LEOS_DEPTH_ATTR = "leos:depth";
    public static final String LEOS_LIST_TYPE_ATTR = "leos:list-type";
    public static final String LEOS_CROSSHEADING_TYPE = "leos:crossheading-type";

    public static final String LEOS_SOFT_ACTION_ATTR = "leos:softaction";
    public static final String LEOS_SOFT_ACTION_ROOT_ATTR = "leos:softactionroot";
    public static final String LEOS_SOFT_MOVED_LABEL_ATTR = "leos:softmove_label";
    public static final String LEOS_SOFT_USER_ATTR = "leos:softuser";
    public static final String LEOS_SOFT_DATE_ATTR = "leos:softdate";
    public static final String MOVE_TO = "move_to";
    public static final String MOVE_FROM = "move_from";
    public static final String LEOS_SOFT_MOVE_TO = "leos:softmove_to";
    public static final String LEOS_SOFT_MOVE_FROM = "leos:softmove_from";
    public static final String LEOS_SOFT_TRANS_FROM = "leos:softtrans_from";
    public static final String SOFT_MOVE_PLACEHOLDER_ID_PREFIX = "moved_";
    public static final String SOFT_DELETE_PLACEHOLDER_ID_PREFIX = "deleted_";
    public static final String SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX = "transformed_";
    public static final String TOGGLED_TO_NUM = "toggled_to_num";
    public static final String BACK_TO_NUM_FROM_SOFT_DELETED = "back_to_num_from_soft_deleted";
    public static final String STATUS_IGNORED_ATTR = "status";
    public static final String STATUS_IGNORED_ATTR_VALUE = "ignored";

    public static final String CLONED_PROPOSAL_REF = "clonedProposalRef";
    public static final String CLONED_TARGET_USER = "targetUser";
    public static final String CLONED_CREATION_DATE = "creationDate";
    public static final String CLONED_STATUS = "status";
    public static final String LEOS_MERGE_ACTION_ATTR = "leos:mergeAction";

    public static final String LEOS_INDENT_LEVEL_ATTR = "leos:indent-level";
    public static final String LEOS_INDENT_NUMBERED_ATTR = "leos:indent-numbered";
    public static final String LEOS_INDENT_ORIGIN_TYPE_ATTR = "leos:indent-origin-type";
    public static final String LEOS_INDENT_ORIGIN_INDENT_LEVEL_ATTR = "leos:indent-origin-indent-level";
    public static final String LEOS_INDENT_ORIGIN_NUM_ID_ATTR = "leos:indent-origin-num-id";
    public static final String LEOS_INDENT_ORIGIN_NUM_ATTR = "leos:indent-origin-num";
    public static final String LEOS_INDENT_ORIGIN_NUM_ORIGIN_ATTR = "leos:indent-origin-num-origin";
    public static final String LEOS_INDENT_UNUMBERED_PARAGRAPH = "leos:indent-unumbered-paragraph";
    public static final String LEOS_AUTO_NUM_OVERWRITE = "leos:auto-num-overwrite";

    public static final String EMPTY_STRING = "";
    public static final String NON_BREAKING_SPACE = "\u00A0";
    public static final String CLASS_ATTR = "class";

    private static final String ID_PLACEHOLDER = "${id}";
    private static final String ID_PLACEHOLDER_ESCAPED = "\\Q${id}\\E";
    private static final String NUM_PLACEHOLDER = "${num}";
    private static final String NUM_PLACEHOLDER_ESCAPED = "\\Q${num}\\E";
    private static final String HEADING_PLACEHOLDER = "${heading}";
    private static final String HEADING_PLACEHOLDER_ESCAPED = "\\Q${heading}\\E";
    private static final String CONTENT_TEXT_PLACEHOLDER = "${default.content.text}";
    private static final String CONTENT_TEXT_PLACEHOLDER_ESCAPED = "\\Q${default.content.text}\\E";

    public static final String LEVEL_NUM_SEPARATOR = ".";
    public static final String DOC_FILE_NAME_SEPARATOR = "-";

    public static final List<String> ELEMENTS_IN_TOC = Arrays.asList(PART, TITLE, CHAPTER, SECTION,
                                                                    ARTICLE, PARAGRAPH, SUBPARAGRAPH, POINT, SUBPOINT, INDENT, LIST,
                                                                    CITATION, RECITAL,
                                                                    LEVEL, CROSSHEADING, DIVISION);
    public static final List<String> ELEMENTS_TO_BE_PROCESSED_FOR_NUMBERING = Arrays.asList(ARTICLE, PARAGRAPH, SUBPARAGRAPH, POINT, SUBPOINT, INDENT, LEVEL);
    public static final List<String> ELEMENTS_TO_BE_NUMBERED = Arrays.asList(ARTICLE, PARAGRAPH, POINT, LEVEL);
    public static final List<String> POINT_ROOT_PARENT_ELEMENTS = Arrays.asList(ARTICLE, LEVEL);
    public static final List<String> INLINE_ELEMENTS = Arrays.asList(AUTHORIAL_NOTE, MATHJAX, MREF, REF, BOLD, ITALICS, UNDERLINE, SUP, SUB, INLINE);
    private static final List<String> ELEMENTS_TO_REMOVE_FROM_CONTENT = Arrays.asList(INLINE, AUTHORIAL_NOTE);
    public static final List<String> ELEMENTS_TO_HIDE_CONTENT = Arrays.asList(PREFACE, PREAMBLE, CITATIONS, RECITALS, BODY, MAIN_BODY);
    public static final List<String> ELEMENTS_WITH_TEXT = Arrays.asList(CROSSHEADING.toLowerCase(), BLOCK);
    public static final List<String> SOFT_ACTIONS_PREFIXES = Arrays.asList(SOFT_TRANSFORM_PLACEHOLDER_ID_PREFIX, SOFT_MOVE_PLACEHOLDER_ID_PREFIX, SOFT_DELETE_PLACEHOLDER_ID_PREFIX);

    public static final String XML_NAME = "name";
    public static final String XML_SHOW_AS = "showAs";

    public static final String ANNEX = "Annex";
    public static final String ANNEX_FILE_PREFIX = "ANNEX";
    public static final String REG_FILE_PREFIX = "REG";
    public static final String DIR_FILE_PREFIX = "DIR";
    public static final String DEC_FILE_PREFIX = "DEC";
    public static final String MEMORANDUM_FILE_PREFIX = "EXPL_MEMORANDUM";
    public static final String PROPOSAL_FILE = "main";
    public static final String PROP_ACT = "PROP_ACT";
    public static final String COUNCIL_EXPLANATORY = "EXPL_COUNCIL";

    public static String extractContentFromTocItem(TableOfContentItemVO tocItem) {
        if (tocItem.getContent() == null) {
            return "";
        }
        Pair<Integer, Integer> indexes = getStartEndIndexesOfContent(tocItem);

        return tocItem.getContent().substring(indexes.left(), indexes.right());
    }

    public static String removeXmlTags(String content) {
        return content.replaceAll("<[^>]+>", "");
    }

    public static boolean containsXmlTags(String content) {
       return content.contains(OPEN_TAG) || content.contains(CLOSE_TAG);
    }

    private static Pair<Integer, Integer> getStartEndIndexesOfContent(TableOfContentItemVO tocItem) {
        String content = tocItem.getContent();
        boolean containTagContent = content.contains(OPEN_TAG + CONTENT) && content.contains(OPEN_END_TAG + CONTENT + CLOSE_TAG);
        String tagName = tocItem.getTocItem().getAknTag().value();
        boolean containTagName = content.contains(OPEN_TAG + tagName.toLowerCase()) && content.contains(OPEN_END_TAG + tagName.toLowerCase(Locale.ROOT) + CLOSE_TAG);
        if (!containTagName) {
            containTagName = content.contains(OPEN_TAG + tagName) && content.contains(OPEN_END_TAG + tagName + CLOSE_TAG);
        } else {
            tagName = tagName.toLowerCase();
        }
        int startIndexContent = 0;
        int endIndexContent = content.length();
        if (containTagContent) {
            startIndexContent = content.indexOf(CLOSE_TAG, content.indexOf(OPEN_TAG + CONTENT)) + 1;
            endIndexContent = content.indexOf(OPEN_END_TAG + CONTENT + CLOSE_TAG, startIndexContent);
        } else if (containTagName) {
            startIndexContent = content.indexOf(CLOSE_TAG, content.indexOf(OPEN_TAG + tagName)) + 1;
            endIndexContent = content.indexOf(OPEN_END_TAG + tagName + CLOSE_TAG, startIndexContent);
        }

        String tmpContent = content.substring(startIndexContent, endIndexContent);
        boolean containP = tmpContent.contains(OPEN_TAG + P)
                && tmpContent.contains(OPEN_END_TAG + P + CLOSE_TAG);
        if (containP) {
            int startPTag = content.indexOf(OPEN_TAG + P, startIndexContent) + 1;
            startIndexContent = content.indexOf(CLOSE_TAG, startPTag) + 1;
            endIndexContent = content.indexOf(OPEN_END_TAG + P + CLOSE_TAG, startIndexContent);
        }
        boolean containInline = tmpContent.startsWith(OPEN_TAG + INLINE) && tmpContent.contains(XML_NAME.concat("=\"").concat(INLINE_NUM).concat("\""));
        if (containInline) {
            int startInlineTag = content.indexOf(OPEN_TAG + INLINE, startIndexContent) + 1;
            int indexEndOfInlineTag = content.indexOf(OPEN_END_TAG + INLINE + CLOSE_TAG, startInlineTag);
            if (indexEndOfInlineTag == -1) {
                startIndexContent = content.indexOf(CLOSE_END_TAG, startIndexContent) +  CLOSE_END_TAG.length();
            } else {
                startIndexContent = content.indexOf(OPEN_END_TAG + INLINE + CLOSE_TAG, startIndexContent) + new String(OPEN_END_TAG + INLINE + CLOSE_TAG).length();
            }
        }
        return new Pair<>(startIndexContent, endIndexContent);
    }

    public static String getTemplate(String tagName) {
        return "<" + tagName + " xml:id=\"" + IdGenerator.generateId(tagName.substring(0, 3), 7) + "\"></" + tagName + ">";
    }

    public static String getTemplate(TocItem tocItem, MessageHelper messageHelper) {
        return getTemplate(tocItem, ImmutableMap.of(NUM, Collections.emptyMap(), HEADING, Collections.emptyMap(),
                CONTENT, Collections.singletonMap(CONTENT_TEXT_PLACEHOLDER_ESCAPED, getDefaultContentText(tocItem.getAknTag().value(), messageHelper))));
    }

    public static String getTemplate(TocItem tocItem, String num, MessageHelper messageHelper) {
        return getTemplate(tocItem, ImmutableMap.of(NUM, Collections.singletonMap(NUM_PLACEHOLDER_ESCAPED, StringUtils.isNotEmpty(num) && tocItem.isNumWithType() ? StringUtils.capitalize(tocItem.getAknTag().value()) + " " + num : num),
                HEADING, Collections.singletonMap(HEADING_PLACEHOLDER_ESCAPED, StringUtils.EMPTY), CONTENT, Collections.singletonMap(CONTENT_TEXT_PLACEHOLDER_ESCAPED, getDefaultContentText(tocItem.getAknTag().value(), messageHelper))));
    }

    public static String getTemplate(TocItem tocItem, String num, String heading, MessageHelper messageHelper) {
        return getTemplate(tocItem, ImmutableMap.of(NUM, Collections.singletonMap(NUM_PLACEHOLDER_ESCAPED, StringUtils.isNotEmpty(num) && tocItem.isNumWithType() ? StringUtils.capitalize(tocItem.getAknTag().value()) + " " + num : num),
                HEADING, Collections.singletonMap(HEADING_PLACEHOLDER_ESCAPED, heading), CONTENT, Collections.singletonMap(CONTENT_TEXT_PLACEHOLDER_ESCAPED, getDefaultContentText(tocItem.getAknTag().value(), messageHelper))));
    }

    private static String getTemplate(TocItem tocItem, Map<String, Map<String, String>> templateItems) {
        StringBuilder template = tocItem.getTemplate() != null ? new StringBuilder(tocItem.getTemplate()) : getDefaultTemplate(tocItem);
        replaceAll(template, ID_PLACEHOLDER_ESCAPED, IdGenerator.generateId("akn_" + tocItem.getAknTag().value(), 7));

        replaceTemplateItems(template, NUM, tocItem.getItemNumber(), templateItems.get(NUM));
        replaceTemplateItems(template, HEADING, tocItem.getItemHeading(), templateItems.get(HEADING));
        replaceTemplateItems(template, CONTENT, OptionsType.MANDATORY, templateItems.get(CONTENT));

        return template.toString();
    }

    private static StringBuilder getDefaultTemplate(TocItem tocItem) {
        StringBuilder defaultTemplate = new StringBuilder("<" + tocItem.getAknTag().value() + " xml:id=\"" + ID_PLACEHOLDER + "\">");
        if (OptionsType.MANDATORY.equals(tocItem.getItemNumber()) || OptionsType.OPTIONAL.equals(tocItem.getItemNumber())) {
            defaultTemplate.append(tocItem.isNumberEditable() ? "<num>" + NUM_PLACEHOLDER + "</num>" : "<num leos:editable=\"false\">" + NUM_PLACEHOLDER + "</num>");
        }
        if (OptionsType.MANDATORY.equals(tocItem.getItemHeading()) || OptionsType.OPTIONAL.equals(tocItem.getItemHeading())) {
            defaultTemplate.append("<heading>" + HEADING_PLACEHOLDER + "</heading>");
        }
        defaultTemplate.append("<content><p>" + CONTENT_TEXT_PLACEHOLDER + "</p></content></" + tocItem.getAknTag().value() + ">");
        return defaultTemplate;
    }

    private static void replaceTemplateItems(StringBuilder template, String itemName, OptionsType itemOption, Map<String, String> templateItem) {
        if (OptionsType.MANDATORY.equals(itemOption)) {
            templateItem.forEach((itemPlaceHolder, itemValue) -> {
                replaceAll(template, itemPlaceHolder, StringUtils.isEmpty(itemValue) ? "" : itemValue);
            });
        } else if (OptionsType.OPTIONAL.equals(itemOption)) {
            templateItem.forEach((itemPlaceHolder, itemValue) -> {
                if (StringUtils.isEmpty(itemValue)) {
                    replaceAll(template, "<" + itemName + ".*?" + itemPlaceHolder + "</" + itemName + ">", "");
                } else {
                    replaceAll(template, itemPlaceHolder, itemValue);
                }
            });
        }
    }

    public static void replaceAll(StringBuilder sb, String toReplace, String replacement) {
        int start = 0;
        Matcher m = Pattern.compile(toReplace).matcher(sb);
        while (m.find(start)) {
            sb.replace(m.start(), m.end(), replacement);
            start = m.start() + replacement.length();
        }
    }

    private static String getDefaultContentText(String tocTagName, MessageHelper messageHelper) {
        String defaultTextContent = messageHelper.getMessage("toc.item.template." + tocTagName + ".content.text");
        if (defaultTextContent.equals("toc.item.template." + tocTagName + ".content.text")) {
            defaultTextContent = messageHelper.getMessage("toc.item.template.default.content.text");
        }
        return defaultTextContent;
    }

    private static final ArrayList<String> prefixTobeUsedForChildren = new ArrayList<String>(Arrays.asList(ARTICLE, RECITALS, CITATIONS));

    public static String determinePrefixForChildren(String tagName, String idOfNode, String parentPrefix) {
        return prefixTobeUsedForChildren.contains(tagName) ? idOfNode : parentPrefix;  //if(root Node Name is in Article/Reictals/Citations..set the prefix)
    }

    private static final ArrayList<String> nodeToSkip = new ArrayList<String>(Arrays.asList(META));

    public static boolean skipNodeAndChildren(String tagName) {
        return nodeToSkip.contains(tagName) ? true : false;
    }

    private static final ArrayList<String> tagNamesToSkip = new ArrayList<String>(Arrays.asList(AKOMANTOSO, BILL, "documentCollection", "doc", "attachments"));

    public static boolean skipNodeOnly(String tagName) {
        return tagNamesToSkip.contains(tagName) ? true : false;
    }

    private static final ArrayList<String> parentEditableNodes = new ArrayList<String>(Arrays.asList(ARTICLE, RECITALS, CITATIONS, BLOCKCONTAINER));

    public static boolean isParentEditableNode(String tagName) {
        return parentEditableNodes.contains(tagName) ? true : false;
    }

    private static final ArrayList<String> exclusionList = new ArrayList<String>(Arrays.asList(AUTHORIAL_NOTE, NUM, CLAUSE));

    public static boolean isExcludedNode(String tagName) {
        return exclusionList.contains(tagName) ? true : false;
    }

    public static EditableAttributeValue getEditableAttribute(String tagName, String attrVal) {
        if (isExcludedNode(tagName)) {
            return EditableAttributeValue.FALSE; // editable = false;
        } else if (attrVal != null) {
            return attrVal.equalsIgnoreCase("true") ? EditableAttributeValue.TRUE : EditableAttributeValue.FALSE;
        } else if (isParentEditableNode(tagName)) {
            return EditableAttributeValue.FALSE; // editable = false;
        } else {
            return EditableAttributeValue.UNDEFINED; // editable not present;
        }
    }

    public static String getDateAsXml() {
        return getDateAsXml(new GregorianCalendar());
    }

    public static String getDateAsXml(GregorianCalendar calendar) {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar).toXMLFormat();
        } catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Cannot instantiate new XMLGregorianCalendar");
        }
    }

    public static String getTrimmedXmlId(String content) {
        //TODO: Make it generic to trim extra spaces for all the attributes inside a tag
        //look for extra spaces after the xml:id attributes (<authorialnote xml:id="authNote_1"  marker="1"><p xml:id="authNote_1_p" >Footnote</p></authorialnote>)
        Pattern patternXmlIds = Pattern.compile("xml:id=\"([^\"]*)\"\\s(\\s|>)");
        Matcher matcherXmlIds = patternXmlIds.matcher(content);
        while (matcherXmlIds.find()) {
            String[] result = content.split(matcherXmlIds.group());
            result[0] += matcherXmlIds.group().replaceFirst("\\s", "");
            content = String.join("", result);
        }
        return content;
    }

    public static byte[] updateSoftTransFromAttribute(byte[] tag, String newValue) {
        StringBuilder tagStr = new StringBuilder(new String(tag, UTF_8));
        insertOrUpdateAttributeValue(tagStr, LEOS_SOFT_TRANS_FROM, newValue);
        return tagStr.toString().getBytes(UTF_8);
    }

    public static String insertAttribute(String attrTag, Object attrVal) {
        return attrVal != null ? (" ").concat(attrTag).concat("=\"").concat(attrVal.toString()).concat("\"") : EMPTY_STRING;
    }

    public static StringBuilder insertOrUpdateAttributeValue(StringBuilder tagStr, String attrName, Object attrValue) {
        if (tagStr != null && attrName != null) {
            int attributePosition = tagStr.substring(0, tagStr.indexOf(">") + 1).indexOf(attrName);
            if (attributePosition != -1) {
                int attrValStartPos = tagStr.indexOf("=", attributePosition) + 2;
                int attrValEndPos = tagStr.indexOf("\"", attrValStartPos);
                if (attrName.equalsIgnoreCase(CLASS_ATTR)) {
                    if (tagStr.indexOf((String) attrValue, attrValStartPos) == -1) {
                        tagStr.insert(attrValEndPos, " ".concat((String) attrValue), 0, ((String) attrValue).length() + 1);
                    }
                } else {
                    tagStr = attrValue != null ? tagStr.replace(attrValStartPos, attrValEndPos, attrValue.toString()) :
                            tagStr.replace(attributePosition, attrValEndPos + 1, EMPTY_STRING);
                }
            } else {
                int position = tagStr.indexOf(">");
                if (position >= 0) {
                    tagStr.insert(position, insertAttribute(attrName, attrValue));
                }
            }
        }
        return tagStr;
    }

    public static StringBuilder removeAttribute(StringBuilder tagStr, String leosAttr) {
        if (tagStr != null && leosAttr != null) {
            int editableAttrPos = tagStr.indexOf(leosAttr);
            if (editableAttrPos != -1) {
                int editableAttrValStartPos = tagStr.indexOf("=", editableAttrPos) + 2;
                int editableAttrValEndPos = tagStr.indexOf("\"", editableAttrValStartPos) + 1;
                tagStr.delete(editableAttrPos - 1, editableAttrValEndPos);
            }
        }
        return tagStr;
    }

    public static String removeTag(String itemContent) {
        for (String element : ELEMENTS_TO_REMOVE_FROM_CONTENT) {
            itemContent = itemContent.replaceAll("<" + element + ".*?</" + element + ">", "");
        }
        itemContent = itemContent.replaceAll("<[^>]+>", "");
        return itemContent.replaceAll("\\s+", " ").trim();
    }

    public static String removeEnclosingTags(String nodeAsStr) {
        Pattern p = Pattern.compile("^<[^>]+>(.*)</[^ ]+>$", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher(nodeAsStr);
        while (m.find()) {
            nodeAsStr = m.group(1);
        }
        return nodeAsStr;
    }

    public static String trimmedXml(String str) {
//        final String WHITESPACE_REGEX = "(^( )*|( )*$)";
        return str.replaceAll("\\s+", " ").trim();
    }

    public static String addLeosNamespace(String str) {
        return str.replaceFirst(">", " xmlns:leos=\"urn:eu:europa:ec:leos\">")
                .replaceFirst(">", " xmlns=\"http://docs\\.oasis-open\\.org/legaldocml/ns/akn/3\\.0\">");
    }

    public static String removeAllNameSpaces(String str) {
        return str.replaceAll(" xmlns=\"http://docs\\.oasis-open\\.org/legaldocml/ns/akn/3\\.0\"", "")
                .replaceAll(" xmlns:leos=\"urn:eu:europa:ec:leos\"", "")
                .replaceAll(" xmlns:fmx=\"http://formex.*?xd\"", "")
                .replaceAll(" xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"", "")
                .replaceAll(" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"", "")
                .replaceAll("<\\?xml version=\"1\\.0\" encoding=\"UTF-8\"\\?>", "");
    }

    /**
     * Escape the string from only characters interfering with Xerces parsing: "<", ">" and "&". The rest of special characters
     * are left in their UTF representation.
     * In case a full escaping is needed use StringEscapeUtils.escapeHtml()
     */
    public static String escapeXml(String str) {
        return str.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("&", "&amp;")
//                .replaceAll("'", "&apos;")
//                .replaceAll("\"", "&quot;")
                ;
    }

    public static String getOpeningTag(String attrName, String attrValue) {
        return "<span " + attrName + "=\"" + attrValue + "\">";
    }

    public static String getClosingTag() {
        return "</span>";
    }

    public static GregorianCalendar convertStringDateToCalendar(String strDate) {
        try {
            GregorianCalendar gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
            gregorianCalendar.setTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(strDate));
            return gregorianCalendar;
        } catch (Exception ex) {
            return null;
        }
    }

    public static String findString(String nodeAsString, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(nodeAsString);
        String foundString = null;
        if (matcher.find()) {
            foundString = matcher.group(0);
        }
        return foundString;
    }

    // LEOS-2639: replace XML self-closing tags not supported in HTML
    public static String removeSelfClosingElements(String fragment) {
        String removeSelfClosingRegex = "<([^>^\\s]+)([^>]*)/>";
        return fragment.replaceAll(removeSelfClosingRegex, "<$1$2></$1>");
    }

    public static String extractNumber(String numberStr, boolean isNumWithType) {
        if (numberStr != null) {
            if (isNumWithType) {
                return (numberStr.contains(WHITESPACE)) ?
                        numberStr.substring(numberStr.indexOf(WHITESPACE) + 1, numberStr.length()) : numberStr;
            } else {
                return numberStr;
            }
        }
        return null;
    }

    public static String wrapXPathWithQuotes(String value) {
        String wrappedValue = value;
        String apostrophe = "'";
        String quote = "\"";

        if (value.contains(quote)) {
            wrappedValue = apostrophe + value + apostrophe;
        } else {
            wrappedValue = quote + value + quote;
        }
        return wrappedValue;
    }

    public static ImmutableTriple<String, Integer, Integer> getSubstringAvoidingTags(String text, int txtStartOffset, int txtEndOffset) {
        int xmlStartIndex = -1;
        int textCounter = -1;
        boolean stopCounting = false;
        boolean stopCountingAfterLineBreak = false;
        for (char c : text.toCharArray()) {
            if (textCounter == txtStartOffset) {
                break;
            }
            if (c == '\n' && !stopCounting) {
                stopCountingAfterLineBreak = true;
            } else if (c != ' ' && stopCountingAfterLineBreak) {
                stopCountingAfterLineBreak = false;
                // Keep one space
                xmlStartIndex--;
            }
            if (c == '<') {
                stopCounting = true;
            } else if (c == '>') {
                stopCounting = false;
            } else if (!stopCounting && !stopCountingAfterLineBreak) {
                textCounter++;
            }
            xmlStartIndex++;
        }
        text = text.substring(xmlStartIndex);

        int xmlEndIndex = xmlStartIndex;
        int textCounterI = txtStartOffset;
        stopCounting = false;
        stopCountingAfterLineBreak = false;
        for (char c : text.toCharArray()) {
            if (textCounterI == txtEndOffset) {
                break;
            }
            if (c == '\n' && !stopCounting) {
                stopCountingAfterLineBreak = true;
            } else if (c != ' ' && stopCountingAfterLineBreak) {
                stopCountingAfterLineBreak = false;
                // Keep one space
                xmlEndIndex--;
            }
            if (c == '<') {
                stopCounting = true;
            } else if (c == '>') {
                stopCounting = false;
            } else if (!stopCounting && !stopCountingAfterLineBreak) {
                textCounterI++;
            }
            xmlEndIndex++;
        }
        String matchingText = text.substring(0, xmlEndIndex - xmlStartIndex);
        return new ImmutableTriple<>(matchingText, xmlStartIndex, xmlEndIndex);
    }

    public static String normalizeNewText(String origText, String newText) {
        return new StringBuilder(origText.startsWith(WHITESPACE) ? WHITESPACE : EMPTY_STRING)
                .append(org.apache.commons.lang3.StringUtils.normalizeSpace(newText))
                .append(origText.endsWith(WHITESPACE) ? WHITESPACE : EMPTY_STRING).toString();
    }

    public static String addDocTypeToXmlId(String template, String docType) {
        final String regex = String.format("%s=\"", XMLID);
        final String replaceWith = String.format("%s=\"_%s", XMLID, docType.toLowerCase());
        return template.replaceAll(regex, replaceWith);
    }

    public static Integer getAttributeValueAsInteger(String content, String attrName) {
        String attrVal = getAttributeValue(content, attrName);
        return attrVal != null ? Integer.valueOf(attrVal) : null;
    }

    public static Boolean getAttributeValueAsBoolean(String content, String attrName) {
        String attrVal = getAttributeValue(content, attrName);
        return attrVal != null ? Boolean.valueOf(attrVal) : null;
    }

    private static String getAttributeValue(String content, String attrName) {
        String attr = null;
        String search = " " + attrName + "=\"";
        int startIndex = content.indexOf(search);
        if (startIndex != -1) {
            startIndex += search.length();
            int endIndex = content.indexOf("\"", startIndex + 1);
            if (endIndex != -1) {
                attr = content.substring(startIndex, endIndex);
            }
        }
        return attr;
    }

    public static String getSoftUserAttribute(User user) {
        return user.getName().concat("(").
                concat(user.getDefaultEntity().getOrganizationName()).concat(")");
    }
}
