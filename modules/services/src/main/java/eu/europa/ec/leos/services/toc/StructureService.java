package eu.europa.ec.leos.services.toc;

import eu.europa.ec.leos.vo.toc.AlternateConfig;
import eu.europa.ec.leos.vo.toc.NumberingConfig;
import eu.europa.ec.leos.vo.toc.TocItem;

import java.util.List;
import java.util.Map;

public interface StructureService {
    
    List<TocItem> getTocItems(String docTemplate);
    
    Map<TocItem, List<TocItem>> getTocRules(String docTemplate);
    
    List<NumberingConfig> getNumberingConfigs(String docTemplate);
    
    String getStructureName(String docTemplate);
    
    String getStructureVersion(String docTemplate);

    String getStructureDescription(String docTemplate);
    
    List<AlternateConfig> getAlternateConfigs(String docTemplate);
    
}
