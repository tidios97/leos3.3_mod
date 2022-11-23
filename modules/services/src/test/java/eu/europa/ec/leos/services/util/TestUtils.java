package eu.europa.ec.leos.services.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static eu.europa.ec.leos.services.support.XmlHelper.removeAllNameSpaces;

public class TestUtils {
    
    public static byte[] getBytesFromFile(String path, String fileName) {
        return getBytesFromFile(path + fileName);
    }
    
    public static byte[] getBytesFromFile(String path) {
        try {
            File file = new File(path);
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read bytes from file: " + path);
        }
    }
    
    public static byte[] getFileContent(String path, String fileName) {
        return getFileContent(path + fileName);
    }
    
    public static byte[] getFileContent(String fileName) {
        try {
            InputStream inputStream = TestUtils.class.getResource(fileName).openStream();
            byte[] content = new byte[inputStream.available()];
            inputStream.read(content);
            inputStream.close();
            return content;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read bytes from file: " + fileName);
        }
    }

    public static String trimAndRemoveNS(String input) {
        input = removeAllNameSpaces(input);
        return input.replaceAll("\\s+", "");
    }

    public static String squeezeXmlAndRemoveAllNS(String input) {
        input = removeAllNameSpaces(input);
        return squeezeXml(input);
    }

    public static String squeezeXmlRemovingAttributeAndRemoveAllNS(String input, String attr) {
        input = removeAllNameSpaces(input);
        return squeezeXml(input, attr);
    }

    public static String squeezeXml(String input, String attr) {
        return input.replaceAll("\\s+", "")
                .replaceAll(attr+"=\".+?\"", "leos:softdate=\"dummyUser\"")
                .replaceAll("leos:softdate=\".+?\"", "leos:softdate=\"dummyDate\"");
    }

    public static String squeezeXml(String input) {
        return input.replaceAll("\\s+", "")
                .replaceAll("leos:softdate=\".+?\"", "leos:softdate=\"dummyDate\"")
                .replaceAll("id=\".+?\"", "id=\"dummyId\"")
                .replaceAll("xml:id=\".+?\"", "xml:id=\"dummyId\"");
    }
    
    public static String squeezeXmlRemoveNum(String input) {
        return squeezeXml(input)
                .replaceAll("<num(\\s)*?xml:id=\".+?\"(\\s)*?>", "<num>");
    }

    public static String squeezeXmlWithoutXmlIds(String input) {
        return input.replaceAll("\\s+", "")
                .replaceAll("leos:softdate=\".+?\"", "leos:softdate=\"dummyDate\"")
                .replaceAll("id=\".+?\"", "id=\"dummyId\"");
    }

    public static String squeezeXmlWithoutIds(String input) {
        return input.replaceAll("\\s+", "")
                .replaceAll("leos:softdate=\".+?\"", "leos:softdate=\"dummyDate\"");
    }

    public static String squeezeXmlAndDummyDate(String input) {
        return input.replaceAll("\\s+", "")
                .replaceAll("leos:softdate=\".+?\"", "leos:softdate=\"dummyDate\"");
    }

    public static String dummyDate(String input) {
        return input.replaceAll("leos:softdate=\".+?\"", "leos:softdate=\"dummyDate\"");
    }
}
