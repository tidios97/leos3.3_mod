package eu.europa.ec.leos.services.search;

import eu.europa.ec.leos.domain.vo.SearchMatchVO;

import java.util.List;

public interface SearchEngine {
    
    byte[] replace(final byte[] docContent, final List<SearchMatchVO> searchMatchVOs, String searchText, String replaceText, boolean removeEmptyTags);
    List<SearchMatchVO> searchText(String searchText, boolean isMatchCase, boolean isWholeWords);

}
