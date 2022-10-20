/*
 * Copyright 2022 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.annotate.model.search;

import eu.europa.ec.leos.annotate.model.entity.Annotation;

import java.util.ArrayList;
import java.util.List;

public class DocumentAnnotationsResult {
    private List<Annotation> replies;
    private AnnotationSearchOptions searchOptions;
    private AnnotationSearchResult searchResult;

    public DocumentAnnotationsResult() {
        this.searchOptions = null;
        this.searchResult = new AnnotationSearchResult();
        this.replies = new ArrayList<>();
    }

    public AnnotationSearchOptions getSearchOptions() {
        return searchOptions;
    }

    public void setSearchOptions(final AnnotationSearchOptions searchOptions) {
        this.searchOptions = searchOptions;
    }

    public void setSearchResult(final AnnotationSearchResult searchResult) {
        this.searchResult = searchResult;
    }

    public AnnotationSearchResult getSearchResult() {
        return searchResult;
    }

    public void setReplies(final List<Annotation> replies) {
        this.replies = replies;
    }

    public List<Annotation> getReplies() {
        return replies;
    }
}
