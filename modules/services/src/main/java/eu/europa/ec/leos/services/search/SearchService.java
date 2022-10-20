package eu.europa.ec.leos.services.search;

import eu.europa.ec.leos.domain.vo.SearchMatchVO;

import java.util.List;

public interface SearchService {

    byte[] replaceText(byte[] xmlContent, String searchText, String replaceText, List<SearchMatchVO> searchMatchVOs);

    byte[] searchAndReplaceText(byte[] xmlContent, String searchText, String replaceText);

    List<SearchMatchVO> searchText(byte[] xmlContent, String searchText, boolean caseSensitive, boolean completeWords) throws Exception;
}
