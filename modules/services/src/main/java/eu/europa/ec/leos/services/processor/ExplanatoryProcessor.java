package eu.europa.ec.leos.services.processor;

import eu.europa.ec.leos.domain.cmis.document.Explanatory;
import eu.europa.ec.leos.model.annex.LevelItemVO;
import eu.europa.ec.leos.model.xml.Element;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import io.atlassian.fugue.Pair;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

public interface ExplanatoryProcessor {

    @PreAuthorize("hasPermission(#document, 'CAN_UPDATE')")
    byte[] insertNewElement(Explanatory document, String elementId, String tagName, boolean before);

    @PreAuthorize("hasPermission(#document, 'CAN_UPDATE')")
    byte[] deleteElement(Explanatory document, String elementId, String tagName) throws Exception;

    @PreAuthorize("hasPermission(#document, 'CAN_UPDATE')")
    byte[] mergeElement(Explanatory document, String elementContent, String elementName, String elementId);

    @PreAuthorize("hasPermission(#document, 'CAN_UPDATE')")
    byte[] updateElement(Explanatory document, String elementId, String tagName, String elementFragment);

    Pair<byte[], Element> getSplittedElement(byte[] docContent, String elementContent, String elementName, String elementId) throws Exception;

    Element getMergeOnElement(Explanatory document, String elementContent, String elementName, String elementId) throws Exception;

    Element getTocElement(final Explanatory document, final String elementId, final List<TableOfContentItemVO> toc);

    LevelItemVO getLevelItemVO(Explanatory document, String elementId, String elementTagName) throws Exception;

}
