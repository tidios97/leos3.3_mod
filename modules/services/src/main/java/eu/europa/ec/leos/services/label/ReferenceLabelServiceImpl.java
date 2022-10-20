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
package eu.europa.ec.leos.services.label;

import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.domain.cmis.LeosPackage;
import eu.europa.ec.leos.domain.cmis.document.Annex;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.domain.common.ErrorCode;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.i18n.LanguageHelper;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.services.store.PackageService;
import eu.europa.ec.leos.services.store.WorkspaceService;
import eu.europa.ec.leos.services.label.ref.LabelHandler;
import eu.europa.ec.leos.services.label.ref.Ref;
import eu.europa.ec.leos.services.label.ref.TreeNode;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.support.XercesUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.w3c.dom.Node;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.support.XmlHelper.ANNEX;
import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_MOVE_FROM;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_MOVE_TO;
import static eu.europa.ec.leos.services.support.XercesUtils.createXercesDocument;
import static eu.europa.ec.leos.services.label.TreeHelper.createTree;
import static eu.europa.ec.leos.services.label.TreeHelper.findCommonAncestor;
import static eu.europa.ec.leos.services.label.TreeHelper.getLeaves;
import static eu.europa.ec.leos.services.support.XmlHelper.XML_SHOW_AS;

abstract class ReferenceLabelServiceImpl implements ReferenceLabelService {
    private static final Logger LOG = LoggerFactory.getLogger(ReferenceLabelServiceImpl.class);
    
    @Autowired
    private LanguageHelper languageHelper;

    @Autowired
    protected MessageHelper messageHelper;

    @Autowired
    protected List<LabelHandler> labelHandlers;

    @Autowired
    protected WorkspaceService workspaceService;

    @Autowired
    protected PackageService packageService;

    @Autowired
    protected ReferenceLabelService self;

    @Autowired
    protected XPathCatalog xPathCatalog;

    @PostConstruct
    public void postConstructInit() {
        labelHandlers.sort(Comparator.comparingInt(LabelHandler::getOrder));
    }

    /**
     * Generates the multi references label.
     * This method is used when you don't need real <code>html</code> ref, instead you want only the label.
     * Example of generated label: Article 1(1), point (a)(1)(i), second indent
     * Overloads: {@link #generateLabel(List, String, String, Node, Node, String, boolean, boolean)}
     *
     * @param refs              element ids selected by the user
     * @param sourceNode        source node
     * @return: returns the label
     */
    @Override
    public Result<String> generateLabel(List<Ref> refs, Node sourceNode){
        return generateLabel(refs, "", "", sourceNode, sourceNode, null, false, false);
    }
    
    @Override
    public Result<String> generateLabelStringRef(List<String> refsString, String sourceDocumentRef, byte[] sourceBytes){
        final List refs = buildRefs(refsString, sourceDocumentRef);
        return generateLabel(refs, createXercesDocument(sourceBytes));
    }
    
    /**
     * Generates the multi references label.
     * Uses <code>documentRef</code> of the <code>refs</code> list to fetch the target document from the repository.
     * Once getting the target data, overloads: {@link #generateLabel(List, String, String, Node, Node, String, boolean, boolean)}
     *
     * @param refs              element ids selected by the user
     * @param sourceDocumentRef document reference of the source document
     * @param sourceRefId       element id being edited
     * @param sourceNode        source node
     * @param capital The variable capital is only used in LabelArticleElementsOnly
     * @return: returns the label if multi ref is valid or an error code otherwise.
     */
    @Override
    public Result<String> generateLabel(List<Ref> refs, String sourceDocumentRef, String sourceRefId, Node sourceNode, boolean capital) {
        // fetch the target only if is different from the source
        Node targetNode = sourceNode;
        String targetDocumentType = "";
        try {
            Ref targetDocumentRef = refs.stream()
                .filter(ref -> !ref.getDocumentref().equals(sourceDocumentRef))
                .findFirst()
                .orElse(null);
            if (targetDocumentRef != null) {
                // Using interface for allowing spring AOP proxy caching.
                // These functions can be moved to another service ex. "ReferenceLabelCacheService" and then
                // not needed the use of "self" or let it here for simplicity
                targetNode = self.getTargetDocument(targetDocumentRef.getDocumentref());
                targetDocumentType = self.getTargetDocumentType(targetDocumentRef.getDocumentref(), targetNode);
            }
        } catch (Exception e) {
            LOG.warn("Error fetching target document. {}", e.getMessage());
            return new Result<>("", ErrorCode.DOCUMENT_REFERENCE_NOT_VALID);
        }
        return generateLabel(refs, sourceDocumentRef, sourceRefId, sourceNode, targetNode, targetDocumentType, true, capital);
    }

