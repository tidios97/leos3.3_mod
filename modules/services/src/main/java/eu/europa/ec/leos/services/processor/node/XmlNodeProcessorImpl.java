package eu.europa.ec.leos.services.processor.node;

import com.google.common.base.Stopwatch;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.services.support.XPathCatalog;
import eu.europa.ec.leos.services.processor.node.XmlNodeConfig.Attribute;
import eu.europa.ec.leos.services.support.XercesUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.processor.node.XmlNodeConfigProcessor.getDocEEATagList;
import static eu.europa.ec.leos.services.support.XercesUtils.createXercesDocument;
import static eu.europa.ec.leos.services.support.XmlHelper.EMPTY_STRING;
import static eu.europa.ec.leos.services.support.XmlHelper.parseXml;

@Service
public class XmlNodeProcessorImpl implements XmlNodeProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(XmlNodeProcessorImpl.class);
    @Autowired
    private MessageHelper messageHelper;

    @Override
    public Map<String, String> getValuesFromXml(byte[] xmlContent, String[] keys, Map<String, XmlNodeConfig> config) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<String, String> metaDataMap = new HashMap<>();
        Document document = createXercesDocument(xmlContent);
        for (String key : keys) {
            if (config.get(key) != null) {
                Node node = XercesUtils.getFirstElementByXPath(document, config.get(key).xPath);
                if (node != null) {
                    String value = StringEscapeUtils.unescapeXml(node.getTextContent());
                    metaDataMap.put(key, value);
                }
            }
        }
        LOG.trace("{} Values retrieved from xml in ({} milliseconds)", metaDataMap.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return metaDataMap;
    }

    @Override
    public byte[] setValuesInXml(byte[] xmlContent, Map<String, String> keyValue, Map<String, XmlNodeConfig> config, Map<String, XmlNodeConfig> oldConfig) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Document document = createXercesDocument(xmlContent);
        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            String key = entry.getKey();
            if (config.get(key) == null) {
                LOG.warn("Configuration not found for:{}, ignoring and continuing", key);
                continue;
            }

            String value = parseXml(entry.getValue());
            // EEA Relevance: Transform boolean to default EEA Relevance message (for now, before accepting custom message)
            if (getDocEEATagList().contains(key)) {
                value = Boolean.parseBoolean(value) ?
                        messageHelper.getMessage("wizard.document.create.metadata.eeaRelevance.default") : EMPTY_STRING;
            }

            String xPath = config.get(key).xPath;
            Node node = XercesUtils.getFirstElementByXPath(document, xPath);
            if (oldConfig != null && oldConfig.get(key) != null) {
                String oldXPath = oldConfig.get(key).xPath;
                Node oldNode = XercesUtils.getFirstElementByXPath(document, oldXPath);
                if (oldNode != null) {
                    node = oldNode;
                }
            }
            if (xPath.contains("council_explanatory")) {
                createComponentNode(document, xPath, config.get(key).attributes, value);
            } else if (node != null && config.get(key).delete && value.isEmpty()) {
                // Delete the XML element
                String parentXPath = config.get(key).xPathParent;
                Node parentNode = XercesUtils.getFirstElementByXPath(document, parentXPath);
                XercesUtils.deleteElementsByXPath(parentNode, parentXPath);
            } else if (node != null) {
                // Update existing node
                updateNode(node, value);
            } else if (config.get(key).create && (!getDocEEATagList().contains(key) ||
                    (getDocEEATagList().contains(key) && !value.isEmpty()))) { //LEOS-5691: Do not add the node if empty
                // Create the node, if the creation flag for the specific element is true
                createAndUpdateNode(document, xPath, config.get(key).attributes, value);
            }
        }
        LOG.trace("Values set in xml ({} milliseconds)", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return XercesUtils.nodeToByteArraySimple(document);
    }

    @Override
    public byte[] setValuesInXml(byte[] xmlContent, Map<String, String> keyValue, Map<String, XmlNodeConfig> config) {
        return this.setValuesInXml(xmlContent, keyValue, config, null);
    }

    private void updateNode(Node node, String value) {
        node.setTextContent(value);
    }

    private void createComponentNode(Document document, String xPath, List<Attribute> configAttributes, String value) {
        String[] nodes = xPath.split("(?<!/)(?=((/+)))");

        // 1. iterate and break at first non-existing node in xml
        StringBuilder partialXPath = new StringBuilder();
        int index = 0;
        Node node = null;
        for (; index < nodes.length; index++) {
            partialXPath.append(nodes[index]);
            Node found = XercesUtils.getFirstElementByXPath(document, partialXPath.toString());
            if (!nodes[index].isEmpty() && nodes[index].contains("collectionBody") && found != null) {// ignore nodes
                LOG.debug("Node not found:{}", nodes[index]);
                node = found;
                break;
            }
        }

        if (node != null) {
            // 2. create xml structure for remaining absent nodes
            Deque<String> stack = new ArrayDeque<>();
            for (index = 0; index < nodes.length; index++) {
                if (!(nodes[index].contains("documentCollection") || nodes[index].contains("collectionBody"))) {
                    stack.push(nodes[index].replaceAll("//|/", "") // Strip // and /
                            .replaceAll("@refersTo='#.+' or ", "")); // Strip refersTo first 'or' condition
                }
            }

            LOG.debug("Need to add node in path {}. First missing node: {}; Has to add these nodes (inverse) : {}", xPath, partialXPath.toString(), getQueueAsString(stack));
            addToFragment(document, node, configAttributes, stack, value);
        } else {
            throw new IllegalStateException("The XPath used for populating the Document is not valid " + xPath);
        }
    }

    private void createAndUpdateNode(Document document, String xPath, List<Attribute> configAttributes, String value) {
        String[] nodes = xPath.split("(?<!/)(?=((/+)))");

        // 1. iterate and break at first non-existing node in xml
        StringBuilder partialXPath = new StringBuilder();
        int index = 0;
        Node node = null;
        for (; index < nodes.length; index++) {
            partialXPath.append(nodes[index]);
            Node found = XercesUtils.getFirstElementByXPath(document, partialXPath.toString());
            if (!nodes[index].isEmpty() && found == null) {// ignore nodes
                LOG.debug("Node not found:{}", nodes[index]);
                break;
            } else {
                node = found;
            }
        }

        if (node != null) {
            // 2. create xml structure for remaining absent nodes
            Deque<String> stack = new ArrayDeque<>();
            for (; index < nodes.length; index++) {
                if (!(nodes[index].contains("documentCollection") || nodes[index].contains("collectionBody"))) {
                    stack.push(nodes[index].replaceAll("//|/", "") // Strip // and /
                        .replaceAll("@refersTo='#.+' or ", "")); // Strip refersTo first 'or' condition
                }
            }

            LOG.debug("Need to add node in path {}. First missing node: {}; Has to add these nodes (inverse) : {}", xPath, partialXPath.toString(), getQueueAsString(stack));
            addToFragment(document, node, configAttributes, stack, value);
        } else {
            throw new IllegalStateException("The XPath used for populating the Document is not valid " + xPath);
        }
    }

    private void addToFragment(Document document, Node node, List<Attribute> configAttributes, Deque<String> stack, String value) {
        Set<Attribute> attributes;
        // 2.2 create rest of structure; add content only to the leaf.
        while (stack.peekLast() != null) {
            String nextFragment = stack.pollLast();
            nextFragment = XPathCatalog.removeNamespaceFromXml(nextFragment);
            String tagName = parseForTagName(nextFragment);

            // 2.1 handle if final selection is attribute value
            attributes = new HashSet<>();
            if (!stack.isEmpty() && stack.getLast().startsWith("@")) {
                attributes.add(new Attribute(stack.pollLast().substring(1), value, null));
            }
            boolean isAttr = false;
            if (!attributes.isEmpty()) {
                isAttr = true;
            }

            Attribute fragmentAttribute = parseForAttribute(nextFragment);
            if (fragmentAttribute != null) {
                attributes.add(fragmentAttribute);
            }
            LOG.debug("Adding {}, with attributes from stack: ", tagName, getAttributesAsString(attributes));
            attributes.addAll(configAttributes.stream().filter(attr -> attr.parent.equals(tagName)).collect(Collectors.toSet()));
            LOG.debug("Added attributes from configAttributes {}: ", getAttributesAsString(attributes));

            String content = null;
            if (!isAttr && stack.peekLast() == null) {
                content = value;
            }
            // 3. Inject newly created node in XML
            Node newNode = XercesUtils.createElementWithAknNS(document, tagName, content);
            node = XercesUtils.addChild(newNode, node);
            for (Attribute attr : attributes) {
                XercesUtils.addAttribute(node, attr.name, attr.value);
            }
            attributes.clear();
        }
    }

    /*
        parse xpathFragment between // or / for existance of attribute selector,
        we expect attribute selector wil be in format [@attName='attValue']
        returns null if there is no attribute
        returns attribute if there is attribute selector.
     */
    private Attribute parseForAttribute(String xPathFragment) {
        if (!xPathFragment.contains("[")) {
            return null;
        }
        Pattern pattern = Pattern.compile("(?<tagName>[a-zA-Z]+?)\\[@(?<attName>.+?)='(?<attValue>.+?)'\\]");
        Matcher matcher = pattern.matcher(xPathFragment.trim());

        matcher.matches();
        String tagName = matcher.group("tagName");
        String attributeName = matcher.group("attName");
        String attributeValue = matcher.group("attValue");
        return new Attribute(attributeName, attributeValue, tagName);
    }

    private String parseForTagName(String xPathFragment) {
        return xPathFragment != null && xPathFragment.contains("[") ? xPathFragment.substring(0, xPathFragment.indexOf("[")) : xPathFragment;
    }

    public static String getQueueAsString(Queue<String> queue) {
        String str = "{";
        for (String s : queue) {
            str += ", " + s;
        }
        str += "}";
        return str;
    }

    public static String getAttributesAsString(Set<XmlNodeConfig.Attribute> attrs) {
        String str = "";
        for (XmlNodeConfig.Attribute attr : attrs) {
            str += "" + attr.parent + "[" + attr.name + "=" + attr.value + "]";
        }
        return str;
    }
}
