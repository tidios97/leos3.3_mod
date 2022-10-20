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

import static eu.europa.ec.leos.services.support.XmlHelper.CN;

import java.util.ArrayList;
import java.util.List;

import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

public class TocVOCreateExplanatoryUtils {

    public static TableOfContentItemVO createUnnumberedParagraph(String id, int itemDepth) {
        return TocVOCreateUtils.createUnnumberedExplanatoryParagraph(id, itemDepth, CN);
    }

    public static TableOfContentItemVO createPoint(String id, int itemDepth) {
        return TocVOCreateUtils.createExplanatoryPoint(id, itemDepth, CN);
    }

}
