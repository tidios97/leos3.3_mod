/*
 * Copyright 2019 European Commission
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.services.processor.content;

import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.model.user.User;
import eu.europa.ec.leos.services.numbering.depthBased.ClassToDepthType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.indent.IndentedItemType;
import org.w3c.dom.Node;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.support.XmlHelper.CN;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.getSoftUserAttribute;

public interface TableOfContentProcessor {

    static String getTagValueFromTocItemVo(TableOfContentItemVO tableOfContentItemVO) {
        return tableOfContentItemVO.getTocItem().getAknTag().value();
    }

    static Boolean checkIfParagraphNumberingIsToggled(TableOfContentItemVO tableOfContentItemVO) {
        if (PARAGRAPH.equals(tableOfContentItemVO.getTocItem().getAknTag().value())
                && tableOfContentItemVO.getParentItem().isNumberingToggled() != null) {
            return tableOfContentItemVO.getParentItem().isNumberingToggled();
        }
        return null;
    }

    List<TableOfContentItemVO> buildTableOfContent(String startingNode, byte[] xmlContent, TocMode mode);

    static void updateStyleClassOfTocItems(List<TableOfContentItemVO> list, String elementName) {
        List<TableOfContentItemVO> divisionTocItems = list.stream()
                .filter(tocItemVO -> tocItemVO.getTocItem().getAknTag().value().equals(elementName))
                .collect(Collectors.toList());

        ClassToDepthType currentStyleType = ClassToDepthType.ofDepth(1);
        for (int index = 0; index < divisionTocItems.size(); index++) {
            final TableOfContentItemVO division = divisionTocItems.get(index);
            if (index != 0 && division.getStyle() != null) {
                currentStyleType = ClassToDepthType.valueOf(division.getStyle().toUpperCase());
                final String previousStyle = divisionTocItems.get(index - 1).getStyle();
                ClassToDepthType previousStyleType = ClassToDepthType.valueOf(previousStyle.toUpperCase());

                int previousStyleValue = previousStyleType.getDepth();
                int currentStyleValue = currentStyleType.getDepth();
                if (currentStyleValue - previousStyleValue > 1) {
                    currentStyleType = ClassToDepthType.ofDepth(previousStyleValue + 1);
                }
            }
            division.setStyle(currentStyleType.name().toLowerCase());
        }
    }

    static void updateDepthOfTocItems(List<TableOfContentItemVO> list) {
        List<TableOfContentItemVO> tocItems = list.stream()
                .flatMap(l -> l.flattened())
                .filter(tocItemVO -> tocItemVO.getTocItem().getAknTag().value().equals(LEVEL))
                .collect(Collectors.toList());

        for (int index = 0; index < tocItems.size(); index++) {
            final TableOfContentItemVO item = tocItems.get(index);
            if (index != 0) {
                int previousDepth = tocItems.get(index - 1).getItemDepth();
                int depth = item.getItemDepth();
                if (depth - previousDepth > 1) {
                    depth = previousDepth + 1;
                }
                String numOrigin = item.getOriginNumAttr();
                if (numOrigin == null || CN.equals(numOrigin)) {
                    item.setItemDepth(depth);
                }
            }
        }
    }

    static void updateUserInfo(TableOfContentItemVO sourceItem, User user) {
        sourceItem.setSoftUserAttr(user != null ? getSoftUserAttribute(user) : null);
        sourceItem.setSoftDateAttr((GregorianCalendar)GregorianCalendar.getInstance());
    }

    static void resetUserInfo(TableOfContentItemVO sourceItem) {
        sourceItem.setSoftUserAttr(null);
        sourceItem.setSoftDateAttr(null);
    }

    boolean isFirstElement(TableOfContentItemVO tableOfContentItemVO, String elementName);
    boolean containsElement(TableOfContentItemVO tableOfContentItemVO, String elementName);
    void convertTocItemContent(TableOfContentItemVO item, TableOfContentItemVO subelement, IndentedItemType beforeIndentedType, IndentedItemType afterIndentedType, boolean restored);
    boolean containsInlineElement(TableOfContentItemVO item);
    void replaceContentFromTocItem(TableOfContentItemVO tocItem, String updatedContent);
    void setContentInNodeFromTocItem(TableOfContentItemVO tocItem, Node node);
}
