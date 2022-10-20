/*
 * Copyright 2020 European Commission
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
package integration.saveToc;

import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

import java.util.ArrayList;
import java.util.List;

import static eu.europa.ec.leos.services.support.XmlHelper.CN;

public class TocVOCreateMandateUtils {

    public static TableOfContentItemVO createPart(String id, int itemDepth) {
        return TocVOCreateUtils.createPart(id, itemDepth, CN);
    }

    public static TableOfContentItemVO createTitle(String id, int itemDepth) {
        return TocVOCreateUtils.createTitle(id, itemDepth, CN);
    }

    public static TableOfContentItemVO createChapter(String id, int itemDepth) {
        return TocVOCreateUtils.createChapter(id, itemDepth, CN);
    }

    public static TableOfContentItemVO createSection(String id, int itemDepth) {
        return TocVOCreateUtils.createSection(id, itemDepth, CN);
    }

    public static TableOfContentItemVO createArticle(String id, int itemDepth) {
        return TocVOCreateUtils.createArticle(id, itemDepth, CN);
    }

    public static TableOfContentItemVO createParagraph(String id, int itemDepth) {
        return TocVOCreateUtils.createParagraph(id, itemDepth, CN);
    }

    public static List<TableOfContentItemVO> createParagraphs(String id, int nrParagraphs, int itemDepth) {
        List<TableOfContentItemVO> pars = new ArrayList<>();
        for (int i = 0; i < nrParagraphs; i++) {
            pars.add(createParagraph(id + 1, itemDepth));
        }
        return pars;
    }

    public static TableOfContentItemVO createSubParagraph(String id, int itemDepth) {
        return TocVOCreateUtils.createSubParagraph(id, itemDepth, CN);
    }

    public static TableOfContentItemVO createPoint(String id, int itemDepth) {
        return TocVOCreateUtils.createPoint(id, itemDepth, CN);
    }

    public static TableOfContentItemVO createLevel(String id, int itemDepth) {
        return TocVOCreateUtils.createLevel(id, itemDepth, CN);
    }

    public static TableOfContentItemVO createMoveToElement(TableOfContentItemVO originalElement) {
        return TocVOCreateUtils.createMoveToElement(originalElement);
    }

    public static TableOfContentItemVO createMoveToPoint(TableOfContentItemVO originalElement) {
        return TocVOCreateUtils.createMoveToPoint(originalElement);
    }

    public static TableOfContentItemVO restorePointToPreviousPosition(TableOfContentItemVO moveFrom, TableOfContentItemVO moveTo) {
        return TocVOCreateUtils.restorePointToPreviousPosition(moveFrom, moveTo);
    }

    public static TableOfContentItemVO createMoveFromElement(TableOfContentItemVO originalElement) {
        return TocVOCreateUtils.createMoveFromElement(originalElement, CN);
    }

}
