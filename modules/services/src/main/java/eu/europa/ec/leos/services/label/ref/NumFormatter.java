package eu.europa.ec.leos.services.label.ref;

import com.google.common.base.Strings;
import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.XmlHelper.CHAPTER;
import static eu.europa.ec.leos.services.support.XmlHelper.CITATION;
import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.PART;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.SECTION;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPOINT_LABEL;
import static eu.europa.ec.leos.services.support.XmlHelper.TITLE;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;

class NumFormatter {
    static private final List<String> unNumberedItems = Arrays.asList(CITATION, PARAGRAPH, SUBPARAGRAPH, SUBPOINT_LABEL, POINT, INDENT);

    static String formattedNum(TreeNode node, Locale locale) {
        switch (node.getType()) {
            case PART:
            case TITLE:
            case CHAPTER:
            case SECTION:
            case ARTICLE:
            case LEVEL:
                return node.getNum();
            default:
                return (isUnnumbered(node)) ? formatUnnumbered(node, locale) : formatNumbered(node);
        }
    }

    private static String formatNumbered(TreeNode node) {
        return Strings.isNullOrEmpty(node.getNum()) ? "" : String.format("(%s)", node.getNum());
    }

    static boolean isUnnumbered(TreeNode node) {
        if (unNumberedItems.contains(node.getType())
                && (StringUtils.isEmpty(node.getNum()) || node.getNum().matches("[^a-zA-Z0-9]+"))) {
            return true;
        } else {
            return false;
        }
    }

    static boolean anyUnnumberedParent(TreeNode node) {
        if(ARTICLE.equals(node.getType()) || node.getParent() == null){
            return false;
        }
        else{
            return isUnnumbered(node) ||
                    anyUnnumberedParent(node.getParent());
        }        
    }
    
    static String formatUnnumbered(TreeNode node, Locale locale) {
        RuleBasedNumberFormat numberFormat = new RuleBasedNumberFormat(locale, RuleBasedNumberFormat.SPELLOUT);
        //FIXME: Need to define spellout rules for other than english in files RbnfRulesSet
        //once done, remove if below
        if (locale.getLanguage().equalsIgnoreCase("en")) {
            numberFormat.setDefaultRuleSet("%spellout-ordinal");
        }
        
        return numberFormat.format(node.getSiblingNumber());
    }
    
    static String formatPlural(TreeNode node, int number, Locale locale) {
        return formatPlural(node.getType(), number, locale);
    }

    static String formatPlural(String nodeType, int number, Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(new ClassPathResource("messages/message").getPath(), locale);
        String pattern = bundle.getString("plural");
        MessageFormat msgFormat = new MessageFormat(pattern, locale);
        return msgFormat.format(new Object[] {nodeType, number});
    }
}