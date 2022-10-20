package eu.europa.ec.leos.integration;

import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import org.springframework.stereotype.Service;
import java.io.File;

@Service
@Instance(InstanceType.OS)
public class LegisWriteServiceImpl implements LegisWriteService {

    @Override
    public byte[] convert(File legFile) throws Exception {
        return null;
    }
    
}
