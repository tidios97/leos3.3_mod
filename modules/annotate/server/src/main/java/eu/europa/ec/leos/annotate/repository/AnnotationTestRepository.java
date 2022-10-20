package eu.europa.ec.leos.annotate.repository;

import org.springframework.stereotype.Repository;

/**
 * This is an extension of the {@link AnnotationRepository} with functions that should be called only from unit tests.
 * In order to allow Spring autowiring to properly inject the correct Repository, this class needs to be referred to as follows:
 * 
 * @Autowired
 * @Qualifier("annotationTestRepos")
 * private AnnotationTestRepository annotRepos;
 * 
 */
@Repository("annotationTestRepos")
public interface AnnotationTestRepository extends AnnotationRepository {

    /**
     * count all items having a root annotation id set (i.e. replies)
     * 
     * @return number of found annotations
     */
    long countByRootAnnotationIdNotNull();
}
