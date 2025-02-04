package com.example.solaceconnector;
import org.apache.flink.table.data.GenericRowData;
import org.apache.flink.table.data.RowData;
// import org.apache.flink.table.formats.DeserializationSchema;
import org.apache.flink.api.common.serialization.DeserializationSchema.InitializationContext;
import org.apache.flink.streaming.util.serialization.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solacesystems.jms.message.SolTextMessage;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.commons.lang3.SerializationUtils;


import java.io.IOException;

public class DynamicMsgDeserializer implements DeserializationSchema<RowData> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void open(InitializationContext context) throws Exception {
        // Initialization logic if needed
    }

    @Override
    public RowData deserialize(byte[] message) throws IOException {
        try {
            // Deserialize SolTextMessage (assuming it's serialized as bytes)
            SolTextMessage solMessage = (SolTextMessage) SerializationUtils.deserialize(message);
            String payload = solMessage.getText(); // Get the raw payload

            // Parse JSON payload
            JsonNode jsonNode = objectMapper.readTree(payload);

            // Create RowData dynamically
            GenericRowData rowData = new GenericRowData(jsonNode.size());
            int index = 0;
            for (JsonNode field : jsonNode) {
                rowData.setField(index++, field.asText());
            }
            return rowData;

        } catch (Exception e) {
            throw new IOException("Error deserializing SolTextMessage dynamically", e);
        }
    }

    @Override
    public boolean isEndOfStream(RowData nextElement) {
        return false;
    }

    @Override
    public TypeInformation<RowData> getProducedType() {
        return TypeInformation.of(RowData.class);
    }
}

