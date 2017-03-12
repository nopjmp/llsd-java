/*
 * LLSDJ - LLSD in Java example
 *
 * Copyright(C) 2008 University of St. Andrews
 */

package lindenlab.llsd;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * LLSD parser in Java. See <a href="http://wiki.secondlife.com/wiki/LLSD">http://wiki.secondlife.com/wiki/LLSD</a>
 * for more information on LLSD.
 */
public class LLSDParser {
    private final DateFormat iso9601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Document builder used to parse the replies
     */
    private final DocumentBuilder documentBuilder;

    public      LLSDParser()
        throws ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        this.documentBuilder = factory.newDocumentBuilder();
    }

    private List<Node> extractElements(final NodeList nodes) {
        final List<Node> trimmedNodes = new ArrayList<Node>();

        for (int nodeIdx = 0; nodeIdx < nodes.getLength(); nodeIdx++) {
            final Node node = nodes.item(nodeIdx);
            switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                trimmedNodes.add(node);
                break;
            default:
                break;
            }
        }

        return trimmedNodes;
    }

    /**
     * Parses an LLSD document from the given input stream.
     *
     * @param xmlFile the XML input stream to read and parse as LLSD.
     * @throws IOException if there was a problem reading from the input
     * stream.
     * @throws LLSDException if the document is valid XML, but invalid LLSD,
     * for example if a date cannot be parsed.
     * @throws SAXException if there was a problem parsing the XML structure of
     * the document.
     */
    public LLSD parse(final InputStream xmlFile)
        throws IOException, LLSDException, SAXException {
        final Document document = this.documentBuilder.parse(xmlFile);
        final List<Node> childNodesTrimmed;
        final Node llsdNode = document.getDocumentElement();
        final Object llsdContents;

        if (null == llsdNode) {
            throw new LLSDException("Outer-most tag for LLSD missing.");
        }

        if (!llsdNode.getNodeName().equalsIgnoreCase("llsd")) {
            throw new LLSDException("Outer-most tag for LLSD is \""
                + llsdNode.getNodeName() + "\" instead of \"llsd\".");
        }

        childNodesTrimmed = extractElements(llsdNode.getChildNodes());
        if (childNodesTrimmed.size() == 0) {
            // XXX: Warn?
            return new LLSD(null);
        }

        if (childNodesTrimmed.size() > 1) {
            throw new LLSDException("Expected only one subelement for element <llsd>.");
        }

        llsdContents = parseNode(childNodesTrimmed.get(0));

        return new LLSD(llsdContents);
    }

    private List<Object> parseArray(final NodeList nodeList)
        throws LLSDException {
        final List<Object> value = new ArrayList<Object>();

        for (int nodeIdx = 0; nodeIdx < nodeList.getLength(); nodeIdx++) {
            final Node node = nodeList.item(nodeIdx);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                value.add(parseNode(node));
            }
        }

        return value;
    }

    private Boolean parseBoolean(final String elementContents)
        throws LLSDException {
        if (elementContents.equals("1") ||
            elementContents.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    private Date parseDate(final String elementContents)
        throws LLSDException {
        final Date value;

        if (elementContents.length() == 0) {
            return new Date(0);
        }

        try {
            value = this.iso9601Format.parse(elementContents);
        } catch(java.text.ParseException e) {
            throw new LLSDException("Unable to parse LLSD date value, received \""
                + elementContents + "\".", e);
        }

        return value;
    }

    private Integer parseInteger(final String elementContents)
        throws LLSDException {
        final Integer value;

        if (elementContents.length() == 0) {
            return 0;
        }

        try {
            // XXX: Don't just assume the default parser is okay for SSLD, check
            value = new Integer(elementContents);
        } catch(NumberFormatException e) {
            throw new LLSDException("Unable to parse LLSD integer value, received \""
                + elementContents + "\".", e);
        }

        return value;
    }

    private Map<String, Object> parseMap(final NodeList nodeList)
        throws LLSDException {
        final List<Node> trimmedNodes = extractElements(nodeList);
        final Map<String, Object> valueMap = new HashMap<String, Object>();

        if ((trimmedNodes.size() % 2) != 0) {
            throw new LLSDException("Unable to parse LLSD map as it has odd number of nodes: "
                + nodeList.toString());
        }

        for (int nodeIdx = 0; nodeIdx < trimmedNodes.size(); nodeIdx = nodeIdx + 2) {
            final NodeList keyChildren;
            final Node keyNode = trimmedNodes.get(nodeIdx);
            final Node valueNode = trimmedNodes.get(nodeIdx + 1);
            String key = null;
            final Object value;

            keyChildren = keyNode.getChildNodes();
            for (int keyNodeIdx = 0; keyNodeIdx < keyChildren.getLength(); keyNodeIdx++) {
                final Node textNode = keyChildren.item(keyNodeIdx);
                switch (textNode.getNodeType()) {
                case Node.TEXT_NODE:
                    key = textNode.getNodeValue();
                    break;
                default:
                    throw new LLSDException("Unexpected node \""
                        + textNode.getNodeName() + "\" found while parsing key for map.");
                }
            }

            value = parseNode(valueNode);
            assert null != value;

            valueMap.put(key, value);
        }

        return valueMap;
    }

    private Object parseNode(final Node node)
        throws LLSDException {
        boolean isUndefined = false;
        final NodeList childNodes;
        final String nodeName = node.getNodeName().toLowerCase();
        final StringBuilder nodeText;

        childNodes = node.getChildNodes();

        // Handle compound types (array and map) and stupid decisions by Linden
        // Labs (binary).
        if (nodeName.equals("array")) {
            return parseArray(childNodes);
        } else if (nodeName.equals("map")) {
            return parseMap(childNodes);
        }

        nodeText = new StringBuilder();
        for (int nodeIdx = 0; nodeIdx < childNodes.getLength(); nodeIdx++) {
            final Node childNode = childNodes.item(nodeIdx);

            switch (childNode.getNodeType()) {
            case Node.TEXT_NODE:
                nodeText.append(childNode.getNodeValue());
                break;
            case Node.ELEMENT_NODE:
                if (childNode.getNodeName().equals("undef")) {
                    isUndefined = true;
                }
                break;
            default:
                break;
            }
        }

        switch(nodeName) {
            case "undef":
                return "";
            case "boolean":
                return isUndefined
                        ? LLSDUndefined.BOOLEAN
                        : parseBoolean(nodeText.toString());
            case "date":
                return isUndefined
                        ? LLSDUndefined.DATE
                        : parseDate(nodeText.toString());
            case "integer":
                return isUndefined
                        ? LLSDUndefined.INTEGER
                        : parseInteger(nodeText.toString());
            case "real":
                return isUndefined
                        ? LLSDUndefined.REAL
                        : parseReal(nodeText.toString());
            case "string":
                return isUndefined
                        ? LLSDUndefined.STRING
                        : parseString(nodeText.toString());
            case "uri":
                return isUndefined
                        ? LLSDUndefined.URI
                        : parseURI(nodeText.toString());
            case "uuid":
                return isUndefined
                        ? LLSDUndefined.UUID
                        : parseUUID(nodeText.toString());
            case "binary":
                NamedNodeMap attrs = node.getAttributes();
                switch (attrs.getNamedItem("encoding").getNodeValue()) {
                    case "base64":
                        return isUndefined
                                ? LLSDUndefined.BINARY
                                : parseBinary(nodeText.toString());
                    default:
                        throw new LLSDException("encoding other than base64 is not supported");
                }
            default:
                throw new LLSDException("Encountered unexpected node \""
                        + node.getNodeName() + "\".");
        }
    }

    private Double parseReal(final String elementContents)
        throws LLSDException {
        final Double value;

        if (elementContents.length() == 0) {
            return 0.0;
        }

        if (elementContents.equals("nan")) {
            return Double.NaN;
        }

        try {
            // XXX: Don't just assume the default parser is okay for SSLD, check
            value = new Double(elementContents);
        } catch(NumberFormatException e) {
            throw new LLSDException("Unable to parse LLSD real value, received \""
                + elementContents + "\".", e);
        }

        return value;
    }

    private String parseString(final String elementContents)
        throws LLSDException {
        return elementContents;
    }

    private URI parseURI(final String elementContents)
        throws LLSDException {
        final URI value;

        try {
            value = new URI(elementContents);
        } catch(java.net.URISyntaxException e) {
            throw new LLSDException("Unable to parse LLSD URI value, received \""
                + elementContents + "\".", e);
        }

        return value;
    }

    private UUID parseUUID(final String elementContents)
        throws LLSDException {
        final UUID value;

        if (elementContents.length() == 0) {
            return new UUID(0L, 0L);
        }

        try {
            value = UUID.fromString(elementContents);
        } catch(IllegalArgumentException e) {
            throw new LLSDException("Unable to parse LLSD UUID value, received \""
                + elementContents + "\".", e);
        }

        return value;
    }

    private byte[] parseBinary(final String elementContents) throws LLSDException {
        if (elementContents.length() == 0) {
            return new byte[]{};
        }
        try {
            return Base64.getDecoder().decode(elementContents.getBytes());
        } catch (IllegalArgumentException e) {
            throw new LLSDException("Unable to parse LLSD binary value. Contents omitted in exception.");
        }
    }
}
