package com.example.solaceconnector;

import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.data.StringData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.ByteArrayInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.jms.*;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import com.solacesystems.jms.message.SolTextMessage;
import com.example.solaceconnector.MessageProcessor;
// import io.netty.channel.ChannelOutboundBuffer.MessageProcessor;
import com.example.solaceconnector.message_builder.JMSHeadersBuilder;
import com.example.solaceconnector.message_builder.JMSPayloadBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

public class SolaceJMSQueueSource extends RichSourceFunction<RowData> {

    private final String brokerUrl;
    private final String vpnName;
    private final String username;
    private final String password;
    private final String queue;

    private volatile boolean isRunning = true;
    private transient Connection connection;
    private transient Session session;
    private transient MessageConsumer consumer;

    public SolaceJMSQueueSource(String brokerUrl, String vpnName, String username, String password, String queue) {
        this.brokerUrl = brokerUrl;
        this.vpnName = vpnName;
        this.username = username;
        this.password = password;
        this.queue = queue;
    }

    private static final Logger LOG = LoggerFactory.getLogger(SolaceJMSQueueSource.class);

    @Override
    public void run(SourceContext<RowData> ctx) throws Exception {
        // Step 1: Create JMS Connection Factory
        SolConnectionFactory connectionFactory = SolJmsUtility.createConnectionFactory();
        connectionFactory.setHost(brokerUrl);
        connectionFactory.setVPN(vpnName);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
    
        // Step 2: Create JMS Connection
        connection = connectionFactory.createConnection();
        connection.start();
    
        // Step 3: Create JMS Session
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    
        // Step 4: Create JMS Queue
        Queue solaceQueue = session.createQueue(queue);
    
        // Step 5: Browse messages using QueueBrowser
        /* browser = session.createBrowser(solaceQueue);
        Enumeration<Message> messages = browser.getEnumeration();
        while (messages.hasMoreElements()) {
            Message message = messages.nextElement();
            LOG.info("INSIDE BROWSER WHILE");
            processMessage(message, ctx, "BROWSED");
        }*/
    
        // Step 6: Consume messages using MessageConsumer //UNCOMMENT FOR CONSUMPTion
        consumer = session.createConsumer(solaceQueue);
        while (isRunning) {
            try {
                Message message = consumer.receive(1000); // Timeout of 1000 ms
                if (message != null) {
                    processMessage(message, ctx, "CONSUMED");
                } else {
                    LOG.info("No messages available for consumption");
                }
            } catch (Exception e) {
                LOG.error("Error while consuming messages", e);
            }
        }
    }
    
    // Reusable method to process messages
    private void processMessage(Message message, SourceContext<RowData> ctx, String mode) {
        try {
//            LOG.info("{} PRINTING MODE AND Message: {}", mode, message);
    
            //TEST EXTRACTING MESSAGE ID.
            String messageID = message.getJMSMessageID();
            String GG_ID = message.getStringProperty("GG_ID");

            //Initialize Headers map.
            Map<String, String> headers = new HashMap<>();

            //Get all the JMS Properties.
            Enumeration<String> propertyNames = message.getPropertyNames();

            // Extract headers from Sol Message Object.
            String headersJson = getHeaders(message, headers, propertyNames);
    
            //Initialize empty payload.
            String payload = "NULL";
            // Extract payload
            payload = getPayload(message, payload);

            // Create RowData for Flink
            GenericRowData rowData = GenericRowData.of(
                StringData.fromString(GG_ID),
                StringData.fromString(messageID),
                StringData.fromString(headersJson),
                StringData.fromString(payload)
            );

            //BROWSED DUMP MESSAGE
           /* String browsedMessg = "NULL";
            browsedMessg=SolJmsUtility.dumpMessage(message);
            LOG.info("THIS IS THE dumpMessage output::::BROWSED-MESSAGE::::",browsedMessg);*/

            // Emit RowData to the context only for consumed messages
            if ("CONSUMED".equals(mode)) {
                synchronized (ctx.getCheckpointLock()) {
                    ctx.collect(rowData);
                }
            }
        } catch (Exception e) {
            LOG.error("Error while processing {} message", mode, e);
        }
    }

    private static String getPayload(Message message, String payload) throws JMSException {
        if (message instanceof TextMessage) {
            payload = ((TextMessage) message).getText();
        }
        return payload;
    }

    private String getHeaders(Message message, Map<String, String> headers, Enumeration<String> propertyNames)
            throws JMSException, JsonProcessingException {
        while (propertyNames.hasMoreElements()) {
            String propertyName = propertyNames.nextElement();
            String propertyValue = message.getStringProperty(propertyName);
            headers.put(propertyName, propertyValue);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String headersJson = objectMapper.writeValueAsString(headers);
        return headersJson;
    }
    
    @Override
    public void cancel() {
        isRunning = false;
        try {
            if (consumer != null)
                consumer.close();
            if (session != null)
                session.close();
            if (connection != null)
                connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
