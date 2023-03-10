/*
 * Copyright 2017 European Commission
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
; // jshint ignore:line
define(function testAknAuthorialNotePlugin(require) {
    "use strict";
    var aknAuthorialNotePluginToTest =  require("plugins/aknAuthorialNote/aknAuthorialNotePlugin");

    describe(
        "Unit tests for plugins/aknAuthorialNote",
        function() {
            var transformationConfigForAknAuthorialNote = '{"akn":"authorialNote","html":"span[class=authorialnote]","attr":[{"html":"class=authorialnote"},{"akn":"marker","html":"marker"},{"akn":"placement","html":"placement"},{"akn":"xml:id","html":"id"},{"akn":"leos:origin","html":"data-origin"},{"akn":"leos:softuser","html":"data-akn-attr-softuser"},{"akn":"leos:softdate","html":"data-akn-attr-softdate"},{"html":"data-akn-name=aknAuthorialNote"}],"sub":{"akn":"mp","html":"span","attr":[{"akn":"xml:id","html":"data-akn-mp-id"},{"akn":"leos:origin","html":"data-mp-origin"}],"sub":[{"akn":"text","html":"span[title]"}]}}';

            it("Tests if transformation config is valid.", function() {
                expect(JSON.stringify(aknAuthorialNotePluginToTest.transformationConfig)).toEqual(transformationConfigForAknAuthorialNote);
            });
        });
});