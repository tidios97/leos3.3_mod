/*
 * Copyright 2018 European Commission
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

package eu.europa.ec.leos.domain.vo;

import eu.europa.ec.leos.domain.common.ErrorCode;

import java.util.ArrayList;
import java.util.List;

public class ValidationVO {

    private List<ErrorVO> errors = new ArrayList<>();

    public void addError(ErrorCode errorCode, Object... args) {
        this.errors.add(new ErrorVO(errorCode, args));
    }

    public void addErrors(List<ErrorVO> errors) {
        this.errors.addAll(errors);
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<ErrorVO> getErrors() {
        return errors;
    }
}
