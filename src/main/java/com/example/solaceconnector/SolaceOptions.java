package com.example.solaceconnector;

import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;

public class SolaceOptions {
    public static final ConfigOption<String> BROKER_URL =
            ConfigOptions.key("host")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The URL of the Solace broker.");
    
     public static final ConfigOption<String> VPN_NAME = ConfigOptions
            .key("vpn")
            .stringType()
            .noDefaultValue()
            .withDescription("The VPN name for the Solace connection");                


    public static final ConfigOption<String> QUEUE_NAME =
            ConfigOptions.key("queue")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The name of the Solace queue.");

    public static final ConfigOption<String> TOPIC = ConfigOptions
                    .key("topic")
                    .stringType()
                    .noDefaultValue()
                    .withDescription("The VPN name for the Solace connection");                

    public static final ConfigOption<String> USERNAME =
            ConfigOptions.key("username")
                    .stringType()
                    .defaultValue("admin")
                    .withDescription("The username for connecting to the Solace broker.");

    public static final ConfigOption<String> PASSWORD =
            ConfigOptions.key("password")
                    .stringType()
                    .defaultValue("admin")
                    .withDescription("The password for connecting to the Solace broker.");

    public static final ConfigOption<String> FORMAT =
            ConfigOptions.key("format")
                    .stringType()
                    .defaultValue("json")
                    .withDescription("The format to deserialize messages.");
}
