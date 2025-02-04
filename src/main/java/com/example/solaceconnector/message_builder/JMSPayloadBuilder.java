package com.example.solaceconnector.message_builder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.BytesMessage;
import java.io.ByteArrayInputStream;

public class JMSPayloadBuilder {

    private static final XmlMapper xmlMapper = new XmlMapper();

    /**
     * Extracts the SolPayload from a JMS Message and parses it as a JSON object.
     *
     * @param message JMS Message object.
     * @return Parsed JsonNode representation of the payload.
     * @throws Exception If extraction or parsing fails.
     */
    public static JsonNode extractAndParseSolPayload(Message message) throws Exception {
        String solPayload = null;

        // Extract SolPayload depending on message type
        if (message instanceof TextMessage) {
            solPayload = ((TextMessage) message).getText();
        } else if (message instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) message;
            byte[] data = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(data);
            solPayload = new String(data); // Convert bytes to string
        } else {
            throw new IllegalArgumentException("Unsupported message type: " + message.getClass());
        }

        // Parse the SolPayload XML into JSON
        return parseXmlToJson(solPayload);
    }

    /**
     * Parses an XML string into a JsonNode using Jackson's XmlMapper.
     *
     * @param xmlString The XML content as a string.
     * @return JsonNode representation of the XML.
     * @throws Exception If parsing fails.
     */
    private static JsonNode parseXmlToJson(String xmlString) throws Exception {
        return xmlMapper.readTree(new ByteArrayInputStream(xmlString.getBytes()));
    }
}
