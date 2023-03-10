/*
 * Copyright 2017 European Commission
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
package eu.europa.ec.leos.ui.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.europa.ec.leos.services.support.XmlHelper;
import eu.europa.ec.leos.vo.toc.Attribute;
import eu.europa.ec.leos.vo.toc.TocItemType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.JavaScriptFunction;

import elemental.json.JsonArray;
import elemental.json.JsonObject;
import eu.europa.ec.leos.domain.cmis.metadata.LeosMetadata;
import eu.europa.ec.leos.ui.event.CloseBrowserRequestEvent;
import eu.europa.ec.leos.ui.event.MergeElementRequestEvent;
import eu.europa.ec.leos.vo.toc.AlternateConfig;
import eu.europa.ec.leos.vo.toc.Level;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.NumberingType;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.vo.toc.StructureConfigUtils;
import eu.europa.ec.leos.web.event.view.document.CheckElementCoEditionEvent;
import eu.europa.ec.leos.web.event.view.document.CheckElementCoEditionEvent.Action;
import eu.europa.ec.leos.web.event.view.document.CloseElementEvent;
import eu.europa.ec.leos.web.event.view.document.DeleteElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.EditElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.EditElementResponseEvent;
import eu.europa.ec.leos.web.event.view.document.FetchCrossRefTocRequestEvent;
import eu.europa.ec.leos.web.event.view.document.FetchCrossRefTocResponseEvent;
import eu.europa.ec.leos.web.event.view.document.FetchElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.FetchElementResponseEvent;
import eu.europa.ec.leos.web.event.view.document.InsertElementRequestEvent;
import eu.europa.ec.leos.web.event.view.document.ReferenceLabelRequestEvent;
import eu.europa.ec.leos.web.event.view.document.ReferenceLabelResponseEvent;
import eu.europa.ec.leos.web.event.view.document.RefreshElementEvent;
import eu.europa.ec.leos.web.event.view.document.SaveElementRequestEvent;
import eu.europa.ec.leos.web.event.window.CloseElementEditorEvent;
import eu.europa.ec.leos.web.model.UserVO;
import eu.europa.ec.leos.web.support.LeosCacheToken;
import eu.europa.ec.leos.web.support.cfg.ConfigurationHelper;

import static eu.europa.ec.leos.services.support.XmlHelper.ARTICLE;

@JavaScript({"vaadin://../js/editor/leosEditorConnector.js" + LeosCacheToken.TOKEN})
public class LeosEditorExtension<T extends AbstractComponent> extends LeosJavaScriptExtension {

    private static final long serialVersionUID = -6952215433796274723L;
    
    private EventBus eventBus;

    public LeosEditorExtension(T target, EventBus eventBus, ConfigurationHelper cfgHelper, List<TocItem> tocItemList, 
            List<NumberingConfig> numberingConfigs, List<AlternateConfig> alternateConfigs, List<LeosMetadata> documents,
            String documentRef) {
        super();
        this.eventBus = eventBus;

        getState().isImplicitSaveEnabled = Boolean.valueOf(cfgHelper.getProperty("implicitSaveAndClose.enabled"));
        getState().isSpellCheckerEnabled = Boolean.valueOf(cfgHelper.getIntegrationProperty("leos.spell.checker.enabled"));
        getState().spellCheckerServiceUrl = cfgHelper.getIntegrationProperty("leos.spell.checker.service.url");
        getState().spellCheckerSourceUrl = cfgHelper.getIntegrationProperty("leos.spell.checker.source.url");
        getState().tocItemsJsonArray = toJsonString(tocItemList);
        getState().numberingConfigsJsonArray = toJsonString(numberingConfigs);
        getState().listNumberConfigJsonArray = toJsonString(StructureConfigUtils.getNumberingConfigsFromTocItem(numberingConfigs, tocItemList,
                XmlHelper.POINT));
        getState().articleTypesConfigJsonArray = toJsonString(getArticleTypesAttributes(tocItemList));
        getState().alternateConfigsJsonArray = toJsonString(alternateConfigs);
        getState().documentsMetadataJsonArray = toJsonString(documents);
        getState().documentRef = documentRef;

        registerServerSideAPI();
        extend(target);
    }

    protected void extend(T target) {
        super.extend(target);
        target.addStyleName("leos-editing-pane");
    }

    @Override
    public void attach() {
        super.attach();
        eventBus.register(this);
    }

    @Override
    public void detach() {
        eventBus.unregister(this);
        super.detach();
    }

    @Subscribe
    public void editElement(EditElementResponseEvent event) {
        LOG.trace("Editing element...");
        getState(false).user = new UserVO(event.getUser());
        getState(false).permissions = event.getPermissions();
        callFunction("editElement", event.getElementId(), event.getElementTagName(), event.getElementFragment(), 
                event.getDocType(), event.getInstanceType(), event.getAlternatives(),
                toJsonString(event.getLevelItemVO()), event.isCloneProposal());
    }

    @Subscribe
    public void refreshElement(RefreshElementEvent event) {
        LOG.trace("Refreshing element...");
        callFunction("refreshElement", event.getElementId(), event.getElementTagName(), event.getElementFragment());
    }

    @Subscribe
    public void receiveElement(FetchElementResponseEvent event) {
        LOG.trace("Receiving element...");
        callFunction("receiveElement", event.getElementId(), event.getElementTagName(), event.getElementFragment(), 
                event.getDocumentRef());
    }

    @Subscribe
    public void receiveToc(FetchCrossRefTocResponseEvent event) {
        LOG.trace("Receiving table of content...");
        callFunction("receiveToc", toJsonString(event.getTocAndAncestorsVO()));
    }

    @Subscribe
    public void receiveReferenceLabel(ReferenceLabelResponseEvent event) {
        LOG.trace("Receiving references...");
        callFunction("receiveRefLabel", event.getLabel().replaceAll("xml:id=", "id="), event.getDocumentRef());
    }

    @Subscribe
    public void closeElement(CloseElementEvent event) {
        LOG.trace("Closing element...");
        callFunction("closeElement");
    }

    @Override
    protected LeosEditorState getState() {
        return (LeosEditorState) super.getState();
    }

    @Override
    protected LeosEditorState getState(boolean markAsDirty) {
        return (LeosEditorState) super.getState(markAsDirty);
    }

    private void registerServerSideAPI() {
        addFunction("insertElementAction", new JavaScriptFunction() {
            @Override
            public void call(JsonArray arguments) {
                LOG.trace("Insert element action...");
                JsonObject data = arguments.get(0);
                String elementId = data.getString("elementId");
                String elementType = data.getString("elementType");
                String position = data.getString("position");
                try {
                    eventBus.post(
                            new InsertElementRequestEvent(elementId, elementType,
                                    InsertElementRequestEvent.POSITION.valueOf(StringUtils.upperCase(position))));
                } catch (Exception ex) {
                    LOG.error("Exception when inserting element!", ex);
                }
            }
        });
        addFunction("editElementAction", new JavaScriptFunction() {
            @Override
            public void call(JsonArray arguments) {
                LOG.trace("Edit element action...");
                JsonObject data = arguments.get(0);
                String elementId = data.getString("elementId");
                String elementType = data.getString("elementType");
                eventBus.post(new CheckElementCoEditionEvent(elementId, elementType, Action.EDIT, new EditElementRequestEvent(elementId, elementType)));
            }
        });
        addFunction("deleteElementAction", new JavaScriptFunction() {
            @Override
            public void call(JsonArray arguments) {
                LOG.trace("Delete element action...");
                JsonObject data = arguments.get(0);
                String elementId = data.getString("elementId");
                String elementType = data.getString("elementType");
                eventBus.post(new CheckElementCoEditionEvent(elementId, elementType, Action.DELETE, new DeleteElementRequestEvent(elementId, elementType)));
            }
        });
        addFunction("releaseElement", new JavaScriptFunction() {
            @Override
            public void call(JsonArray arguments) {
                LOG.trace("Releasing element...");
                JsonObject data = arguments.get(0);
                String elementId = data.getString("elementId");
                String elementType = data.getString("elementType");
                eventBus.post(new CloseElementEditorEvent(elementId, elementType));
            }
        });
        addFunction("saveElement", new JavaScriptFunction() {
            @Override
            public void call(JsonArray arguments) {
                LOG.trace("Saving element...");
                JsonObject data = arguments.get(0);
                String elementId = data.getString("elementId");
                String elementType = data.getString("elementType");
                String elementFragment = data.getString("elementFragment");
                boolean isSplit = data.getBoolean("isSplit");
                Validate.isTrue(elementFragment.contains(elementType), String.format("Element must contain %s", elementType));
                eventBus.post(new SaveElementRequestEvent(elementId, elementType, elementFragment, isSplit));
            }
        });
        addFunction("requestElement", new JavaScriptFunction() {
            @Override
            public void call(JsonArray arguments) {
                LOG.trace("Element request...");
                JsonObject data = arguments.get(0);
                String elementId = data.getString("elementId");
                String elementType = data.getString("elementType");
                String documentRef = data.getString("documentRef");
                eventBus.post(new FetchElementRequestEvent(elementId, elementType, documentRef));
            }
        });
        addFunction("requestToc", new JavaScriptFunction() {
            @Override
            public void call(JsonArray arguments) {
                LOG.trace("Toc request...");
                JsonObject data = arguments.get(0);
                JsonArray nodeIds = data.hasKey("elementIds") ? data.getArray("elementIds") : null;
                List<String> elementIds = new ArrayList<>();
                for (int index = 0; nodeIds != null && index < nodeIds.length(); index++) {
                    String hrefArr[] = nodeIds.getString(index).split("/");
                    if(hrefArr.length == 2) {
                        elementIds.add(hrefArr[1]);
                    } else {
                        elementIds.add(hrefArr[0]);
                    }
                }
                eventBus.post(new FetchCrossRefTocRequestEvent(elementIds)); // along with TOC, the ancester tree for elementId passed will be fetched
            }
        });
        addFunction("requestRefLabel", new JavaScriptFunction() {
            @Override
            public void call(JsonArray arguments) {
                LOG.trace("Reference Label request...");
                JsonObject data = arguments.get(0);
                String currentElementId = data.hasKey("currentEditPosition") ? data.getString("currentEditPosition") : null;
                JsonArray refs = data.hasKey("references") ? data.getArray("references") : null;
                String documentRef = data.hasKey("documentRef") ? data.getString("documentRef") : null;
                boolean capital = data.hasKey("capital") ? data.getBoolean("capital") : false;
                List<String> references = new ArrayList<>();
                for (int index = 0; refs != null && index < refs.length(); index++) {
                    references.add(refs.getString(index));
                }
                eventBus.post(new ReferenceLabelRequestEvent(references, currentElementId, documentRef, capital));
            }
        });
        addFunction("closeBrowser", arguments -> {
            LOG.trace("Close browser request...");
            eventBus.post(new CloseBrowserRequestEvent());
        });
        addFunction("mergeElement", arguments -> {
            LOG.trace("Merge element request...");
            JsonObject data = arguments.get(0);
            String elementId = data.getString("elementId");
            String elementType = data.getString("elementType");
            String elementFragment = data.getString("elementFragment");
            Validate.isTrue(elementFragment.contains(elementType), String.format("Element must contain %s", elementType));
            eventBus.post(new CheckElementCoEditionEvent(elementId, elementType, elementFragment, Action.MERGE,
                    new MergeElementRequestEvent(elementId, elementType, elementFragment)));
        });

    }

    private Map<String, Attribute> getArticleTypesAttributes(List<TocItem> tocItems) {
        Map<String, Attribute> articleTypesAttributes = new HashMap<>();
        List<TocItemType> tocItemTypes = StructureConfigUtils.getTocItemTypesByTagName(tocItems, ARTICLE);
        tocItemTypes.forEach(tocItemType -> {
            Attribute attribute = tocItemType.getAttribute();
            if (attribute == null) {
                attribute = new Attribute();
                attribute.setAttributeName("");
                attribute.setAttributeValue("");
            }
            articleTypesAttributes.put(tocItemType.getName().name(), attribute);
        });
        return articleTypesAttributes;
    }

    private String toJsonString(Object o) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return "null";
        }
    }

}
