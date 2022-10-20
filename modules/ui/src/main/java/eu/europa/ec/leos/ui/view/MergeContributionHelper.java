package eu.europa.ec.leos.ui.view;

import com.google.common.eventbus.EventBus;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.services.document.ContributionService;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.ui.event.contribution.ApplyContributionsRequestEvent;
import eu.europa.ec.leos.ui.event.contribution.CompareAndShowRevisionEvent;
import eu.europa.ec.leos.ui.event.contribution.MergeActionRequestEvent.MergeAction;
import eu.europa.ec.leos.services.clone.InternalRefMap;
import eu.europa.ec.leos.util.LeosDomainUtil;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.web.event.NotificationEvent;
import eu.europa.ec.leos.web.model.MergeActionVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_MERGE_ACTION_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_ACTION_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_ACTION_ROOT_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_DATE_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_MOVED_LABEL_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_MOVE_FROM;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_MOVE_TO;
import static eu.europa.ec.leos.services.support.XmlHelper.LEOS_SOFT_USER_ATTR;
import static eu.europa.ec.leos.services.support.XmlHelper.MOVE_FROM;
import static eu.europa.ec.leos.services.support.XmlHelper.MOVE_TO;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_DELETE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.services.support.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;
import static eu.europa.ec.leos.ui.event.contribution.MergeActionRequestEvent.ElementState;

@Component
@Scope("prototype")
public class MergeContributionHelper {

    private static final Logger LOG = LoggerFactory.getLogger(MergeContributionHelper.class);

    private final XPathCatalog xPathCatalog;
    private final XmlContentProcessor xmlContentProcessor;
    private final MessageHelper messageHelper;
    private final ContributionService contributionService;
    protected final EventBus eventBus;

    public MergeContributionHelper(XPathCatalog xPathCatalog, XmlContentProcessor xmlContentProcessor, MessageHelper messageHelper,
                                   ContributionService contributionService, EventBus eventBus) {
        this.xPathCatalog = xPathCatalog;
        this.xmlContentProcessor = xmlContentProcessor;
        this.messageHelper = messageHelper;
        this.contributionService = contributionService;
        this.eventBus = eventBus;
    }

    public byte[] updateDocumentWithContributions(ApplyContributionsRequestEvent event, XmlDocument xmlDocument, List<TocItem> tocItemList, List<InternalRefMap> intRefMap) throws IOException {

        byte[] xmlContent = xmlDocument.getContent().get().getSource().getBytes();
        byte[] contributionXmlContent = null;
        ContributionVO contributionVO = null;
        // Sort the merge actions so that move actions are done first
        List<MergeActionVO> sortedEvents = event.getMergeActionVOS().stream().
                sorted(Collections.reverseOrder(Comparator.comparing((MergeActionVO m) -> m.getElementState().getState()))).collect(Collectors.toList());
        List<String> movedElementIds = new ArrayList<>();
        for (MergeActionVO mergeActionVO : sortedEvents ) {
            boolean isMovedElementChild = checkIfIsMovedElementChild(movedElementIds, mergeActionVO);
            if (MergeAction.ACCEPT.equals(mergeActionVO.getAction())) {
                if (ElementState.MOVE.equals(mergeActionVO.getElementState())) {
                    movedElementIds.add(mergeActionVO.getElementId());
                    xmlContent = acceptMove(xmlContent, mergeActionVO, tocItemList);
                } else if (ElementState.DELETE.equals(mergeActionVO.getElementState())) {
                    xmlContent = acceptDeletion(xmlContent, mergeActionVO);
                } else {
                    xmlContent = acceptAddition(xmlContent, mergeActionVO, tocItemList, intRefMap);
                }
            } else if (MergeAction.UNDO.equals(mergeActionVO.getAction())) {
                String contributionFragment = xmlContentProcessor.getElementById(mergeActionVO.getContributionVO().getXmlContent(), mergeActionVO.getElementId()).getElementFragment();
                boolean isUndoReject = contributionFragment.substring(0, contributionFragment.indexOf(">")).contains("leos:mergeAction=\"rejected\"");
                if (!isUndoReject) {
                    if (ElementState.MOVE.equals(mergeActionVO.getElementState())) {
                        movedElementIds.add(mergeActionVO.getElementId());
                        xmlContent = undoMove(xmlContent, mergeActionVO, tocItemList);
                    } else if (ElementState.DELETE.equals(mergeActionVO.getElementState())) {
                        xmlContent = acceptAddition(xmlContent, mergeActionVO, tocItemList, intRefMap);
                    } else if (ElementState.ADD.equals(mergeActionVO.getElementState())) {
                        xmlContent = xmlContentProcessor.removeElementById(xmlContent, mergeActionVO.getElementId());
                    } else if (ElementState.CONTENT_CHANGE.equals(mergeActionVO.getElementState())) {
                        xmlContent = undoContentChange(xmlContent, mergeActionVO, tocItemList, intRefMap);
                    }
                }
            } else if (MergeAction.REJECT.equals(mergeActionVO.getAction()) && isMovedElementChild) {
                if (ElementState.ADD.equals(mergeActionVO.getElementState())) {
                    xmlContent = xmlContentProcessor.removeElementById(xmlContent, mergeActionVO.getElementId());
                } else if (ElementState.CONTENT_CHANGE.equals(mergeActionVO.getElementState())) {
                    xmlContent = undoContentChange(xmlContent, mergeActionVO, tocItemList, intRefMap);
                }
            }
            contributionXmlContent = executeContributionAction(mergeActionVO);
            mergeActionVO.getContributionVO().setXmlContent(contributionXmlContent);
            contributionVO =  mergeActionVO.getContributionVO();
        }
        eventBus.post(new CompareAndShowRevisionEvent(contributionVO));
        return xmlContent;
    }

