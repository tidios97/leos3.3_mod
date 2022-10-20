package eu.europa.ec.leos.integration;

import java.io.File;

public interface DocuWriteService {
    
    byte[] convert(File legFile) throws Exception;
}
