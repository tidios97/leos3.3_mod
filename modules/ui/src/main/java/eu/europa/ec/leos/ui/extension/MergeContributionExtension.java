package eu.europa.ec.leos.ui.extension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.JavaScriptFunction;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import eu.europa.ec.leos.model.action.ContributionVO;
import eu.europa.ec.leos.ui.event.contribution.ApplyContributionsRequestEvent;
import eu.europa.ec.leos.ui.event.contribution.ContributionSelectionEvent;
import eu.europa.ec.leos.ui.event.contribution.FetchContributionsListEvent;
import eu.europa.ec.leos.web.event.view.document.TocItemListRequestEvent;
import eu.europa.ec.leos.web.event.view.document.TocItemListResponseEvent;
import eu.europa.ec.leos.web.event.view.document.RefreshContributionEvent;
import eu.europa.ec.leos.web.model.MergeActionVO;
import eu.europa.ec.leos.web.support.LeosCacheToken;

import java.util.ArrayList;
import java.util.List;

import static eu.europa.ec.leos.ui.event.contribution.MergeActionRequestEvent.MergeAction;
import static eu.europa.ec.leos.ui.event.contribution.MergeActionRequestEvent.ElementState;

@JavaScript({"vaadin://../js/ui/extension/mergeContributionConnector.js" + LeosCacheToken.TOKEN })
public class MergeContributionExtension<T extends AbstractField<V>, V> extends LeosJavaScriptExtension {

    private static final long serialVersionUID = 1L;
    private EventBus eventBus;
    private ContributionVO contributionVO;
    private boolean selectAll = false;

    public MergeContributionExtension(T target, EventBus eventBus) {
        super();
        this.eventBus = eventBus;
        extend(target);
        registerServerSideAPI();
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

    protected void extend(T target) {
        super.extend(target);
        // handle target's value change
        target.addValueChangeListener(event -> {
            LOG.trace("Target's value changed...");
            // Mark that this connector's state might have changed.
            // There is no need to send new data to the client-side,
            // since we just want to trigger a state change event...
            forceDirty();
        });
    }

    @Override
    protected MergeContributionState getState() {
        return (MergeContributionState) super.getState();
    }

    @Override
    protected MergeContributionState getState(boolean markAsDirty) {
        return (MergeContributionState) super.getState(markAsDirty);
    }

    @Subscribe
    public void populateTocItemList(TocItemListResponseEvent event) {
        LOG.trace("Calling populateTocItemList");
        getState().tocItemsJsonArray = toJsonString(event.getTocItemList());
        callFunction("populateTocItemList");
    }

    @Subscribe
    public void refreshContributionContent(RefreshContributionEvent event) {
        LOG.trace("Calling refresh");
        callFunction("refreshContributions");
    }

    @Subscribe
    public void populateMergeActionList(FetchContributionsListEvent event) {
        this.contributionVO = event.getContributionVO();
        this.selectAll = event.isSelectAll();
        callFunction("populateMergeActionList", selectAll);
    }

    private void registerServerSideAPI() {

        addFunction("handleMergeAction", new JavaScriptFunction() {
            @Override
            public void call(JsonArray arguments) {
                List<MergeActionVO> mergeActionVOS = new ArrayList<>();
                JsonArray jsonArray = arguments.getArray(0);
                for (int i = 0; i <= jsonArray.length(); i++) {
                    try {
                        JsonObject selector = jsonArray.getObject(i);
                        String action = selector.getString("action");
                        String elementState = selector.getString("elementState");
                        String elementId = selector.getString("elementId").replaceAll("revision-", "");;
                        String elementTagName = selector.getString("elementTagName");
                        if (action == null || elementState == null || elementId == null || elementTagName == null) {
                            throw new Exception("Invalid request parameters");
                        }
                        mergeActionVOS.add(new MergeActionVO(MergeAction.of(action), ElementState.of(elementState), elementId, elementTagName, contributionVO));
                    } catch (Exception e) {
                        LOG.debug("Request merge suggestion stopped because of bad arguments in the request");
                    }
                }
                LOG.trace("Merge element request...");
                eventBus.post(new ApplyContributionsRequestEvent(mergeActionVOS, selectAll));
            }
        });

        addFunction("handleContributionSelection", new JavaScriptFunction() {
            @Override
            public void call(JsonArray arguments) {
                JsonObject data = arguments.get(0);
                Boolean selected = new Boolean(data.get("selected").asBoolean());
                eventBus.post(new ContributionSelectionEvent(selected));
            }

        });

        addFunction("requestTocItemList", new JavaScriptFunction() {
            @Override
            public void call(JsonArray arguments) {
                eventBus.post(new TocItemListRequestEvent());
            }

        });
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
