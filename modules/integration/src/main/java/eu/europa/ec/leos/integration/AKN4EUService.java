package eu.europa.ec.leos.integration;

import eu.europa.ec.leos.model.user.User;

import java.io.File;

public interface AKN4EUService {

    void convert(File legFile, User user, String outputDescriptor) throws Exception;
}
