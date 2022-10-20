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

package eu.europa.ec.leos.services.label.ref;

import eu.europa.ec.leos.services.support.XercesUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.XmlHelper.INDENT;
import static eu.europa.ec.leos.services.support.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.XmlHelper.PARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.POINT;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPARAGRAPH;
import static eu.europa.ec.leos.services.support.XmlHelper.SUBPOINT_LABEL;
import static eu.europa.ec.leos.services.label.ref.NumFormatter.isUnnumbered;

/**
 * This rule has the lowest priority so it will be fire for all nodes.
 * In computation time we check if is an numbered or unnumbered tree.
 *
 * The build label will be as follows for numbered and unnumbered respectively:
 * - Article 1(1), point (a)(1)(i), first indent
 * - Article 1, firth paragraph, point (a)(1)(i), first indent
 *
 * The class use a buffer to group by the nodes for later building the label. The buffer looks like this:
 *      article      - 2
 *      paragraph    - first
 *      subparagraph -
 *      point        - (a)(i)(1)
 *      indent       - first
 *
 * The class build the label in 3 steps
 *      1. Create the anchors for the selected nodes and add it to the buffer.
 *      2. Enreach the buffer with ancestors for the selected node.
 *      3. print the label.
 */
@Component
public class LabelArticleElementsOnly extends LabelHandler {
    
    private final List<String> excludeUnnumbered = new ArrayList<>(Arrays.asList(INDENT, SUBPARAGRAPH, SUBPOINT_LABEL));
    private static final String ANNEX_FILE_PREFIX = "annex_";

    @Override
    public boolean canProcess(List<TreeNode> refs) {
        return true;
    }

    @Override
    public void addPreffix(StringBuffer label, String docType, List<TreeNode> refs) {
        TreeNode firstReference = refs.stream()
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Selected Internal References cannot be empty"));
        if (firstReference.getDocumentRef() != null
                && firstReference.getDocumentRef().startsWith(ANNEX_FILE_PREFIX)
                && !StringUtils.isEmpty(docType)) {
            label.append(docType);
            label.append(", ");
        }
    }

    /**
     * @param refs List with nodes to be used as reference
     * @param mrefCommonNodes List with ancestor nodes for each ref element, in common with the actual node. Example:
     *                        If you are in Article1->paragraph1 and try to reference Article1->paragraph1->Point1
     *                        The list will be {Article1, paragraph1}.
     * @param label The label to be build for later shown to the user.
     * @param locale
     * @param withAnchor true if the label should be a html anchor for navigation purpose
     * @param capital The variable capital is only used in LabelArticleElementsOnly
     * @return return true if the actual rule computed the change to the label, false otherwise to break and let other rules handle.
     */
    @Override
    public void process(List<TreeNode> refs, List<TreeNode> mrefCommonNodes, TreeNode sourceNode, StringBuffer label, Locale locale, boolean withAnchor,
                        boolean capital) {
        TreeNode ref = refs.get(0); //first from the selected nodes
        String refType = ref.getType();//node type of the first selected node
        String documentRef = ref.getDocumentRef();
        Map<String, LabelKey> bufferLabels = new LinkedHashMap<>(); //util buffer to group by the numbers by element

        int number = 1;
        if (refs.size() > 1) {
            number = 0;
        }

        // 1. add selected node in the buffer
        if (showThisLabel(refs, mrefCommonNodes, sourceNode)) {
            bufferLabels.put(ref.getType(), new LabelKey(getPresentationType(ref), THIS_REF, true, documentRef));
        } else {
            StringBuilder sb = createAllAnchors(refs, locale, withAnchor);
            bufferLabels.put(ref.getType(), new LabelKey(getPresentationType(ref), sb.toString(), isUnnumbered(ref), documentRef));
        }

        //2. add rest of nodes, starting from the leaf, going up to parents until it reach Article
        while (!ARTICLE.equals(ref.getType()) && ref.getParent()!= null) {
            ref = ref.getParent();
            processOtherNodesLabel(bufferLabels, ref, mrefCommonNodes.contains(ref), locale);
        }

        // 3. build the label based on the bufferLabels
        List<String> listLabels = new ArrayList<>();
        List<String> reverseOrderedKeys = new ArrayList<>(bufferLabels.keySet());
        Collections.reverse(reverseOrderedKeys);
        /**
         * The order of the words depends if is numbered, unnumbered or the word "this". Ex:
         * - Point (1)
         * - first indent
         * - this indent
         */
        boolean articleProcessed = false;
        String lastElementKeyOfReverseOrderedKeys = reverseOrderedKeys.get(reverseOrderedKeys.size()-1);
        int numberForReverseOrderedKeys = 1;
        for (String key : reverseOrderedKeys) {
            LabelKey val = bufferLabels.get(key);
            /* The number variable when has 0 is to do the plural
            *  But the plural is done only in the last word
            *  So, numberForReverseOrderedKeys will be 0 only for the last word
            */
            if (number == 0 && key.equals(lastElementKeyOfReverseOrderedKeys)) {
                numberForReverseOrderedKeys = 0;
            }
            if (val.getLabelNumber().equals(THIS_REF) && key.equals(refType)) {
                // we print "this" only for the clicked node
                listLabels.add(String.format("%s %s, ", val.getLabelNumber(), val.getLabelName()));
            } else if (!val.getLabelNumber().equals(THIS_REF) && !"".equals(val.getLabelNumber())) {
                // when we are here we are sure the node is not "this" nor with an empty number string.
                if(isUnumberedTree(refs.get(0))){
                    addLabel(listLabels, val, numberForReverseOrderedKeys, locale);
                } else {
                    if (key.equals(ARTICLE)) {
                        // keep track of processed article to avoid the word "paragraph"
                        articleProcessed = true;
                        listLabels.add(String.format("%s %s", val.getLabelName(), val.getLabelNumber()));
                    } else if (key.equals(PARAGRAPH) && articleProcessed) {
                        // in case of paragraph node we write the word "paragraph" only if the previous word "Article" is missing
                        // Otherwise we only show the number, example: Article 1(1)
                        listLabels.add(String.format("%s, ", val.getLabelNumber()));
                    } else {
                        // general nodes
                        addLabel(listLabels, val, numberForReverseOrderedKeys, locale);
                    }
                }
            }
        }

        label.append(String.join("", listLabels));
        if (capital) {
            Node node = XercesUtils.createXercesDocument(("<fakeNodeToReadTextContent>" + label + "</fakeNodeToReadTextContent>").getBytes(), false);
            String onlyText = XercesUtils.getContentByTagName(node, "fakeNodeToReadTextContent");
            String firstWord = onlyText.split(" ")[0].replaceAll(",", "");
            int firstWordPosition = label.indexOf(firstWord);
            label.setCharAt(firstWordPosition, label.substring(firstWordPosition, firstWordPosition+1).toUpperCase(Locale.ROOT).charAt(0));
        }
        if(label.length() > 1 && label.substring(label.length()-2, label.length()).equals(", ")){
            label.delete(label.length()-2, label.length());
        }
    }

