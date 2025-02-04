package com.example.solaceconnector;


import org.apache.flink.table.factories.DynamicTableSourceFactory;
import org.apache.flink.table.factories.FactoryUtil;
import org.slf4j.Logger;

import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.configuration.ConfigOption;

import org.apache.flink.table.connector.source.DynamicTableSource;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.LoggerFactory;

public class SolaceJMSFactory implements DynamicTableSourceFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SolaceJMSFactory.class);
    public static final String IDENTIFIER = "solace";

    @Override
    public String factoryIdentifier() {
        LOG.debug("Factory identifier requested: solace");
        return IDENTIFIER;
    }

    @Override
    public Set<ConfigOption<?>> requiredOptions() {
        Set<ConfigOption<?>> requiredOptions = new HashSet<>();
        requiredOptions.add(SolaceOptions.BROKER_URL);
        requiredOptions.add(SolaceOptions.VPN_NAME);
        requiredOptions.add(SolaceOptions.USERNAME);
        requiredOptions.add(SolaceOptions.PASSWORD);
        requiredOptions.add(SolaceOptions.QUEUE_NAME);
        return requiredOptions;
    }

    @Override
    public Set<ConfigOption<?>> optionalOptions() {
        Set<ConfigOption<?>> optionalOptions = new HashSet<>();
        optionalOptions.add(SolaceOptions.TOPIC);
        optionalOptions.add(SolaceOptions.FORMAT);
        return optionalOptions;
    }

    @Override
    public DynamicTableSource createDynamicTableSource(Context context) {
        final FactoryUtil.TableFactoryHelper
            factoryHelper = FactoryUtil.createTableFactoryHelper(this, context);
        factoryHelper.validate();    
        ReadableConfig config = FactoryUtil.createTableFactoryHelper(this, context).getOptions();
        
        // Validate required options
        // if (!config.get(SolaceOptions.BROKER_URL).startsWith("tcp://")) {
        //     throw new ValidationException("Broker URL must start with tcp://");
        // }

        return new SolaceDynamicTableSource(config);
    }
}