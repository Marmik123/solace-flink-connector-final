package com.example.solaceconnector;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.TextMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.messaging.Message;
import com.solacesystems.jms.message.SolTextMessage;

public class MessageProcessor {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String processMessage(Message message) {
        Map<String, Object> messageData = new HashMap<>();
        try {
            messageData.put("messageType", message.getClass().getSimpleName());
            messageData.put("messageId", message.getApplicationMessageId());

            if (message instanceof SolTextMessage) {
                SolTextMessage solTextMessage = (SolTextMessage) message;
                messageData.put("text", solTextMessage.getText());
            } else if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                messageData.put("text", textMessage.getText());
            } else if (message instanceof BytesMessage) {
                BytesMessage bytesMessage = (BytesMessage) message;
                byte[] data = new byte[(int) bytesMessage.getBodyLength()];
                bytesMessage.readBytes(data);
                messageData.put("binaryPayload", data);
            } else if (message instanceof MapMessage) {
                MapMessage mapMessage = (MapMessage) message;
                Map<String, Object> mapData = new HashMap<>();
                Enumeration<String> keys = mapMessage.getMapNames();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement();
                    mapData.put(key, mapMessage.getObject(key));
                }
                messageData.put("mapPayload", mapData);
            } else {
                messageData.put("rawPayload", message.toString());
            }

            // Convert the map to a JSON string
            return objectMapper.writeValueAsString(messageData);

        } catch (JMSException | IOException e) {
            e.printStackTrace();
            return "Error processing message: " + e.getMessage();
        }
    }
}