    @Override
    public Result<String> generateLabelStringRef(List<String> refsString, String sourceDocumentRef, String sourceRefId, byte[] sourceBytes) {
        final List refs = buildRefs(refsString, sourceDocumentRef);
        return generateLabel(refs, sourceDocumentRef, sourceRefId, createXercesDocument(sourceBytes), false);
    }
    
    /**
     * Generates the multi references label composed by the href for navigation purpose.
     * If the selected elements are located in different documents(source!=target), the algorithm add the suffix (@param targetDocType)
     * to the generated label.
     * Example of generated label: Article 1(1), point (a)(1)(i), <ref xml:id="" href="bill_ck67vo25e0004dc9682dmo307/art_1_6FvZwH">second</ref> indent
     *
     * @param refs              element ids selected by the user
     * @param sourceRefId       element id being edited
     * @param sourceDocumentRef document reference of the source document
     * @param sourceNode        source node
     * @param targetNode        target node
     * @param targetDocType     Prefix for the target document. (Ex: Annex, Regulation, Decision, etc)
     * @param withAnchor        true if the label should be a html anchor for navigation purpose
     *
     * @return: returns the label if multi ref is valid or an error code otherwise.
     */
    @Override
    public Result<String> generateLabel(List<Ref> refs, String sourceDocumentRef, String sourceRefId, Node sourceNode, Node targetNode, String targetDocType, boolean withAnchor,
                                        boolean capital) {
        try {
            TreeNode targetTree = createTree(targetNode, null, refs);
            List<TreeNode> targetNodes = getLeaves(targetTree);//in xml order

            //Validate
            boolean valid = RefValidator.validate(targetNodes, refs);
            //If invalid references, mark mref as broken and return
            if (!valid) {
                return new Result<>("", ErrorCode.DOCUMENT_REFERENCE_NOT_VALID);
            }

            TreeNode sourceTree = createTree(sourceNode, null, Arrays.asList(new Ref("", sourceRefId, sourceDocumentRef, null)));
            TreeNode sourceTreeNode = TreeHelper.find(sourceTree, TreeNode::getIdentifier, sourceRefId);

            List<TreeNode> commonNodes = findCommonAncestor(sourceNode, sourceRefId, sourceDocumentRef, targetTree);
            //If Valid, generate, Tree is already sorted
            return new Result<>(createLabel(targetNodes, commonNodes, sourceTreeNode, targetDocType, withAnchor, capital), null);
        } catch (IllegalStateException e) {
            return new Result<>("", ErrorCode.DOCUMENT_REFERENCE_NOT_VALID);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error generation Labels for sourceRefId: " + sourceRefId + " and references: " + refs, e);
        }
    }

    @Override
    public Result<String> generateLabelStringRef(List<String> refsString, String sourceDocumentRef, String sourceRefId, byte[] sourceBytes, String targetDocumentRef,
                                                 boolean capital) {
        final List<Ref> refs = buildRefs(refsString, targetDocumentRef);
        return generateLabel(refs, sourceDocumentRef, sourceRefId, createXercesDocument(sourceBytes), capital);
    }

    @Override
    public Result<String> generateSoftMoveLabel(Ref ref, String sourceRefId, Node sourceNode, String attr,
                                                String sourceDocumentRef) {
        Result<String> labelResult = generateLabel(Arrays.asList(ref), sourceNode);
        if (labelResult.isOk()) {
            String label = Jsoup.parse(labelResult.get()).text();
            labelResult = new Result<> (messageHelper.getMessage("softmove.prefix." + getDirection(attr), new String[] {label}) ,null);
        }
        return labelResult;
    }

    private String getDirection(String attr) {
        if (attr.equals(LEOS_SOFT_MOVE_FROM)) {
            return "from";
        }
        else if (attr.equals(LEOS_SOFT_MOVE_TO)) {
            return "to";
        }
        return "";
    }

    @Cacheable(value = "referenceLabelTargetDocumentCache")
    public Node getTargetDocument(String targetDocumentRef) {
        XmlDocument targetXmlDocument = workspaceService.findDocumentByRef(targetDocumentRef, XmlDocument.class);
        return createXercesDocument(targetXmlDocument.getContent().get().getSource().getBytes());
    }

