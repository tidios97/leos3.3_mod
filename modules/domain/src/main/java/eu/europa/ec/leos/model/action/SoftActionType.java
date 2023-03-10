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

package eu.europa.ec.leos.model.action;

import java.util.Arrays;

public enum SoftActionType {
    ADD("add"),
    MOVE_TO("move_to"),
    MOVE_FROM("move_from"),
    DELETE("del"),
    TRANSFORM("trans"),
    DELETE_TRANSFORM("del_trans"),
    UNDELETE("undelete"),
    SPLITTED("splitted");

    String softAction;

    SoftActionType(String softAction) {
        this.softAction = softAction;
    }

    public String getSoftAction() {
        return softAction;
    }

    public static SoftActionType of(String softAction) {
        return Arrays.asList(SoftActionType.values()).stream()
                .filter(x -> x.softAction.equals(softAction)).findFirst().orElse(null);
    }
}
