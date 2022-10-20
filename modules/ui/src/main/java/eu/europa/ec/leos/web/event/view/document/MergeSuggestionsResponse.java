package eu.europa.ec.leos.web.event.view.document;

import elemental.json.JsonObject;

import java.util.List;

public class MergeSuggestionsResponse {

    private List<JsonObject> suggestionsResponseList;

    public MergeSuggestionsResponse(List<JsonObject> suggestionsResponseList) {
        this.suggestionsResponseList = suggestionsResponseList;
    }

    public List<JsonObject> getSuggestionsResponseList() {
        return suggestionsResponseList;
    }
}