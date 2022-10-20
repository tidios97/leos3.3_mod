package eu.europa.ec.leos.integration;

import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.model.user.User;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Instance(InstanceType.OS)
public class AKN4EUServiceImpl implements AKN4EUService {

    @Override
    public void convert(File legFile, User user, String outputDescriptor) throws Exception {
        return;
    }

}
