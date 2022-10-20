package eu.europa.ec.leos.services.coedition.handler;

import eu.europa.ec.leos.vo.coedition.CoEditionActionInfo;
import eu.europa.ec.leos.vo.coedition.CoEditionVO;

import java.util.List;

public interface InfoHandler {

    /**
     * This method checks and store the info of the user as passed in input
     * @param coEditionVo this object contains the information about the user which is to be stored 
     * @return information about if operation was successful
     */
    CoEditionActionInfo storeInfo(CoEditionVO coEditionVo);
    /**
     * This method removes the info for the user passed in input
     * @param coEditionVo this object contains the information about the user which is to be removed
     * @return information about if operation was successful
     */
    CoEditionActionInfo removeInfo(CoEditionVO coEditionVo);

    /**
     * This method checks if the info with same presenter on same document and same element exists
     * @param coEditionVo this object contains the information about the info which is to be checked
     * @return information about if operation was successful
     */
    CoEditionActionInfo checkIfInfoExists(CoEditionVO coEditionVo);

    /**
     * This method checks if exists info for the session given and returns first info found
     * @param sessionId this object contains the session id about the info which is to be checked
     * @return information about if operation was successful
     */
    CoEditionActionInfo checkIfInfoExists(String sessionId);

    /**this method returns existing edit info on given document
     * @param docId
     * @return unmodifiable list Of edit info
     */
    List<CoEditionVO> getCurrentEditInfo(String docId);
    
    /**this method returns existing edit info in the repository on all existing documents and elements
     * @return unmodifiable list Of all info
     */
    List<CoEditionVO> getAllEditInfo();
}
