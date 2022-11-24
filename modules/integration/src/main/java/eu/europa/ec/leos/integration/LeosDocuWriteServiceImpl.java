package eu.europa.ec.leos.integration;

import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class LeosDocuWriteServiceImpl implements DocuWriteService {
    @Override
    public byte[] convert(File legFile) throws Exception {
        throw new IllegalStateException("Feature not implemented for the running instance");
    }
}
