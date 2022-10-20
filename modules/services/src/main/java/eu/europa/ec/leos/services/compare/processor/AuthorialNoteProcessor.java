package eu.europa.ec.leos.services.compare.processor;

import eu.europa.ec.leos.services.compare.ContentComparatorContext;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthorialNoteProcessor {

    Integer originalLength = 0;
    /**
     * Get the list of authorial notes in a string
     * @param content
     * @return list of authorial notes
     */
    public List<AuthorialNote> getAuthorialNotes(String content)  {
        if (StringUtils.isEmpty(content) || !content.contains("</authorialNote>")) {
            return null;
        }
        List<AuthorialNote> authorialNotes = new ArrayList<>();
        Pattern tagPattern = Pattern.compile("<(\\S+?)(.*?)>(.*?)</\\1>");
        Matcher tagMatcher = tagPattern.matcher(content);

        /*
         *   m.group(0) => tag only
         *   m.group(1) => tag name
         *   m.group(2) => tag attributes
         *   m.group(3) => tag content
         */
        while (tagMatcher.find()) {
            String tagName = tagMatcher.group(1);
            String tagAttributes = tagMatcher.group(2);
            String tagXmlId = "xml:id";
            if(tagAttributes.contains(" id")) {
                tagXmlId = "id";
            }
            if(tagName.equals("authorialNote")) {
                Pattern xmlIdPattern = Pattern.compile("[\\s\\S]*?" + tagXmlId + "=\"(?<gXmlId>[\\s\\S]*?)\"[\\s\\S]*?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                Matcher xmlIdMatcher = xmlIdPattern.matcher(tagAttributes);
                xmlIdMatcher.find();
                String xmlId = xmlIdMatcher.group("gXmlId");

                Pattern markerPattern = Pattern.compile("[\\s\\S]*?marker=\"(?<gMarker>[\\s\\S]*?)\"[\\s\\S]*?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                Matcher markerMatcher = markerPattern.matcher(tagAttributes);
                markerMatcher.find();
                String marker = markerMatcher.group("gMarker");

                AuthorialNote authorialNote = new AuthorialNote(xmlId, marker, tagMatcher.start(), tagMatcher.group(), tagAttributes);
                authorialNotes.add(authorialNote);
            }
        }

        return authorialNotes;
    }

    /**
     * Update content only authorial notes in parameters
     * @param content
     * @param authorialNotes
     * @return content without authorial notes
     */
    public String updateExistingAuthorialNotes(String content, List<AuthorialNote> authorialNotes ,List<AuthorialNote> authorialNotesCompared, boolean currentContent)  {
        if (StringUtils.isEmpty(content) || !content.contains("</authorialNote>") || authorialNotes == null) {
            return content;
        }

        for(AuthorialNote authorialNote:authorialNotes) {
            String marker = authorialNote.getMarker();
            AuthorialNote note = getAuthorialNoteByIdDifferentMarker(authorialNotesCompared,authorialNote.getXmlId(), authorialNote.getMarker());
            if(note != null && !note.getMarker().equals(marker)) {
                if(currentContent) {
                    String originalMarker = "(" + note.getMarker() + ")";
                    content = content.replace("marker=\"" + marker + "\"",
                            "marker=\"" + originalMarker + "" +
                                    marker + "\"");
                } else {
                    String originalMarker = "(" + marker + ")";
                    content = content.replace("marker=\"" + marker + "\"",
                            "marker=\"" + originalMarker + "" +
                                    note.getMarker() + "\"");
                }
            }

        }

        return content;
    }

    /**
     * Update footnotes for live - replace brackets by strikethrough markers
     * @param content
     * @param authorialNotes
     * @param removedValue
     * @return content with authorial notes strikethrough
     */
    public String adjustAuthorialNotes(String content, List<AuthorialNote> authorialNotes, String removedValue)  {
        if (StringUtils.isEmpty(content) || !content.contains("</authorialNote>") || authorialNotes == null) {
            return content;
        }

        for(AuthorialNote authorialNote:authorialNotes) {
            String oldMarker = StringUtils.substringBetween(authorialNote.getMarker(), "(", ")");
            String newMarker = authorialNote.getMarker();
            if(oldMarker == null) {
                oldMarker = authorialNote.getMarker();
            } else {
                Integer lengthMarker = authorialNote.getMarker().length() - (oldMarker.length() + 2);
                newMarker = StringUtils.right(authorialNote.getMarker(), lengthMarker);
            }

            if(!oldMarker.equals(newMarker)) {
                String oldNote = authorialNote.getNote().replace(authorialNote.getMarker(), oldMarker);
                String search = "<authorialNote" + authorialNote.getAttributes();
                String replace = search.replace(authorialNote.getMarker(),newMarker);

                content = content.replace(search, "<span class=\"" + removedValue + "\">" + oldNote + "</span>" + replace);
            }

        }

        return content;
    }

    /**
     * Delete in content only authorial notes in parameters
     * @param content
     * @param authorialNotes
     * @param authorialNotesCompared
     * @return content without authorial notes
     */
    public String deleteExistingAuthorialNotes(String content, List<AuthorialNote> authorialNotes ,List<AuthorialNote> authorialNotesCompared)  {
        if (StringUtils.isEmpty(content) || !content.contains("</authorialNote>") || authorialNotes == null) {
            return content;
        }

        for(AuthorialNote authorialNote:authorialNotes) {
            String xmlId = authorialNote.getXmlId();
            AuthorialNote note = getAuthorialNoteById(authorialNotesCompared,xmlId);
            if(note != null && note.getXmlId().equals(xmlId)) {
                content = content.replace(authorialNote.getNote(), "");
            }

        }

        return content;
    }

    /**
     * Check if character after footnote is alphanumeric
     * If yes, add space between footnote and next word
     * @param content
     * @return content with additional space if needed
     */
    public String processCharacterAfterAuthorialNotes(String content)  {
        if (StringUtils.isEmpty(content) || !content.contains("</authorialNote>")) {
            return content;
        }
        Pattern tagPattern = Pattern.compile("<(\\S+?)(.*?)>(.*?)</\\1>");
        Matcher tagMatcher = tagPattern.matcher(content);

        /*
         *   m.group(0) => tag only
         *   m.group(1) => tag name
         *   m.group(2) => tag attributes
         *   m.group(3) => tag content
         */
        while (tagMatcher.find()) {
            String tagName = tagMatcher.group(1);
            if(tagName.equals("authorialNote")) {

                String tagOnly = tagMatcher.group(0);
                Integer tagPosition = content.lastIndexOf(tagOnly) + tagOnly.length();
                if(tagPosition < content.length()) {
                    String lastCharacter = content.substring(tagPosition, tagPosition + 1);

                    if (isAlphaNumeric(lastCharacter)) {
                        content = content.replace(tagOnly + lastCharacter, tagOnly + " " + lastCharacter);
                        //Need to one to length because of the space added
                        originalLength++;
                    }
                }
            }
        }

        return content;
    }

    /**
     * Delete the authorial notes from the content and store in a string
     * @param content
     * @return content without the authorial notes
     */
    public String deleteAllAuthorialNotes(String content) {
        return content.replaceAll("<authorialNote[\\s\\S]*?>[\\s\\S]*?</authorialNote>", "");
    }

    /**
     * Get Authorialnote by id with different marker in List
     * @param authorialNotes
     * @param id
     * @param marker
     * @return authorialNote selected by id
     */
    public AuthorialNote getAuthorialNoteByIdDifferentMarker(List<AuthorialNote> authorialNotes, String id, String marker) {
        if(authorialNotes == null) {
            return null;
        }
        return authorialNotes.stream().filter(authorialNote -> id.equals(authorialNote.getXmlId())).filter(authorialNote -> !marker.equals(authorialNote.getMarker())).findAny().orElse(null);
    }

    /**
     * Get Authorialnote by id in List
     * @param authorialNotes
     * @param id
     * @return authorialNote selected by id
     */
    public static AuthorialNote getAuthorialNoteById(List<AuthorialNote> authorialNotes, String id) {
        if(authorialNotes == null) {
            return null;
        }
        return authorialNotes.stream().filter(authorialNote -> id.equals(authorialNote.getXmlId())).findAny().orElse(null);
    }

    /**
     * Get Authorialnote by id and marker in List
     * @param authorialNotes
     * @param id
     * @param marker
     * @return authorialNote selected by id
     */
    public static AuthorialNote getAuthorialNoteByIdAndMarker(List<AuthorialNote> authorialNotes, String id, String marker) {
        if(authorialNotes == null) {
            return null;
        }
        return authorialNotes.stream().filter(authorialNote -> id.equals(authorialNote.getXmlId())).filter(authorialNote -> marker.equals(authorialNote.getMarker())).findAny().orElse(null);
    }

    /**
     * Get Length of removed span added by the diffing library
     * Useful when content contains footnotes deleted in original
     * @param content
     * @param start
     * @param context
     * @return Length of removed span
     */
    public Integer getRemovedSpanDiffingLength(String content, Integer start, ContentComparatorContext context) {

        Pattern patternSpans = Pattern.compile("<span[^>]*>[\\s\\S]+<\\/span>");

        content = content.replaceAll("</span>", "</span><!DELIMITER>");
        String[] spans = content.split("<!DELIMITER>");

        Integer lengthRemovedSpan = 0;
        Integer lengthSpan = 0;
        for(String span:spans) {
            Matcher matcherSpans = patternSpans.matcher(span);
            while(matcherSpans.find()) {
                Integer currentLength = matcherSpans.group(0).length();
                if((lengthSpan + matcherSpans.start()) <= start && matcherSpans.group(0).contains(context.getRemovedValue())) {
                    start += currentLength;
                    lengthRemovedSpan += currentLength;
                    if(isEmptyCharacterBefore(span, matcherSpans.start())) {
                        lengthRemovedSpan++;
                    }
                } else {
                    start += currentLength;
                }
            }
            lengthSpan += span.length();
        }
        return lengthRemovedSpan;
    }

    /**
     * Get Length of removed span added by the diffing library
     * Useful when content contains footnotes added in current not present in original
     * @param content
     * @param start
     * @return Length of added span
     */
    public Integer getAddedSpanDiffingLength(String content, Integer start, ContentComparatorContext context)  {
        if (StringUtils.isEmpty(content) || !content.contains("</span>")) {
            return 0;
        }

        Pattern tagPattern = Pattern.compile("<(\\S+?)(.*?)>(.*?)</\\1>");
        Matcher tagMatcher = tagPattern.matcher(content);
        Integer lengthSpan = 0;
        /*
         *   m.group(0) => tag only
         *   m.group(1) => tag name
         *   m.group(2) => tag attributes
         *   m.group(3) => tag content
         */
        while (tagMatcher.find()) {
            String tagName = tagMatcher.group(1);
            if(tagName.equals("span")) {
                String tagOnly = tagMatcher.group(0);
                String tagContent =  tagMatcher.group(3);
                Integer regionStartTag = tagMatcher.start();
                if(regionStartTag < start && tagOnly.contains(context.getAddedValue())) {
                    lengthSpan += tagOnly.length() - tagContent.length();
                    if(tagContent.contains("</authorialNote>")) {
                        lengthSpan++;
                    }
                }
            }
        }

        return lengthSpan;
    }

    /**
     * Check if string is alphanumeric
     * @param content
     * @return true/false
     */
    public static boolean isAlphaNumeric(String content) {
        return content != null &&
                content.chars().allMatch(Character::isLetterOrDigit);
    }

    /**
     * Add substring in string at exact position
     * Useful when postprocess authorial notes
     * @param source
     * @param append
     * @param position
     * @return string with substring
     */
    public String addStringAtPosition(String source, String append, Integer position) {
        if(isEmptyCharacterBefore(source, position)) {
            position = position - 1;
        }
        return source.substring(0, position) + append + source.substring(position);
    }

    /**
     * Check if character before is empty
     * @param content
     * @param position
     * @return true/false
     */
    public boolean isEmptyCharacterBefore(String content, Integer position) {
        if (position > 0) {
            String before = content.substring(position - 1, position);
            if(before.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }


    /**
     * Diffing content from authorial notes
     * @param content
     * @param authorialNotes
     * @param authorialNotesCompared
     * @param context
     * @return Content with diffing on authorial notes
     */
    public String diffingExistingAuthorialNotes(String content, List<AuthorialNote> authorialNotes ,List<AuthorialNote> authorialNotesCompared, ContentComparatorContext context, boolean firstLetterCaseSensitive)  {
        return content;
    }

    /**
     * Double Diffing content from authorial notes
     * @param content
     * @param authorialNotes
     * @param authorialNotesOriginal
     * @param authorialNotesIntermediate
     * @param context
     * @return Content with diffing on authorial notes
     */
    public String doubleDiffingExistingAuthorialNotes(String content, List<AuthorialNote> authorialNotes ,List<AuthorialNote> authorialNotesOriginal, List<AuthorialNote> authorialNotesIntermediate, ContentComparatorContext context, boolean firstLetterCaseSensitive)  {
        return content;
    }

    /**
     * Adjust existing content from authorial notes
     * @param content
     * @param authorialNotes
     * @param authorialNotesCompared
     * @param context
     * @return Content with adjust on authorial notes
     */
    public String adjustExistingAuthorialNotes(String content, List<AuthorialNote> authorialNotes ,List<AuthorialNote> authorialNotesCompared, ContentComparatorContext context)  {
        if (org.apache.commons.lang.StringUtils.isEmpty(content) || !content.contains("</authorialNote>") || authorialNotes == null) {
            return content;
        }

        for(AuthorialNote authorialNote:authorialNotes) {
            String xmlId = authorialNote.getXmlId();
            String currentNote = authorialNote.getNote();
            AuthorialNote note = AuthorialNoteProcessor.getAuthorialNoteById(authorialNotesCompared,authorialNote.getXmlId());
            if(note != null && note.getXmlId().equals(xmlId) && !note.getNote().equals(currentNote)) {

                content = content.replace(currentNote, note.getNote());
            }
        }

        return content;
    }

    /**
     * AuthorialNote object
     */
    public static class AuthorialNote {

        public String xmlId;
        public String marker;
        public Integer regionStart;
        public String note;
        public String attributes;

        public AuthorialNote(String xmlId, String marker, Integer regionStart, String note, String attributes) {
            this.xmlId = xmlId;
            this.marker = marker;
            this.regionStart = regionStart;
            this.note = note;
            this.attributes = attributes;
        }

        public String getXmlId() {
            return xmlId;
        }

        public void setXmlId(String xmlId) {
            this.xmlId = xmlId;
        }

        public String getMarker() {
            return marker;
        }

        public void setMarker(String marker) {
            this.marker = marker;
        }

        public Integer getRegionStart() {
            return regionStart;
        }

        public void setRegionStart(Integer regionStart) {
            this.regionStart = regionStart;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getAttributes() {
            return attributes;
        }

        public void setAttributes(String attributes) {
            this.attributes = attributes;
        }

        @Override
        public boolean equals(Object rightNote) {
            if (this == rightNote) return true;
            if (rightNote == null || getClass() != rightNote.getClass()) return false;
            AuthorialNote leftNote = (AuthorialNote) rightNote;
            return marker.equals(leftNote.marker) &&
                    note.equals(leftNote.note);
        }
    }
}