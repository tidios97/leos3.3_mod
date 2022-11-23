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
package eu.europa.ec.digit.userdata.request;

import java.util.Objects;

public class SpecialEntityRequest {

    private String userId;
    private String entity;

    public String getUserId() {
        return userId;
    }

    public String getEntity() {
        return entity;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecialEntityRequest that = (SpecialEntityRequest) o;
        return userId.equals(that.userId) && entity.equals(that.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, entity);
    }
}