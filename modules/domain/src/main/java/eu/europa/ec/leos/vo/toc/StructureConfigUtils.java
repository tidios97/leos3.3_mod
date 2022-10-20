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

import java.util.List;
import java.util.stream.Collectors;

public class StructureConfigUtils {
    
    public static final String NUM_HEADING_SEPARATOR = " - ";
    public static final String HASH_NUM_VALUE = "#";
    public static final String CONTENT_SEPARATOR = " ";
    
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

    public static TocItem getTocItemByNumberingConfig(List<TocItem> tocItems, NumberingType numType, String tagName) {
        return tocItems.stream()
                .filter(tocItem -> tocItem.getAknTag().name().equalsIgnoreCase(tagName) && tocItem.getNumberingType().equals(numType)).findFirst()
                .orElseThrow(() -> new IllegalStateException("NumberingType '" + numType + "' not present in the list of TocItems [" + tocItems + "]"));
    }

    public static TocItem getTocItemByNumValue(List<NumberingConfig> numberingConfigs, List<TocItem> foundTocItems, String numValue, int depth) {
        NumberingType numberingType = getNumberingTypeBySequence(numberingConfigs, numValue);
        return foundTocItems.stream()
                .filter(tocItem -> {
                    NumberingConfig tocItemNumberingConfig = getNumberingConfig(numberingConfigs, tocItem.getNumberingType());
                    return isNumberingTypeMatchesSequence(numberingConfigs, tocItem.getNumberingType(), numValue)
                            || isNumberingTypePartOfNumberingConfig(numberingType, tocItemNumberingConfig, depth);
                })
                .findFirst()
                .orElse(null);
    }

    private static boolean isNumberingTypePartOfNumberingConfig(NumberingType numberingType, NumberingConfig numberingConfig, int depth) {
        if (numberingType != null && numberingConfig.getLevels() != null && !numberingConfig.getLevels().getLevels().isEmpty()) {
            return numberingConfig.getLevels().getLevels().stream().anyMatch(level -> level.getDepth() == depth && level.getNumberingType().equals(numberingType));
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
                    String numValue = sequence;
                    if (numberConfig.getPrefix() != null && numValue.startsWith(numberConfig.getPrefix())) {
                        numValue = numValue.substring(numberConfig.getPrefix().length());
                    }
                    if (numberConfig.getSuffix() != null && numValue.endsWith(numberConfig.getSuffix())) {
                        numValue = numValue.substring(0, numValue.length() - numberConfig.getSuffix().length());
                    }
                    return (numberConfig.getSequence() != null && numberConfig.getSequence().equalsIgnoreCase(numValue));
                })
                .findFirst()
                .orElse(null);
        if (numberingConfig == null) {
            return null;
        }
        return numberingConfig.getType();
    }

    public static TocItem getTocItemByNumValue(List<NumberingConfig> numberingConfigs, List<TocItem> foundTocItems, String numValue) {
        if (numValue == null || numValue.equals(HASH_NUM_VALUE)) {
            return getTocItemWithNumberedNumberingConfig(numberingConfigs, foundTocItems);
        }

        NumberingType numberingTypeMatchingSequence = getNumberingTypeBySequence(numberingConfigs, numValue);
        List<TocItem> tocItemsWithMatchingNumberingConfig = foundTocItems.stream()
                .filter(tocItem -> {
                    NumberingConfig tocItemNumberingConfig = getNumberingConfig(numberingConfigs, tocItem.getNumberingType());
                    return (isNumberingTypeMatchesSequence(numberingConfigs, tocItem.getNumberingType(), numValue)
                            || isNumberingTypePartOfNumberingConfig(numberingTypeMatchingSequence, tocItemNumberingConfig));
                })
                .collect(Collectors.toList());
        if (tocItemsWithMatchingNumberingConfig.isEmpty()) {
            return getTocItemWithNumberedNumberingConfig(numberingConfigs, foundTocItems);
        } else if (tocItemsWithMatchingNumberingConfig.size() == 1) {
            return tocItemsWithMatchingNumberingConfig.get(0);
        } else {
            return tocItemsWithMatchingNumberingConfig.stream()
                    .filter(tocItem -> isNumberingTypeMatchesSequence(numberingConfigs, tocItem.getNumberingType(), numValue))
                    .findFirst().orElseGet(() -> getTocItemWithNumberedNumberingConfig(numberingConfigs, foundTocItems));
        }
    }

    public static TocItem getTocItemWithNumberedNumberingConfig(List<NumberingConfig> numberingConfigs, List<TocItem> foundTocItems) {
        return foundTocItems.stream()
                .filter(tocItem -> {
                    NumberingConfig tocItemNumberingConfig = getNumberingConfig(numberingConfigs, tocItem.getNumberingType());
                    return tocItemNumberingConfig.isNumbered();
                })
                .findFirst().orElse(null);
    }

    private static boolean isNumberingTypePartOfNumberingConfig(NumberingType numberingType, NumberingConfig numberingConfig) {
        if (numberingType != null && numberingConfig.getLevels() != null && !numberingConfig.getLevels().getLevels().isEmpty()) {
            return numberingConfig.getLevels().getLevels().stream().anyMatch(level -> level.getNumberingType().equals(numberingType));
        }
        return false;
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
