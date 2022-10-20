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
package eu.europa.ec.leos.services.processor.node;

import java.util.*;

import org.apache.commons.lang3.Validate;

public class XmlNodeConfig {
    public final String xPath;
    public final List<Attribute> attributes;
    public final boolean create;
    public final boolean delete;
    public final String xPathParent;    // xPath to the element that should be deleted (including all children)

    public XmlNodeConfig(String xpath, boolean create, List<Attribute> attributes) {
        validateSupportedXpath(xpath, null);
        this.xPath = xpath;
        this.attributes = Collections.unmodifiableList(attributes);
        this.create = create;
        this.delete = false;
        xPathParent = null;
    }

    public XmlNodeConfig(String xPath, boolean create, List<Attribute> attributes, boolean delete, String xPathParent) {
        validateSupportedXpath(xPath, xPathParent);
        this.xPath = xPath;
        this.attributes = Collections.unmodifiableList(attributes);
        this.create = create;
        this.delete = delete;
        this.xPathParent = xPathParent;
    }

    //Assumptions for Xpath(There are due to create )
    // 1. Predicates, axes and operators are not supported(due to inability to create nodes with these xpaths)
    // 2. only wild card supported is // but there is strict requirement that node after wildcard // must be present in XML else it wouldnt know where to create rest of tree.
    private void validateSupportedXpath(String xPath, String xPathParent) throws IllegalFormatException {
        // below validation is a brief attempt to stop programmer to create an invalid xpath
        Validate.isTrue((xPath.split("(\\()|(\\))|(\\*)|(\\|)|(\\.\\.)|(::)").length == 1),
                "Invalid configuration. Selectors with *,.,(,),.,..,:: are not supported in xpath:[%s]", xPath);
        Validate.isTrue((!xPath.contains("[") || xPath.matches("(.+?)([a-zA-Z]+?)\\[@(.+?)='(.+?)'\\](.*?)")),
                "Invalid configuration. Attribute selectors are supported only in format \"tagName[@attName='attValue']\". xpath:[%s]", xPath);
        Validate.isTrue((xPath.split("//@").length == 1), "Invalid configuration. Attribute can not be selected via wild card. xpath:[%s]", xPath);

        if (xPathParent != null) {
            Validate.isTrue(xPath.startsWith(xPathParent) && !xPathParent.endsWith("/"),
                "Invalid configuration. Needs to be a suffix of the main xpath that describes the new child element. xPathParent:[%s]", xPathParent);
        }
    }

    public static class Attribute {
        public final String name;
        public final String value;
        public final String parent;

        public Attribute(String name, String value, String parent) {
            Validate.notNull(name, "Attribute name can not be null");
            Validate.notNull(value, "Attribute value can not be null");
//            Validate.notNull(parent, "Attribute tag can not be null");
            this.name = name;
            this.value = value;
            this.parent = parent;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Attribute attribute = (Attribute) o;
            return Objects.equals(name, attribute.name) &&
                    Objects.equals(value, attribute.value) &&
                    Objects.equals(parent, attribute.parent);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value, parent);
        }
    }
}
