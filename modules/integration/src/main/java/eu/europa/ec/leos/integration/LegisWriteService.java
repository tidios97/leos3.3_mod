package eu.europa.ec.leos.integration;

import java.io.File;

public interface LegisWriteService {
    
    byte[] convert(File legFile) throws Exception;
}
