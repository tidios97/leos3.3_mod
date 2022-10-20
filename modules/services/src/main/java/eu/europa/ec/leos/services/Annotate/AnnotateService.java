package eu.europa.ec.leos.services.Annotate;

import org.springframework.stereotype.Service;

@Service
public interface AnnotateService {

	String getAnnotations(String docName, String proposalRef);
	
}
