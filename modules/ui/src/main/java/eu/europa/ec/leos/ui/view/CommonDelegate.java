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
package eu.europa.ec.leos.ui.view;

import com.google.common.eventbus.EventBus;
import elemental.json.Json;
import elemental.json.JsonObject;
import eu.europa.ec.leos.domain.cmis.Content;
import eu.europa.ec.leos.domain.cmis.document.LeosDocument;
import eu.europa.ec.leos.domain.cmis.document.XmlDocument;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.services.processor.ElementProcessor;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import eu.europa.ec.leos.web.event.view.document.DocumentUpdatedEvent;
import eu.europa.ec.leos.web.event.view.document.MergeSuggestionRequest;
import eu.europa.ec.leos.web.event.view.document.MergeSuggestionResponse;
import eu.europa.ec.leos.web.event.view.document.MergeSuggestionsRequest;
import eu.europa.ec.leos.web.event.view.document.MergeSuggestionsResponse;
import eu.europa.ec.leos.web.event.view.document.RefreshDocumentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class CommonDelegate<T extends XmlDocument> {

    private static final Logger LOG = LoggerFactory.getLogger(CommonDelegate.class);
    private EventBus eventBus;
    private MessageHelper messageHelper;
    byte[] resultXmlContent;
    private XmlContentProcessor xmlContentProcessor;
    private ElementProcessor elementProcessor;

    @Autowired
    public CommonDelegate(XmlContentProcessor xmlContentProcessor, ElementProcessor elementProcessor,
                          EventBus eventBus, MessageHelper messageHelper) {
        this.eventBus = eventBus;
        this.messageHelper = messageHelper;
        this.xmlContentProcessor = xmlContentProcessor;
        this.elementProcessor = elementProcessor;
    }

    /*FIXME : this is unfinished refactoring idea to move common code from presenters.
    * It can not be applied to all presenters as updateFn has different interface for annexService and documentService*/
    public void mergeSuggestion(T document, MergeSuggestionRequest event, ElementProcessor<T> elementProcessor, TriFunction<T, byte[], String, T> updateDocumentFn) {
        byte[] resultXmlContent = elementProcessor.replaceTextInElement(document, event.getOrigText(), event.getNewText(), event.getElementId(), event.getStartOffset(), event.getEndOffset());
        if (resultXmlContent == null) {
            eventBus.post(new MergeSuggestionResponse(messageHelper.getMessage("document.merge.suggestion.failed"), MergeSuggestionResponse.Result.ERROR));
            return;
        }
        document = updateDocumentFn.apply(document, resultXmlContent, messageHelper.getMessage("operation.merge.suggestion"));
        if (document != null) {
            eventBus.post(new RefreshDocumentEvent());
            eventBus.post(new DocumentUpdatedEvent());
            eventBus.post(new MergeSuggestionResponse(messageHelper.getMessage("document.merge.suggestion.success"), MergeSuggestionResponse.Result.SUCCESS));
        } else {
            eventBus.post(new MergeSuggestionResponse(messageHelper.getMessage("document.merge.suggestion.failed"), MergeSuggestionResponse.Result.ERROR));
        }
    }

    public void mergeSuggestions(T document, MergeSuggestionsRequest event, ElementProcessor<T> elementProcessor, TriFunction<T, byte[], String, T> updateDocumentFn) {

        List<MergeSuggestionRequest> listOfSuggestions = event.getListOfSuggestion();
        List<JsonObject> suggestionsResponseList = new ArrayList<>();
        resultXmlContent = getContent(document);

        listOfSuggestions.forEach(suggestionMergeEvent -> {
            byte[] lastEditedContent = xmlContentProcessor.replaceTextInElement(resultXmlContent, suggestionMergeEvent.getOrigText(), suggestionMergeEvent.getNewText(), suggestionMergeEvent.getElementId(), suggestionMergeEvent.getStartOffset(), suggestionMergeEvent.getEndOffset());
            JsonObject suggestion = Json.createObject();
            suggestion.put("origText", suggestionMergeEvent.getOrigText());
            suggestion.put("newText", suggestionMergeEvent.getNewText());
            suggestion.put("elementId", suggestionMergeEvent.getElementId());
            suggestion.put("startOffset", suggestionMergeEvent.getStartOffset());
            suggestion.put("endOffset", suggestionMergeEvent.getEndOffset());
            if (lastEditedContent != null) {
                suggestion.put("result", "SUCCESS");
                resultXmlContent = lastEditedContent;
            } else {
                suggestion.put("result", "FAILURE");
            }
            suggestionsResponseList.add(suggestion);
        });
        LOG.info("Final update of document with suggestions");
        LeosDocument docuemnt = updateDocumentFn.apply(document, resultXmlContent, messageHelper.getMessage("operation.merge.bulk.suggestions"));
        if (docuemnt == null) {
            setResultToSuggestionsMergeResponse(suggestionsResponseList, "FAILURE");
        }
        eventBus.post(new RefreshDocumentEvent());
        eventBus.post(new DocumentUpdatedEvent());
        LOG.info("Send bulk suggestions merge response event");
        eventBus.post(new MergeSuggestionsResponse(suggestionsResponseList));
    }

    private byte[] getContent(T document) {
        final Content content = document.getContent().getOrError(() -> "Annex content is required!");
        return content.getSource().getBytes();
    }

    private void setResultToSuggestionsMergeResponse( List<JsonObject> suggestionsResponseList, String result){
        suggestionsResponseList.forEach(suggestionsResponse -> suggestionsResponse.put("result", result));
    }
}