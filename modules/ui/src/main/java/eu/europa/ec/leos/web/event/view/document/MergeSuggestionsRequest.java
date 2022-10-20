package eu.europa.ec.leos.web.event.view.document;

import java.util.List;

public class MergeSuggestionsRequest {

    private List<MergeSuggestionRequest> listOfSuggestion;

    public MergeSuggestionsRequest(List<MergeSuggestionRequest> listOfSuggestion) {
        this.listOfSuggestion = listOfSuggestion;
    }

    public List<MergeSuggestionRequest> getListOfSuggestion() {
        return listOfSuggestion;
    }
}
