package eu.europa.ec.leos.integration;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.instance.Instance;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
@Instance(InstanceType.COMMISSION)
public class ProposalLegisWriteServiceImpl implements LegisWriteService {

    private static final Logger LOG = LoggerFactory.getLogger(ProposalLegisWriteServiceImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("#{integrationProperties['leos.legiswrite.url']}")
    private String legisWriteUrl;

    @Value("#{integrationProperties['leos.legiswrite.convert.uri']}")
    private String convertUri;

    @Override
    public byte[] convert(File legFile) throws Exception {
        Validate.notNull(legFile, "legFile must not be null!");
        try {
            String uri = legisWriteUrl + convertUri;

            MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
            ByteArrayResource contentsAsResource = convertFileToByteArray(legFile);
            map.add("legFile", contentsAsResource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
            ResponseEntity<ByteArrayResource> response = restTemplate.exchange(uri, HttpMethod.POST, requestEntity, ByteArrayResource.class);

            if (response.getBody() != null) {
                return response.getBody().getByteArray();
            }

            LOG.error("Empty response from the external service LegisWrite");
            throw new IllegalStateException("Empty response from the external service LegisWrite");

        } catch(Exception e){
            LOG.error("Exception while calling external service LegisWrite", e);
            throw e;
        }
    }

    private ByteArrayResource convertFileToByteArray(File legFile) throws IOException {
        byte[] bytesArray = new byte[(int) legFile.length()];

        FileInputStream fis = new FileInputStream(legFile);
        fis.read(bytesArray); //read file into bytes[]
        fis.close();

        return new ByteArrayResource(bytesArray);
    }

}
