<#ftl encoding="UTF-8"
output_format="XML"
auto_esc=true
strict_syntax=true
strip_whitespace=true
strip_text=true
ns_prefixes={}>

<#--
    Copyright 2017 European Commission

    Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
    You may not use this work except in compliance with the Licence.
    You may obtain a copy of the Licence at:

        https://joinup.ec.europa.eu/software/page/eupl

    Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the Licence for the specific language governing permissions and limitations under the Licence.
-->

<#-- FTL imports -->
<#-- XML variable to reference the input node model -->

<#macro explanatory proposal explanatory>
    <#local proposalRef = proposal.getResourceId()>
    <#local explanatory = proposal.getChildResource('council_explanatory')>
    <#local explanatoryRef = explanatory.getResourceId()>
    <#local explanatoryHref = explanatory.getHref()>
    <importJob filename="${explanatory.getLeosCategory().name()?capitalize}_${explanatory.getDocNumber()}"
               convertAnnotations="${proposal.getExportOptions().isWithAnnotations()?c}">
        <leos>
            <resource ref="${explanatoryRef}" filename="${explanatoryHref}">
            </resource>
        </leos>
        <formats>
            <pdf>
                <format>pdf</format>
                <format>pdf/a</format>
            </pdf>
        </formats>
    </importJob>
</#macro>
