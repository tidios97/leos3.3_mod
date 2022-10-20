package eu.europa.ec.leos.services.numbering.config;

import eu.europa.ec.leos.services.support.XercesUtils;
import eu.europa.ec.leos.services.toc.StructureContext;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.NumberingType;
import eu.europa.ec.leos.vo.toc.TocItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.w3c.dom.Node;

import javax.inject.Provider;
import java.util.List;

import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_LIST_TYPE_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.NUM;
import static eu.europa.ec.leos.vo.toc.StructureConfigUtils.getNumberingByName;
import static eu.europa.ec.leos.vo.toc.StructureConfigUtils.getNumberingTypeByDepth;
import static eu.europa.ec.leos.vo.toc.StructureConfigUtils.getTocItemByNumValue;
import static eu.europa.ec.leos.vo.toc.StructureConfigUtils.getTocItemsByName;

@Configuration
public class NumberConfigFactory {

    private static final Logger LOG = LoggerFactory.getLogger(NumberConfigFactory.class);

    @Autowired
    protected Provider<StructureContext> structureContextProvider;

    public NumberConfig getNumberConfig(String elementName, int depth, final Node firstElement) {
        List<TocItem> tocItems = structureContextProvider.get().getTocItems();
        List<NumberingConfig> numberingConfigs = structureContextProvider.get().getNumberingConfigs();
        String listTypeAttributeValue = XercesUtils.getAttributeValue(firstElement.getParentNode(), LEOS_LIST_TYPE_ATTR);
        List<TocItem> foundTocItems = getTocItemsByName(tocItems, elementName);
        NumberingType numberingType ;
        if (listTypeAttributeValue != null) {
            numberingType = NumberingType.fromValue(listTypeAttributeValue.toUpperCase());
            XercesUtils.removeAttribute(firstElement.getParentNode(), LEOS_LIST_TYPE_ATTR);
        } else if (foundTocItems.size() > 1 && XercesUtils.getFirstChild(firstElement, NUM) != null) {
            String currentNum = XercesUtils.getNodeNum(firstElement);
            TocItem tocItem = getTocItemByNumValue(numberingConfigs, foundTocItems, currentNum);
            if (tocItem != null) {
                numberingType = tocItem.getNumberingType();
            } else {
                numberingType = foundTocItems.get(0).getNumberingType();
            }
        } else {
            numberingType = foundTocItems.get(0).getNumberingType();
        }

        NumberingConfig numberingConfig = getNumberingByName(numberingConfigs, numberingType);
        if (depth != 0) { // if is a POINT, different config depending on the depth
            numberingType = getNumberingTypeByDepth(numberingConfig, depth);
            numberingConfig = getNumberingByName(numberingConfigs, numberingType); // update config
        }

        NumberConfig numberConfig = getNumberConfig(numberingType, numberingConfig);
        return numberConfig;
    }

    private NumberConfig getNumberConfig(NumberingType numberingType, NumberingConfig numberingConfig) {
        String prefix = numberingConfig.getPrefix();
        String suffix = numberingConfig.getSuffix();
        switch (numberingType) {
            case ARABIC:
            case ARABIC_POSTFIXDOT:
            case ARABIC_PARENTHESIS:
                return new NumberConfigArabic(prefix, suffix);
            case ROMAN_LOWER_PARENTHESIS:
                return new NumberConfigRoman(false, prefix, suffix);
            case ROMAN_UPPER:
            case ROMAN_UPPER_POSTFIXDOT:
            case ROMAN_UPPER_POSTFIXPARENTHESIS:
                return new NumberConfigRoman(true, prefix, suffix);
            case ALPHA_LOWER_PARENTHESIS:
                return new NumberConfigAlpha(false, prefix, suffix);
            case ALPHA_UPPER_POSTFIXDOT:
            case ALPHA_UPPER_POSTFIXPARENTHESIS:
                return new NumberConfigAlpha(true, prefix, suffix);
            case BULLET_BLACK_CIRCLE:
            case BULLET_WHITE_CIRCLE:
            case BULLET_BLACK_SQUARE:
            case BULLET_WHITE_SQUARE:
            case INDENT:
                return new NumberConfigSymbol(numberingConfig.getSequence(), prefix, suffix);
            case NONE:
                return null;
            default:
                throw new IllegalStateException("No configuration found for numbering: " + numberingConfig.getType());
        }
    }

}
