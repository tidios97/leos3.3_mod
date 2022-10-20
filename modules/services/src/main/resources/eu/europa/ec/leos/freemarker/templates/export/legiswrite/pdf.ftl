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

<#assign root =.data_model.resource_tree>

<#assign proposal = root>
<#assign proposalRef = proposal.getResourceId()>
<#if proposal.getChildResource('memorandum')??>
    <#assign memorandum = proposal.getChildResource('memorandum')>
    <#assign memorandumRef = memorandum.getResourceId()>
</#if>
<#assign bill = proposal.getChildResource('bill')>
<#assign billRef = bill.getResourceId()>
<#assign annexes = bill.getChildResources('annex')>

<@compress>
    <importOptions technicalKey="${proposal.getExportOptions().getTechnicalKey()}">
        <importJob filename="${proposal.getLeosCategory().name()?capitalize}"
                   convertAnnotations="${proposal.getExportOptions().isWithAnnotations()?c}" comparisonType="${proposal.getExportOptions().getComparisonType()?lower_case}">
            <leos>
                <resource ref="${proposalRef}">
                    <#if proposal.getComponentId('coverPage')??>
                        <includes>
                            <include ref="${proposal.getComponentId('coverPage')}"/>
                        </includes>
                    </#if>
                    <#if memorandum??>
                        <resource ref="${memorandumRef}">
                            <#if memorandum.getComponentId('coverPage')??>
                                <excludes>
                                    <exclude ref="${memorandum.getComponentId('coverPage')}"/>
                                </excludes>
                            </#if>
                        </resource>
                    </#if>
                    <resource ref="${billRef}">
                        <#if bill.getComponentId('coverPage')??>
                            <excludes>
                                <exclude ref="${bill.getComponentId('coverPage')}"/>
                            </excludes>
                        </#if>
                    </resource>
                    <#list annexes as annex>
                        <#assign annexRef = annex.getResourceId()>
                        <resource ref="${annexRef}">
                            <#if annex.getComponentId('coverPage')??>
                                <excludes>
                                    <exclude ref="${annex.getComponentId('coverPage')}"/>
                                </excludes>
                            </#if>
                        </resource>
                    </#list>
                </resource>
            </leos>
            <formats>
                <pdf>
                    <format>pdf</format>
                    <format>pdf/a</format>
                </pdf>
            </formats>
        </importJob>
    </importOptions>
</@compress>
