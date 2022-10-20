package eu.europa.ec.leos.services.document.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import eu.europa.ec.leos.model.action.CheckinCommentVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CheckinCommentUtil {
    
    private static final Logger LOG = LoggerFactory.getLogger(CheckinCommentUtil.class);
    
    public static CheckinCommentVO getJavaObjectFromJson(String json) {
        CheckinCommentVO obj;
        ObjectMapper mapper = new ObjectMapper();
        try {
            obj = mapper.readValue(Strings.nullToEmpty(json), CheckinCommentVO.class);
        } catch (IOException e) {
            obj = new CheckinCommentVO(Strings.nullToEmpty(json)); // show the (non json) string as title
            LOG.trace("Cannot convert to a CheckinComment java object from json: " + json);
        }
        return obj;
    }
    
    public static String getJsonObject(CheckinCommentVO obj) {
        String json = "";
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot build JSON string for CheckinCommentVO object: " + obj);
        }
        
        return json;
    }
}
