package eu.europa.ec.leos.ui.extension;

import eu.europa.ec.leos.domain.vo.SearchMatchVO;
import eu.europa.ec.leos.ui.component.search.SearchBar;
import eu.europa.ec.leos.ui.shared.js.LeosJavaScriptExtensionState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SearchBarState extends LeosJavaScriptExtensionState {
    public static final long serialVersionUID = 12345678L;

    Long searchRequestId = 0L; //Keep only one search request . ignore rest
    boolean matchCase;
    boolean wholeWords;
    String searchText;
    String replaceText;

    String searchStatus = "";
    List<SearchMatchVO> matches = new ArrayList<>();
    int selectedMatch = -1; //-1 is none selected. it is 0 based index

    String replaceStatus = "";
    public Long getSearchRequestId() {
        return searchRequestId;
    }

    public void setSearchRequestId(Long searchRequestId) {
        this.searchRequestId = searchRequestId;
    }

    public void newSearch() {
            Long searchId = new Random().nextLong();
            setSearchRequestId(searchId);
            this.selectedMatch = -1;
            this.matches.clear();
            this.searchStatus = SearchBar.INPROGRESS;
            this.replaceStatus = SearchBar.SAVED.equals(this.replaceStatus) ? SearchBar.INPROGRESS :this.replaceStatus; //new search shd not reset if some replace is already done.
    }

    public List<SearchMatchVO> getMatches() {
        return matches;
    }

    public void setMatches(List<SearchMatchVO> matches) {
        this.matches.clear();
        if(matches.size()>0) {
            this.matches.addAll(matches);
            selectedMatch = 0;//first
        } else{
            selectedMatch = -1;
        }
    }
    public String getSearchStatus() {
        return searchStatus;
    }

    public void setSearchStatus(String searchStatus) {
        this.searchStatus = searchStatus;
    }

    public int getSelectedMatch() {
        return selectedMatch;
    }

    public void setSelectedMatch(int selectedMatch) {
        this.selectedMatch = selectedMatch;
    }

    public int getTotalMatch() {
        return matches.size();
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getReplaceText() {
        return replaceText;
    }

    public void setReplaceText(String replaceText) {
        this.replaceText = replaceText;
    }

    public boolean isMatchCase() {
        return matchCase;
    }

    public void setMatchCase(boolean matchCase) {
        this.matchCase = matchCase;
    }

    public boolean isWholeWords() {
        return wholeWords;
    }

    public void setWholeWords(boolean wholeWords) {
        this.wholeWords = wholeWords;
    }

    public String getReplaceStatus() {
        return replaceStatus;
    }

    public void resetSearchState() {
        searchRequestId = 0L;
        searchText = "";
        replaceText = "";

        searchStatus = "";
        matches.clear();
        selectedMatch = -1;

        replaceStatus = "";
    }

  public void setReplaceStatus(String finished) {
        replaceStatus = finished;
  }
}