    private String getPresentationType(TreeNode ref) {
        return ref.getType().equals(LEVEL) ? POINT : ref.getType();
    }

    /**
     * If is an unnumbered node we print first the number(in letters) then the label.
     * If is a numbered one, first the label than the number.
     *  - first paragraph   (unnumbered)
     *  - paragraph (1)     (numbered)
     */
    private void addLabel(List<String> listLabels, LabelKey val, int number, Locale locale) {
        if (val.isUnNumbered()) {
            listLabels.add(String.format("%s %s, ", val.getLabelNumber(), NumFormatter.formatPlural(val.getLabelName(), number, locale)));
        } else {
            listLabels.add(String.format("%s %s, ", NumFormatter.formatPlural(val.getLabelName(), number, locale), val.getLabelNumber()));
        }
    }

    /**
     * If the tree is a numbered or unnumbered paragraph structure.
     * INDENT, SUBPARAGRAPH and SUBPOINT can not determine if the tree is numbered or not so we exclude them in the
     * recursive check.
     * @param ref TreeNode tree to be checked if is numbered or unnumbered
     */
    private boolean isUnumberedTree(TreeNode ref) {
        if (excludeUnnumbered.contains(ref.getType())) {
            if (!NumFormatter.anyUnnumberedParent(ref.getParent())) {
                return false;
            }
        } else {
            if (!NumFormatter.anyUnnumberedParent(ref)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if:
     * - clicked note is a leaf
     * - only if one node is selected. If we choose two nodes even if one of them is in common we should print numbered/unnumbered text.
     * - if source is on the indent, we should print "this" only when clicking on the parent point, but not in other ancestors.
     * @param refs all clicked nodes
     * @param mrefCommonNodes common nodes in the tree from akomantoso tag until the clicked node
     * @param sourceNode source node, where the cursor was before clicking the new node to refer to
     */
    private boolean showThisLabel(List<TreeNode> refs, List<TreeNode> mrefCommonNodes, TreeNode sourceNode) {
        final TreeNode ref = refs.get(0);
        return ref.getChildren().isEmpty()
                && refs.size() == 1
                && mrefCommonNodes.indexOf(ref) != -1
                && (sourceNode.getDepth() - 1 <= ref.getDepth());
    }


    /**
     * Collect all the ref anchors
     * @param refs all selected nodes
     * @param withAnchor true if the label should be a html anchor for navigation purpose
     * @return A list with anchors of selected nodes, example: <ref href="1">1</ref>, <ref href="2">2</ref> and <ref href="3">3</ref>.
     */
    private StringBuilder createAllAnchors(List<TreeNode> refs, Locale locale, boolean withAnchor) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < refs.size(); i++) {
            if (i != 0 && i == refs.size() - 1) {
                sb.append(" and ");
            } else if (i > 0) {
                sb.append(", ");
            }
            sb.append(createAnchor(refs.get(i), locale, withAnchor));
        }
        return sb;
    }

    private void processOtherNodesLabel(Map<String, LabelKey> buffers, TreeNode ref, boolean isThis, Locale locale) {
        // keep the old value if is sameType as the child. Last iterated parent will add the element name.
        // In the end of iteration will have something like: Point (a)(1)(i)(ii)
        String oldnum = "";
        if (ref.getChildren().get(0).getType().equals(ref.getType())) {
            if (buffers.get(ref.getType()) != null) {
                oldnum = buffers.get(ref.getType()).getLabelNumber();
            }
        }

        String labelName;
        String labelNumber;
        if (isThis) {
            labelName = ref.getType();
            labelNumber = oldnum;
        } else {
            if (ref.getType().equals(ARTICLE)) {
                labelName = StringUtils.capitalize(getPresentationType(ref));
            } else {
                labelName = getPresentationType(ref);
            }
            labelNumber = NumFormatter.formattedNum(ref, locale) + oldnum;
        }

        buffers.put(ref.getType(), new LabelKey(labelName, labelNumber, isUnnumbered(ref), ref.getDocumentRef()));
    }

    @Override
    public int getOrder() {
        return 6;
    }
}
