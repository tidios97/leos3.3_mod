/*
 * Copyright 2021 European Commission
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
package eu.europa.ec.leos.services.processor.content;

import eu.europa.ec.leos.services.util.TestUtils;
import org.junit.Test;

import static eu.europa.ec.leos.services.util.TestUtils.squeezeXml;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class XmlContentProcessorMandate_CleanSoftActionTest extends XmlContentProcessorProposalTest {

    private final static String FOLDER = "/cleanSoftActions/";

    /**
     * Article 1
     *   Point (a)
     *   Point (b)
     *        Point (i)
     *        Point (ii)
     *            Point (1)
     *            Point (2)
     *                first indent
     *                second indent
     * Article 2
     *    Point (a)
     *    Point (b)
     *        Point (i)
     *        Point (ii)  *
     *        Point (iii) *
     * Article 3  *
     *    Point (a)
     *    Point (b)
     * Part I
     *    Title I
     *        Article 4
     *        Chapter 1
     *            Section 1  *
     *                Article 5  *
     *                    Point (a)
     *                Article 6 *
     *            Article 7
     *            Article 8 *
     *            Article 9 *
     * Article 10 *
     *
     * Operations on CN instance.
     * 1) Delete Article 9                        (DELETE)
     * 2) Add point (a) and (i) inside Article 8  (TRANSFORM)
     * 3) Move section 1 on top                   (MOVE)
     * 4) Add article -1 (before Article 5)       (ADD)
     * 5) Moved Article 2(1), point(b)(iii), before Article 2(1), point(a)  (will become -a)  (MOVE)
     * 6) Delete Article 2(1), point(b)(ii)       (DELETE)
     * 7) Add point after Article 2(1), point(b)(iii)  (will become iiia) (ADD)
     * 8) Delete Article 10
     * 9) UnDelete Article 10  (UNDELETE)
     * 10) Delete Article 3    (DELETE)
     * 11) Delete Article 6 (DELETE, the only paragraph)
     * 12) Delete Article 5(Points) (DELETE list, subparagraph will remain without a list)
     */
    @Test
    public void test_bill_cleanSoftActions() {
        // Given
        final byte[] xmlContent = TestUtils.getFileContent(FOLDER,"test_bill_cleanSoftActions.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FOLDER,"test_bill_cleanSoftActions__expected.xml");

        // When
        final byte[] resolvedContent = xercesXmlContentProcessor.cleanSoftActions(xmlContent);

        // Then
        String result = squeezeXml(new String(resolvedContent));
        String expected = squeezeXml(new String(xmlExpected));
        assertThat(result, is(expected));
    }

    /**
     * Title I
     *   Level 1
     *      Point (a)
     *        Point (i)
     *        Point (ii)
     *            Point (1)
     *                first indent
     *                second indent
     *    Level 1.1
     *    Level 1.1.1
     *      Point (a)
     *      Point (b)
     *          Point (i)
     * Chapter #
     *    Level 1.2
     * Level 1.3
     * Level 1.3.1
     * Level 1.3.1.1
     * Level 2
     * Paragraph first
     * Paragraph second
     *
     * Operations on CN instance.
     * 1) Move Level 1.1.1, Point(b), before Point(a) (MOVE)
     * 2) Delete Paragraph first, than undelete (UNDELETE)
     * 3) Delete Paragraph second  (DELETE)
     * 4) Delete Level 1.3 (DELETE)
     * 5) Move Chapter/Level 1.2 before Title/Level 1 (will become -1) (MOVE)
     * 6) Delete Level 1 (will start 1.1) (DELETE)
     * 7) Add new Level on top (will be -2)  (ADD)
     */
    @Test
    public void test_annex_cleanSoftActions() {
        // Given
        final byte[] xmlContent = TestUtils.getFileContent(FOLDER,"test_annex_cleanSoftActions.xml");
        final byte[] xmlExpected = TestUtils.getFileContent(FOLDER,"test_annex_cleanSoftActions__expected.xml");

        // When
        final byte[] resolvedContent = xercesXmlContentProcessor.cleanSoftActions(xmlContent);

        // Then
        String result = squeezeXml(new String(resolvedContent));
        String expected = squeezeXml(new String(xmlExpected));
        assertThat(result, is(expected));
    }

}
