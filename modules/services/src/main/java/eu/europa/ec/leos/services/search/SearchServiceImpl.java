package eu.europa.ec.leos.services.search;

import eu.europa.ec.leos.domain.vo.SearchMatchVO;
import eu.europa.ec.leos.services.processor.content.XmlContentProcessor;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {

    SearchServiceImpl(XmlContentProcessor xmlContentProcessor,
                      ObjectProvider<SearchEngine> searchEngineProvider){
        this.xmlContentProcessor = xmlContentProcessor;
        this.searchEngineProvider =searchEngineProvider;

    }
    protected final ObjectProvider<SearchEngine> searchEngineProvider;
    protected final XmlContentProcessor xmlContentProcessor;
    private static final Logger LOG = LoggerFactory.getLogger(SearchServiceImpl.class);

    @Override
    public byte[] replaceText(byte[] xmlContent, String searchText, String replaceText, List<SearchMatchVO> searchMatchVOs) {
        Validate.notNull(xmlContent, "xml content is required");
        try {
            SearchEngine se = searchEngineProvider.getObject((Object) xmlContent);
            return se.replace(xmlContent, searchMatchVOs, searchText, replaceText, true);
        } catch (Exception e) {
            LOG.error("Unable to replace", e);
            throw e;
        }
    }

    @Override
    public byte[] searchAndReplaceText(byte[] xmlContent, String searchText, String replaceText) {
        return xmlContentProcessor.searchAndReplaceText(xmlContent, searchText, replaceText);
    }

    @Override
    public List<SearchMatchVO> searchText(byte[] xmlContent, String searchText, boolean caseSensitive, boolean completeWords) throws Exception {
        Validate.notNull(xmlContent, "xml content is required");
        try {
            SearchEngine se = searchEngineProvider.getObject((Object) xmlContent);
            return se.searchText(searchText, caseSensitive, completeWords);
        } catch (Exception e) {
            LOG.error("Unable to search", e);
            throw e;
        }
    }
}