    private boolean checkIfIsMovedElementChild(List<String> movedElementIds, MergeActionVO mergeActionVO) {
        for (String prefix : movedElementIds) {
            String elementId = getMovedOrDeletedElementId(mergeActionVO);
            elementId = elementId != null ? elementId : mergeActionVO.getElementId();
            if (elementId.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private byte[] undoContentChange(byte[] xmlContent, MergeActionVO mergeActionVO, List<TocItem> tocItemList, List<InternalRefMap> intRefMap) {
        byte[] contributionXMLContent = mergeActionVO.getContributionVO().getXmlContent();

        Element documentElement = xmlContentProcessor.getElementById(xmlContent, mergeActionVO.getElementId());
        String contributionElementFragment = revertContent(xmlContentProcessor.getElementById(contributionXMLContent, mergeActionVO.getElementId()).getElementFragment(), tocItemList);

        contributionElementFragment = updateInternalReferences(contributionElementFragment, intRefMap);
        if (documentElement != null) {
            xmlContent = xmlContentProcessor.replaceElementById(xmlContent, contributionElementFragment, mergeActionVO.getElementId());
        } else {
            eventBus.post(new NotificationEvent(NotificationEvent.Type.WARNING,
                    messageHelper.getMessage("contribution.merge.action.invalid.notification")));
        }
        return xmlContent;
    }

    private byte[] acceptMove(byte[] xmlContent, MergeActionVO mergeActionVO, List<TocItem> tocItemList) {
        String elementId = getMovedOrDeletedElementId(mergeActionVO);
        String addedFragment = xmlContentProcessor.getElementById(xmlContent, elementId).getElementFragment();
        xmlContent = acceptDeletion(xmlContent, mergeActionVO);
        xmlContent = acceptAddition(xmlContent, mergeActionVO, tocItemList, elementId, addedFragment);
        xmlContent = removeSoftMoveAttributes(xmlContent, mergeActionVO.getElementId());
        return xmlContent;
    }

    private byte[] undoMove(byte[] xmlContent, MergeActionVO mergeActionVO, List<TocItem> tocItemList) {
        String elementId = getMovedOrDeletedElementId(mergeActionVO);
        String addedFragment = xmlContentProcessor.getElementById(xmlContent, mergeActionVO.getElementId()).getElementFragment();
        xmlContent = acceptDeletion(xmlContent, mergeActionVO);
        xmlContent = acceptAddition(xmlContent, mergeActionVO, tocItemList,SOFT_MOVE_PLACEHOLDER_ID_PREFIX + mergeActionVO.getElementId(), addedFragment);
        xmlContent = removeSoftMoveAttributes(xmlContent, mergeActionVO.getElementId());
        return xmlContent;
    }

    private byte[] acceptAddition(byte[] xmlContent, MergeActionVO mergeActionVO, List<TocItem> tocItemList, List<InternalRefMap> intRefMap) {
        String addedFragment = xmlContentProcessor.getElementById(mergeActionVO.getContributionVO().getXmlContent(), mergeActionVO.getElementId()).getElementFragment();
        addedFragment = updateInternalReferences(addedFragment, intRefMap);
        return this.acceptAddition(xmlContent, mergeActionVO, tocItemList, mergeActionVO.getElementId(), addedFragment);
    }

    private String updateInternalReferences(String xmlContentStr, List<InternalRefMap> map) {
        for (InternalRefMap internalRefMap : map) {
            xmlContentStr = xmlContentStr.replaceAll(internalRefMap.getClonedRef(), internalRefMap.getRef());
        }
        return xmlContentStr;
    }

    private byte[] acceptAddition(byte[] xmlContent, MergeActionVO mergeActionVO, List<TocItem> tocItemList, String contributionElementId, String addedFragment) {
        byte[] contributionXMLContent = mergeActionVO.getContributionVO().getXmlContent();
        String tagName = mergeActionVO.getElementTagName();
        String elementId = mergeActionVO.getElementId();

        Element documentElement = xmlContentProcessor.getElementById(xmlContent, elementId);
        Element contributionPreviousSibling = xmlContentProcessor.getSiblingElement(contributionXMLContent, tagName, contributionElementId, Collections.emptyList(), true);
        Element contributionNextSibling = xmlContentProcessor.getSiblingElement(contributionXMLContent, tagName, contributionElementId, Collections.emptyList(), false);
        Element contributionParentElement = xmlContentProcessor.getParentElement(contributionXMLContent, contributionElementId);
        String contributionElementFragment = cleanAcceptedFragment(addedFragment, tocItemList);

        Element xmlPreviousSibling = contributionPreviousSibling != null ? xmlContentProcessor.getElementById(xmlContent, contributionPreviousSibling.getElementId()) : null;
        Element xmlNextSibling = contributionNextSibling != null ? xmlContentProcessor.getElementById(xmlContent, contributionNextSibling.getElementId()) : null;
        Element xmlParentSibling = contributionParentElement != null ? xmlContentProcessor.getElementById(xmlContent, contributionParentElement.getElementId()) : null;

        if (documentElement != null) {
            xmlContent = xmlContentProcessor.replaceElementById(xmlContent, contributionElementFragment, elementId);
        } else if (xmlPreviousSibling != null && xmlPreviousSibling.getElementId() != null) {
            xmlContent = xmlContentProcessor.insertElementByTagNameAndId(xmlContent,  contributionElementFragment, contributionPreviousSibling.getElementTagName(), contributionPreviousSibling.getElementId(),false);
        } else if (xmlNextSibling !=null && xmlNextSibling.getElementId() != null) {
            xmlContent = xmlContentProcessor.insertElementByTagNameAndId(xmlContent,  contributionElementFragment, contributionNextSibling.getElementTagName(), contributionNextSibling.getElementId(),true);
        } else if (xmlParentSibling != null && xmlParentSibling.getElementId() != null) {
            xmlContent = xmlContentProcessor.addChildToParent(xmlContent, contributionElementFragment, contributionParentElement.getElementId());
        } else {
            eventBus.post(new NotificationEvent(NotificationEvent.Type.WARNING, messageHelper.getMessage("contribution.merge.action.invalid.notification")));
        }
        return xmlContent;
    }

    private byte[] acceptDeletion(byte[] xmlContent, MergeActionVO mergeActionVO) {
        try {
            String elementId = getMovedOrDeletedElementId(mergeActionVO);
            xmlContent = xmlContentProcessor.removeElementById(xmlContent, elementId);
        } catch (Exception e) {
            LOG.debug("could not accept this action", e);
        }
        return xmlContent;
    }

    private byte[] undoAddition(byte[] xmlContent, MergeActionVO mergeActionVO) {
        try {
            String elementId = getMovedOrDeletedElementId(mergeActionVO);
            xmlContent = xmlContentProcessor.removeElementById(xmlContent, elementId);
        } catch (Exception e) {
            LOG.debug("could not accept this action", e);
        }
        return xmlContent;
    }

    private byte[] executeContributionAction(MergeActionVO mergeActionVO) throws IOException {
        String mergeActionMsg;
        String mergeActionNotificationKey = null;
        byte[] updatedXmlContent = mergeActionVO.getContributionVO().getXmlContent();
        boolean isMovedElement = checkForMovedElement(updatedXmlContent, mergeActionVO.getElementTagName(),
                mergeActionVO.getElementId());
        if (MergeAction.ACCEPT.equals(mergeActionVO.getAction())) {
            mergeActionMsg = "contribution.merge.accept.action.value";
            mergeActionNotificationKey = "contribution.merge.action.accepted.notification";
            updatedXmlContent = xmlContentProcessor.insertAttributeToElement(updatedXmlContent,
                    mergeActionVO.getElementTagName(), mergeActionVO.getElementId(),
                    LEOS_MERGE_ACTION_ATTR, messageHelper.getMessage(mergeActionMsg));
            
            if (ElementState.MOVE.equals(mergeActionVO.getElementState())) {
                updatedXmlContent = xmlContentProcessor.insertAttributeToElement(updatedXmlContent,
                        mergeActionVO.getElementTagName(), SOFT_MOVE_PLACEHOLDER_ID_PREFIX + mergeActionVO.getElementId(),
                        LEOS_MERGE_ACTION_ATTR, messageHelper.getMessage(mergeActionMsg));
            }
        } else if (MergeAction.REJECT.equals(mergeActionVO.getAction())) {
            mergeActionMsg = "contribution.merge.reject.action.value";
            mergeActionNotificationKey = "contribution.merge.action.rejected.notification";
            if (isMovedElement) {
                if (mergeActionVO.getElementId().startsWith(SOFT_MOVE_PLACEHOLDER_ID_PREFIX)) {
                    updatedXmlContent = xmlContentProcessor.insertAttributeToElement(mergeActionVO.getContributionVO().getXmlContent(),
                            mergeActionVO.getElementTagName(), mergeActionVO.getElementId().replaceAll(SOFT_MOVE_PLACEHOLDER_ID_PREFIX, ""),
                            LEOS_MERGE_ACTION_ATTR, messageHelper.getMessage(mergeActionMsg));
                } else {
                    updatedXmlContent = xmlContentProcessor.insertAttributeToElement(mergeActionVO.getContributionVO().getXmlContent(),
                            mergeActionVO.getElementTagName(), SOFT_MOVE_PLACEHOLDER_ID_PREFIX.concat(mergeActionVO.getElementId()),
                            LEOS_MERGE_ACTION_ATTR, messageHelper.getMessage(mergeActionMsg));
                }
            }
            updatedXmlContent = xmlContentProcessor.insertAttributeToElement(updatedXmlContent,
                    mergeActionVO.getElementTagName(), mergeActionVO.getElementId(),
                    LEOS_MERGE_ACTION_ATTR, messageHelper.getMessage(mergeActionMsg));
            //notification event

        } else if (MergeAction.UNDO.equals(mergeActionVO.getAction())) {
            mergeActionNotificationKey = "contribution.merge.action.undo.notification";
            if (isMovedElement) {
                if (mergeActionVO.getElementId().startsWith(SOFT_MOVE_PLACEHOLDER_ID_PREFIX)) {
                    updatedXmlContent = xmlContentProcessor.removeAttributeFromElement(updatedXmlContent,
                            mergeActionVO.getElementId().replaceAll(SOFT_MOVE_PLACEHOLDER_ID_PREFIX, ""), LEOS_MERGE_ACTION_ATTR);
                } else {
                    updatedXmlContent = xmlContentProcessor.removeAttributeFromElement(updatedXmlContent,
                            SOFT_MOVE_PLACEHOLDER_ID_PREFIX.concat(mergeActionVO.getElementId()), LEOS_MERGE_ACTION_ATTR);
                }
            }
            updatedXmlContent = xmlContentProcessor.removeAttributeFromElement(updatedXmlContent,
                    mergeActionVO.getElementId(), LEOS_MERGE_ACTION_ATTR);
        }
        contributionService.updateContributionMergeActions(mergeActionVO.getContributionVO().
                        getDocumentId(), mergeActionVO.getContributionVO().getLegFileName(), mergeActionVO.getContributionVO().getDocumentName(),
                updatedXmlContent);

        eventBus.post(new NotificationEvent(NotificationEvent.Type.INFO, mergeActionNotificationKey));
        return updatedXmlContent;
    }

    private boolean checkForMovedElement(byte[] xmlContent, String elementName, String elementId) {
        String attrVal = xmlContentProcessor.getElementAttributeValueByNameAndId(xmlContent, LEOS_SOFT_ACTION_ATTR,
                elementName, elementId);
        return (MOVE_TO.equalsIgnoreCase(attrVal) || MOVE_FROM.equalsIgnoreCase(attrVal));
    }

    private String cleanAcceptedFragment(String contributionFragment, List<TocItem> tocItemList) {
        contributionFragment = contributionFragment.replaceAll("<span class=\"leos-content-removed\".*?</span>", "").
                replaceAll("<content class=\"leos-content-removed\".*?</content>", "").
                replaceAll("class=\"leos-content-new\"", "").
                replaceAll("class=\"leos-content-removed\"", "").
                replaceAll("<span(?: [^>]*)?>", "").
                replaceAll("</span>", "").
                replaceAll("leos:mergeAction=\"accepted\"", "").
                replaceAll(SOFT_DELETE_PLACEHOLDER_ID_PREFIX, "");
        return cleanSoftAction(contributionFragment, tocItemList);
    }

    private String revertContent(String contributionFragment, List<TocItem> tocItemList) {
        contributionFragment = contributionFragment.replaceAll("<span class=\"leos-content-new\".*?</span>", "").
                replaceAll("class=\"leos-content-removed\"", "").
                replaceAll("class=\"leos-content-new\"", "").
                replaceAll("<span(?: [^>]*)?>", "").
                replaceAll("</span>", "").
                replaceAll("leos:mergeAction=\"accepted\"", "").
                replaceAll(SOFT_DELETE_PLACEHOLDER_ID_PREFIX, "");
        return cleanSoftAction(contributionFragment, tocItemList);
    }

    private String cleanSoftAction(String contributionFragment, List<TocItem> tocItemList) {
        contributionFragment = LeosDomainUtil.wrapXmlFragment(contributionFragment);
        byte[] cleanedFragment = xmlContentProcessor.cleanSoftActionsForNode(
                contributionFragment.getBytes(StandardCharsets.UTF_8), tocItemList);
        contributionFragment = new String(cleanedFragment, StandardCharsets.UTF_8);
        contributionFragment = LeosDomainUtil.unWrapXmlFragment(contributionFragment);
        return contributionFragment;
    }

    private String getMovedOrDeletedElementId(MergeActionVO mergeActionVO) {
        String elementId = null;
        if (ElementState.MOVE.equals(mergeActionVO.getElementState())) {
            elementId =  StringUtils.difference(SOFT_MOVE_PLACEHOLDER_ID_PREFIX, mergeActionVO.getElementId());
        } else if ((ElementState.DELETE.equals(mergeActionVO.getElementState()))) {
            elementId = StringUtils.difference(SOFT_DELETE_PLACEHOLDER_ID_PREFIX, mergeActionVO.getElementId());
        }
        return elementId;
    }

    private byte[] removeSoftMoveAttributes(byte[] xmlContent, String elementId) {
        xmlContent = xmlContentProcessor.removeAttributeFromElement(xmlContent, elementId, LEOS_SOFT_ACTION_ATTR);
        xmlContent = xmlContentProcessor.removeAttributeFromElement(xmlContent, elementId, LEOS_SOFT_ACTION_ROOT_ATTR);
        xmlContent = xmlContentProcessor.removeAttributeFromElement(xmlContent, elementId, LEOS_SOFT_MOVED_LABEL_ATTR);
        xmlContent = xmlContentProcessor.removeAttributeFromElement(xmlContent, elementId, LEOS_SOFT_USER_ATTR);
        xmlContent = xmlContentProcessor.removeAttributeFromElement(xmlContent, elementId, LEOS_SOFT_DATE_ATTR);
        xmlContent = xmlContentProcessor.removeAttributeFromElement(xmlContent, elementId, LEOS_SOFT_MOVE_TO);
        xmlContent = xmlContentProcessor.removeAttributeFromElement(xmlContent, elementId, LEOS_SOFT_MOVE_FROM);
        return xmlContent;
    }

}
