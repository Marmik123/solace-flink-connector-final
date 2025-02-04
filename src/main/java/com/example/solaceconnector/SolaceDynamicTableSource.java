package com.example.solaceconnector;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.table.connector.ChangelogMode;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.connector.source.ScanTableSource;
import org.apache.flink.table.connector.source.SourceProvider;
import org.apache.flink.api.connector.source.Source;
import org.apache.flink.table.connector.source.SourceFunctionProvider;
import org.apache.flink.table.data.RowData;



public class SolaceDynamicTableSource implements ScanTableSource {

    private final ReadableConfig config;
    
    public SolaceDynamicTableSource(ReadableConfig config) {
        this.config = config;
    }

    @Override
    public ScanRuntimeProvider getScanRuntimeProvider(ScanContext runtimeProviderContext) {
        // DeserializationSchema<RowData> deserializationSchema = new SolaceJmsDeserializationSchema(config);

          // Extract the necessary configuration options from the config
        String brokerUrl = config.get(SolaceOptions.BROKER_URL);
        String vpnName = config.get(SolaceOptions.VPN_NAME);
        String username = config.get(SolaceOptions.USERNAME);
        String password = config.get(SolaceOptions.PASSWORD);
        String topic = config.get(SolaceOptions.TOPIC);
        String queue = config.get(SolaceOptions.QUEUE_NAME);

        // Create the Solace JMS Source Function
        // Source<RowData,?,?> solaceSource = new SolaceJMSSourceFunction(brokerUrl, vpnName, username, password, topic,queue);
        SolaceJMSQueueSource sourceFunction = new SolaceJMSQueueSource(
            brokerUrl, vpnName, username, password, queue
        );


        return SourceFunctionProvider.of(sourceFunction,false);
    }

    @Override
    public ChangelogMode getChangelogMode() {
        return ChangelogMode.insertOnly();
    }

    @Override
    public DynamicTableSource copy() {
        return new SolaceDynamicTableSource(config);
    }

    @Override
    public String asSummaryString() {
        return "Solace JMS Source";
    }
}

