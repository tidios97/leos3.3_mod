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
<#import "annex.ftl" as annexFtl>
<#-- XML variable to reference the input node model -->

<#assign root=.data_model.resource_tree>

<#assign proposal = root>
<#assign proposalRef = proposal.getResourceId()>
<#if proposal.getChildResource('memorandum')??>
    <#assign memorandum = proposal.getChildResource('memorandum')>
    <#assign memorandumRef = memorandum.getResourceId()>
</#if>
<#if proposal.getChildResource('bill')??>
    <#assign bill = proposal.getChildResource('bill')>
    <#assign billRef = bill.getResourceId()>
    <#assign annexes = bill.getChildResources('annex')>
</#if>
<#assign explanatories = proposal.getChildResources('council_explanatory')>

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
                    </resource>
                </#if>
                <#list explanatories as explanatory>
                    <#assign explanatoryRef = explanatory.getResourceId()>
                    <resource ref="${explanatoryRef}">
                    </resource>
                </#list>
                <#if bill??>
                    <resource ref="${billRef}">
                        <excludes>
                            <#list annexes as annex>
                                <#assign annexRef = annex.getResourceId()>
                                <exclude ref="${annexRef}"/>
                            </#list>
                        </excludes>
                    </resource>
                </#if>
                </resource>
            </leos>
            <formats>
                <legisWrite>
                    <format>docx</format>
                </legisWrite>
            </formats>
        </importJob>
    <#if bill??>
        <#list annexes as annex>
            <@annexFtl.annex proposal annex/>
        </#list>
    </#if>
    </importOptions>
</@compress>
