package eu.europa.ec.leos.vo.toc;
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

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StructureConfigUtils {
    
    public static final String NUM_HEADING_SEPARATOR = " - ";
    public static final String HASH_NUM_VALUE = "#";
    public static final String CONTENT_SEPARATOR = " ";

    public static List<TocItemType> getTocItemTypesByTagName(List<TocItem> tocItems, String tagName) {
        TocItem tocItem = getTocItemByName(tocItems, tagName);
        if (tocItem != null && tocItem.getTocItemTypes() != null) {
            return tocItem.getTocItemTypes().getTocItemTypes();
        }
        return Arrays.asList();
    }

    public static NumberingType getNumberingTypeByTagNameAndTocItemType(List<TocItem> tocItems, TocItemTypeName tocItemType, String subElementTagName) {
        List<TocItem> subElementTocItems = getTocItemsByName(tocItems, subElementTagName);
        if (subElementTocItems.size() > 1 && subElementTocItems.get(0).getParentNameNumberingTypeDependency() != null) {
            TocItem parentTocItem = getTocItemByName(tocItems, subElementTocItems.get(0).getParentNameNumberingTypeDependency().value());
            for (TocItemType tocItemTyp : parentTocItem.getTocItemTypes().tocItemTypes) {
                if (tocItemTyp.getName().equals(tocItemType)) {
                    return getNumberingTypeFromSubElementNumberingConfigs(tocItems, subElementTagName, tocItemTyp.getSubElementNumberingConfigs());
                }
            }
        } else if (subElementTocItems.size() >= 1) {
            return subElementTocItems.get(0).getNumberingType();
        }
        return null;
    }

    public static TocItem getTocItemByTagNameAndTocItemType(List<TocItem> tocItems, TocItemTypeName tocItemType, String subElementTagName) {
        List<TocItem> subElementTocItems = getTocItemsByName(tocItems, subElementTagName);
        if (subElementTocItems.size() > 1 && subElementTocItems.get(0).getParentNameNumberingTypeDependency() != null) {
            TocItem parentTocItem = getTocItemByName(tocItems, subElementTocItems.get(0).getParentNameNumberingTypeDependency().value());
            for (TocItemType tocItemTyp : parentTocItem.getTocItemTypes().tocItemTypes) {
                if (tocItemTyp.getName().equals(tocItemType)) {
                    NumberingType numberingType = getNumberingTypeFromSubElementNumberingConfigs(tocItems, subElementTagName,
                            tocItemTyp.getSubElementNumberingConfigs());
                    return getTocItemByNumberingType(tocItems, numberingType, subElementTagName);
                }
            }
        } else if (subElementTocItems.size() == 1) {
            return getTocItemByNumberingType(tocItems, subElementTocItems.get(0).getNumberingType(), subElementTagName);
        }
        return null;
    }

    public static Attribute getAttributeByTagNameAndTocItemType(List<TocItem> tocItems, TocItemTypeName tocItemType, String tagName) {
        TocItem tocItem = getTocItemByName(tocItems, tagName);
        if (tocItem != null && tocItem.getTocItemTypes() != null) {
            for (TocItemType tocItemTyp : tocItem.getTocItemTypes().getTocItemTypes()) {
                if (tocItemTyp.getName().equals(tocItemType)) {
                    return tocItemTyp.getAttribute();
                }
            }
        }
        return null;
    }

    public static Map<TocItemTypeName, List<Level>> getNumberingConfigsFromTocItem(List<NumberingConfig> numberingConfigs, List<TocItem> tocItems,
                                                                                String tagName) {
        List<TocItem> foundTocItems = getTocItemsByName(tocItems, tagName);
        Map<TocItemTypeName, List<Level>> foundNumberingConfigs = new HashMap<>();
        if (foundTocItems.size() == 1) {
            NumberingConfig numberingConfig = getNumberingConfig(numberingConfigs, foundTocItems.get(0).getNumberingType());
            foundNumberingConfigs.put(TocItemTypeName.REGULAR, numberingConfig != null ? numberingConfig.getLevels().getLevels() : null);
        } else if (foundTocItems.size() > 1 && foundTocItems.get(0).getParentNameNumberingTypeDependency() != null) {
            TocItem tocItem = getTocItemByName(tocItems, foundTocItems.get(0).getParentNameNumberingTypeDependency().value());
            if (tocItem != null) {
                for (TocItemType tocItemTyp : tocItem.getTocItemTypes().getTocItemTypes()) {
                    TocItemTypeName tocItemType = tocItemTyp.getName();
                    SubElementNumberingConfigs subElementNumberingConfigs = tocItemTyp.getSubElementNumberingConfigs();
                    if (!subElementNumberingConfigs.getSubElementNumberingConfigs().isEmpty()) {
                        for (SubElementNumberingConfig subElementNumberingConfig : subElementNumberingConfigs.getSubElementNumberingConfigs()) {
                            if (subElementNumberingConfig.getSubElement().value().equals(tagName)) {
                                NumberingConfig numberingConfig = getNumberingConfig(numberingConfigs, subElementNumberingConfig.getNumberingType());
                                foundNumberingConfigs.put(tocItemType, numberingConfig != null ? numberingConfig.getLevels().getLevels() : null);
                            }
                        }
                    }
                }
            }
        }
        return foundNumberingConfigs;
    }

    private static NumberingType getNumberingTypeFromSubElementNumberingConfigs(List<TocItem> tocItems,
                                                                        String subElementTagName, SubElementNumberingConfigs subElementNumberingConfigs) {
        if (!subElementNumberingConfigs.getSubElementNumberingConfigs().isEmpty()) {
            for (SubElementNumberingConfig subElementNumberingConfig : subElementNumberingConfigs.getSubElementNumberingConfigs()) {
                if (subElementNumberingConfig.getSubElement().value().equals(subElementTagName)) {
                    return subElementNumberingConfig.getNumberingType();
                }
            }
        }
        TocItem tocItem = getTocItemByName(tocItems, subElementTagName);
        if (tocItem != null) {
            return tocItem.getNumberingType();
        }
        return null;
    }

    public static TocItemTypeName getTocItemTypeFromTagNameAndAttributes(List<TocItem> tocItems, String tagName, Map<String, String> attributes) {
        TocItem tocItem = getTocItemByName(tocItems, tagName);
        TocItemTypeName tocItemType = TocItemTypeName.REGULAR;
        if (tocItem != null && tocItem.getTocItemTypes() != null) {
            for (TocItemType tocItemTyp : tocItem.getTocItemTypes().tocItemTypes) {
                if (tocItemTyp.getAttribute() != null
                        && attributes.containsKey(tocItemTyp.getAttribute().getAttributeName())
                        && attributes.get(tocItemTyp.getAttribute().getAttributeName()).equals(tocItemTyp.getAttribute().getAttributeValue())) {
                    return tocItemTyp.getName();
                }
            }
        }
        return tocItemType;
    }

    public static TocItem getTocItemByName(List<TocItem> tocItems, String tagName) {
        return tocItems.stream()
                .filter(tocItem -> tocItem.getAknTag().value().equalsIgnoreCase(tagName))
                .findFirst()
                .orElse(null);
    }

    public static TocItem getTocItemByNameOrThrow(List<TocItem> tocItems, String tagName) {
        return tocItems.stream()
                .filter(tocItem -> tocItem.getAknTag().value().equalsIgnoreCase(tagName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("TocItem '" + tagName + "' not present in the list of Items [" + getTocItemNamesAsList(tocItems) + "]"));
    }

    public static List<String> getTocItemNamesAsList(List<TocItem> tocItems) {
        return tocItems.stream()
                .map(tocItem -> tocItem.getAknTag().value())
                .collect(Collectors.toList());
    }

    public static List<TocItem> getTocItemsByName(List<TocItem> tocItems, String tagName) {
        return tocItems.stream()
                .filter(tocItem -> tocItem.getAknTag().value().equalsIgnoreCase(tagName))
                .collect(Collectors.toList());
    }

    public static TocItem getTocItemByNumberingType(List<TocItem> tocItems, NumberingType numType, String tagName) {
        return tocItems.stream()
                .filter(tocItem -> tocItem.getAknTag().name().equalsIgnoreCase(tagName) && tocItem.getNumberingType().equals(numType)).findFirst()
                .orElseThrow(() -> new IllegalStateException("NumberingType '" + numType + "' not present in the list of TocItems [" + tocItems + "]"));
    }

    public static TocItem getTocItemByNumValue(List<NumberingConfig> numberingConfigs, List<TocItem> foundTocItems, String numValue, int depth) {
        List<NumberingType> numberingTypes = getNumberingTypesBySequence(numberingConfigs, numValue);
        return foundTocItems.stream()
                .filter(tocItem -> {
                    NumberingConfig tocItemNumberingConfig = getNumberingConfig(numberingConfigs, tocItem.getNumberingType());
                    return tocItemNumberingConfig != null && (isNumberingTypeMatchesSequence(numberingConfigs, tocItem.getNumberingType(), numValue)
                            || isNumberingTypesPartOfNumberingConfig(numberingTypes, tocItemNumberingConfig, depth));
                })
                .findFirst()
                .orElse(null);
    }

    private static boolean isNumberingTypesPartOfNumberingConfig(List<NumberingType> numberingTypes, NumberingConfig numberingConfig, int depth) {
        if (!numberingTypes.isEmpty() && numberingConfig.getLevels() != null && !numberingConfig.getLevels().getLevels().isEmpty()) {
            return numberingConfig.getLevels().getLevels().stream().anyMatch(level -> level.getDepth() == depth && numberingTypes.contains(level.getNumberingType()));
        }
        return false;
    }

    public static NumberingConfig getNumberingByName(List<NumberingConfig> numberingConfigs, NumberingType numType) {
        return numberingConfigs.stream()
                .filter(config -> config.getType().equals(numType)).findFirst()
                .orElseThrow(() -> new IllegalStateException("NumberingType '" + numType + "' not present in the list of NumberingConfigs [" + numberingConfigs + "]"));
    }

    public static NumberingType getNumberingTypeByDepth(NumberingConfig numberingConfig, int depth) {
        if (numberingConfig.getLevels() == null) {
            return numberingConfig.getType();
        } else {
            return numberingConfig.getLevels().getLevels().stream()
                    .filter(level -> level.getDepth() == depth).findFirst()
                    .map(level -> level.getNumberingType())
                    .orElseThrow(() -> new IllegalStateException("Depth '" + depth + "' not defined in the NumberingConfig [" + numberingConfig + "]"));
        }
    }

    public static int getDepthByNumberingType(List<NumberingConfig> numberingConfigs, NumberingType numberingType) {
        final NumberingConfig pointNumberingConfig = numberingConfigs.stream()
                .filter(config -> config.getType() == NumberingType.POINT_NUM)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("NumberingType '" + NumberingType.POINT_NUM + "' not defined in the NumberingConfigs [" + numberingConfigs + "]"));
        final int depth = pointNumberingConfig.getLevels().getLevels().stream().filter(level -> level.getNumberingType() == numberingType)
                .findFirst()
                .map(level -> level.getDepth())
                .orElseThrow(() -> new IllegalStateException("NumberingType '" + numberingType + "' not defined in the NumberingConfig [" + pointNumberingConfig + "]"));
        return depth;
    }

    public static NumberingConfig getNumberingConfig(List<NumberingConfig> numberConfigs, NumberingType numType) {
        return numberConfigs.stream()
                .filter(numberingConfig -> numberingConfig.getType().equals(numType))
                .findFirst()
                .orElse(null);
    }
    
    public static NumberingConfig getNumberingConfigByTagName(List<TocItem> items, List<NumberingConfig> numberingConfigs, String tagName) {
        TocItem tocItem = getTocItemByName(items, tagName);
        return getNumberingByName(numberingConfigs, tocItem.getNumberingType());
    }

    public static NumberingType getNumberingTypeBySequence(List<NumberingConfig> numberingConfigs, String sequence) {
        NumberingConfig numberingConfig = numberingConfigs.stream()
                .filter(numberConfig -> {
                    if (StringUtils.isNotBlank(numberConfig.getSequence())) {
                        String numValueToCompare = "";
                        if (numberConfig.getPrefix() != null) {
                            numValueToCompare += numberConfig.getPrefix();
                        }
                        numValueToCompare += numberConfig.getSequence();
                        if (numberConfig.getSuffix() != null) {
                            numValueToCompare += numberConfig.getSuffix();
                        }

                        return (numValueToCompare.equalsIgnoreCase(sequence));
                    }
                    return false;
                })
                .findFirst()
                .orElse(null);
        if (numberingConfig == null) {
            return null;
        }
        return numberingConfig.getType();
    }

    public static List<NumberingType> getNumberingTypesBySequence(List<NumberingConfig> numberingConfigs, String sequence) {
        List<NumberingConfig> matchingNumberingConfigs = numberingConfigs.stream()
                .filter(numberConfig -> {
                    if (StringUtils.isNotBlank(numberConfig.getSequence())) {
                        String numValueToCompare = "";
                        if (numberConfig.getPrefix() != null) {
                            numValueToCompare += numberConfig.getPrefix();
                        }
                        numValueToCompare += numberConfig.getSequence();
                        if (numberConfig.getSuffix() != null) {
                            numValueToCompare += numberConfig.getSuffix();
                        }
                        if (numValueToCompare.equalsIgnoreCase(sequence)) {
                            return true;
                        } else if (StringUtils.isNotBlank(numberConfig.getRegex())
                                && isNumValueWithPrefixAndSuffix(sequence, numberConfig)) {
                            Pattern pattern = Pattern.compile(numberConfig.getRegex());
                            String num = getNumValueWithoutPrefixAndSuffix(sequence, numberConfig);
                            Matcher matcher = pattern.matcher(num);
                            return matcher.find() && StringUtils.isNotBlank(matcher.group(0));
                        }
                    }
                    return false;
                }).collect(Collectors.toList());
        return matchingNumberingConfigs.stream().map(nc -> nc.getType()).collect(Collectors.toList());
    }

    private static boolean isNumberingTypeMatchesSequence(List<NumberingConfig> numberingConfigs, NumberingType numberingType, String numValue) {
        NumberingConfig numberingConfig = getNumberingConfig(numberingConfigs, numberingType);
        String numValueWithoutPrefixAndSuffix = getNumValueWithoutPrefixAndSuffix(numValue, numberingConfig);
        return (isNumValueWithPrefixAndSuffix(numValue, numberingConfig) &&
                numberingConfig.getSequence() != null
                && numberingConfig.getSequence().equalsIgnoreCase(numValueWithoutPrefixAndSuffix));
    }

    private static boolean isNumValueWithPrefixAndSuffix(final String numValue, final NumberingConfig numberingConfig) {
        if (numberingConfig.getPrefix() != null && !numberingConfig.getPrefix().isEmpty() && !numValue.startsWith(numberingConfig.getPrefix())) {
            return false;
        }
        return numberingConfig.getSuffix() == null || numberingConfig.getSuffix().isEmpty() || numValue.endsWith(numberingConfig.getSuffix());
    }

    private static String getNumValueWithoutPrefixAndSuffix(String numValue, NumberingConfig numberingConfig) {
        String numValueWithoutPrefixAndSuffix = numValue;
        if (numberingConfig.getPrefix() != null && !numberingConfig.getPrefix().isEmpty() && numValueWithoutPrefixAndSuffix.startsWith(numberingConfig.getPrefix())) {
            numValueWithoutPrefixAndSuffix = numValueWithoutPrefixAndSuffix.substring(numberingConfig.getPrefix().length());
        }
        if (numberingConfig.getSuffix() != null && !numberingConfig.getSuffix().isEmpty() && numValueWithoutPrefixAndSuffix.endsWith(numberingConfig.getSuffix())) {
            numValueWithoutPrefixAndSuffix = numValueWithoutPrefixAndSuffix.substring(0, numValue.length() - numberingConfig.getSuffix().length() - 1);
        }
        return numValueWithoutPrefixAndSuffix;
    }

    public static boolean isAutoNumberingEnabled(List<TocItem> tocItems, String elementName) {
        TocItem tocItem = getTocItemByName(tocItems, elementName);
        Boolean isAutoNumEnabled = tocItem != null ? tocItem.isAutoNumbering() == null ? true : tocItem.isAutoNumbering() : false;
        return isAutoNumEnabled;
    }

}
