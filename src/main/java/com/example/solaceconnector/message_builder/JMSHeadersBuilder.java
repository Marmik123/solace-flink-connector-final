package com.example.solaceconnector.message_builder;

import javax.jms.Message;
import javax.jms.JMSException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JMSHeadersBuilder {

    public static String buildHeadersJson(Message message) throws JMSException {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> propertyNames = message.getPropertyNames();
        
        // Iterate over all JMS Properties
        while (propertyNames.hasMoreElements()) {
            String propertyName = propertyNames.nextElement();
            String propertyValue = message.getStringProperty(propertyName); // Fetch the property value
            headers.put(propertyName, propertyValue); // Add to headers map
        }

        // Convert headers map to JSON
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(headers);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize headers to JSON", e);
        }
    }
}

