package eu.europa.ec.leos.services.document;

import eu.europa.ec.leos.services.search.SearchEngine;
import eu.europa.ec.leos.services.search.SearchEngineImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class SearchEngineFactory {

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    @Cacheable(value = "searchEngineCache", key = "T(java.util.Arrays).hashCode(#p0)")
    public SearchEngine getInstance(byte[] content) {
        return SearchEngineImpl.forContent(content);
    }
}