    /**
     * Calculate the docType in case the source and the target are different documents.
     * Reads the docType from the XML. Options: Regulation, Decision, Directive, Annex, etc.
     * In case of an Annex, check if there is more than one annex, and in that case append the annex index.
     */
    @Cacheable(value = "referenceLabelTargetDocumentTypeCache", key = "#targetDocumentRef")
    public String getTargetDocumentType(String targetDocumentRef, Node node) {
        String docType = getDocType(node);
        if (ANNEX.equals(docType)) {
            final XmlDocument targetDocument = workspaceService.findDocumentByRef(targetDocumentRef, XmlDocument.class);
            final LeosPackage targetPackage = packageService.findPackageByDocumentId(targetDocument.getId());
            final List<XmlDocument> targetSiblings = packageService.findDocumentsByPackagePath(targetPackage.getPath(), XmlDocument.class, true);
            List<Annex> annexes = targetSiblings.stream()
                .filter(p -> p.getCategory() == LeosCategory.ANNEX)
                .map(p -> (Annex) p)
                .collect(Collectors.toList());
            boolean hasMoreThanOneAnnex = annexes.size() > 1;
            if (hasMoreThanOneAnnex) {
                Annex annex = annexes.stream()
                    .filter(p -> p.getMetadata().get().getRef().equals(targetDocument.getMetadata().get().getRef()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Annex not found by the ref " + targetDocument.getMetadata().get().getRef()));
                docType += " " + annex.getMetadata().get().getIndex();
            }
        }
        return docType;
    }

    private String getDocType(Node sourceNode) {
        Node node = XercesUtils.getFirstElementByXPath(sourceNode, xPathCatalog.getXPathDocType(), true);
        String docType = null;
        if (node != null) {
            docType = XercesUtils.getAttributeValue(node, XML_SHOW_AS);
        }
        return docType;
    }
    
    private String createLabel(List<TreeNode> refs, List<TreeNode> mrefCommonNodes, TreeNode sourceNode, String docType, boolean withAnchor,
                               boolean capital) {
        StringBuffer accumulator = new StringBuffer();
        for (LabelHandler rule : labelHandlers) {
            if(rule.canProcess(refs)) {
                rule.addPreffix(accumulator, docType, refs);
                rule.process(refs, mrefCommonNodes, sourceNode, accumulator, languageHelper.getCurrentLocale(), withAnchor, capital);
                break;
            }
        }
        return accumulator.toString();
    }
    
    private List<Ref> buildRefs(List<String> refs, String targetDocumentRef) {
        return refs
                .stream()
                .map(r -> {
                    String[] strs = r.split(",");
                    String refId;
                    String refHref;
                    if (strs.length == 0) {
                        throw new RuntimeException("references list is empty");
                    } else if (strs.length == 1) {
                        refId = "";
                        refHref = strs[0];
                    } else {
                        refId = strs[0];
                        refHref = strs[1];
                    }
                    return new Ref(refId, refHref, targetDocumentRef, null);
                })
                .filter(ref -> !StringUtils.isEmpty(ref.getHref()))
                .collect(Collectors.toList());
    }
    

    static class RefValidator {
        private static final List<BiFunction<List<TreeNode>, List<Ref>, Boolean>> validationRules = new ArrayList<>();

        static {
            validationRules.add(RefValidator::checkEmpty);
            validationRules.add(RefValidator::checkSameParentAndSameType);
            validationRules.add(RefValidator::checkBrokenRefs);
        }

        static boolean validate(List<TreeNode> refs, List<Ref> oldRefs) {
            for (BiFunction<List<TreeNode>, List<Ref>, Boolean> rule : validationRules) {
                if (!rule.apply(refs, oldRefs)) {
                    return false;
                }
            }
            return true;
        }

        private static boolean checkBrokenRefs(List<TreeNode> refs, List<Ref> oldRefs) {
            return (refs.size() == oldRefs.size());
        }

        private static boolean checkEmpty(List<TreeNode> refs, List<Ref> oldRefs) {
            return !refs.isEmpty();
        }

        private static boolean checkSameParentAndSameType(List<TreeNode> refs, List<Ref> oldRefs) {
            TreeNode parent = refs.get(0).getParent();
            String type = refs.get(0).getType();
            int depth = refs.get(0).getDepth();

            for (int i = 1; i < refs.size(); i++) {
                TreeNode ref= refs.get(i);
                if (!ref.getParent().equals(parent)
                        && !(ref.getType().equals(type) && (ref.getDepth() == depth || type.equalsIgnoreCase(ARTICLE)))) {
                    return false;
                }
            }
            return true;
        }
    }
}
