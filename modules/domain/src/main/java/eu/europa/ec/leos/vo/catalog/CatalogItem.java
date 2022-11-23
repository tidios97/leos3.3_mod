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
package eu.europa.ec.leos.vo.catalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogItem {

    public static enum ItemType {
        CATEGORY,
        TEMPLATE
    }

    private ItemType type;
    private String id;
    private Boolean enabled;
    private Boolean hidden;
    private String key;

    private Map<String, String> nameMap;
    private Map<String, String> descMap;
    private Map<String, String> langMap;

    private List<CatalogItem> itemList = new ArrayList<CatalogItem>();

    public ItemType getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getKey() {
        return key;
    }

    public String getName(String lang) {
        return (nameMap != null) ? nameMap.get(lang.toUpperCase()) : null;
    }

    public String getDescription(String lang) {
        return (descMap != null) ? descMap.get(lang.toUpperCase()) : null;
    }

    public Map<String, String> getLanguages() {
        return (langMap != null) ? langMap : new HashMap<String, String>(0);
    }

    public Map<String, String> getNames() {
        return (nameMap != null) ? nameMap : new HashMap<String, String>(0);
    }

    public Map<String, String> getDescriptions() {
        return (descMap != null) ? descMap : new HashMap<String, String>(0);
    }

    public List<CatalogItem> getItems() {
        return (itemList != null) ? itemList : new ArrayList<CatalogItem>(0);
    }

    public Boolean isHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }
}
