# Solace connector for Flink

This connector integrates Solace JMS with Apache Flink, allowing users to:
✅ Stream data from Solace queues into Flink
✅ Use Flink SQL to query Solace data
✅ Convert Solace messages into Flink's RowData format

1️⃣ SolaceOptions.java
Purpose:
Defines the configuration options for connecting to a Solace JMS broker.

Key Configurations:

BROKER_URL → Solace broker's URL
VPN_NAME → VPN name for Solace
QUEUE_NAME → Queue name to consume messages from
TOPIC → Topic name (if topic-based consumption is used)
USERNAME & PASSWORD → Credentials for authentication
FORMAT → Expected message format (default: JSON)
Usage:
These options are referenced in other classes to configure the connection dynamically.

2️⃣ SolaceJMSQueueSource.java
Purpose:
This is a Flink source function that consumes messages from a Solace JMS Queue and converts them into Flink RowData.

Key Features:
✅ Establishes a JMS connection using Solace JMS API
✅ Consumes messages from a Solace queue
✅ Extracts headers and payload from messages
✅ Converts messages into Flink RowData
✅ Implements RichSourceFunction<RowData> for better state management

3️⃣ SolaceJMSFactory.java
Purpose:
Implements Flink's Table Source Factory (DynamicTableSourceFactory) to register Solace as a custom Flink SQL Table Source.

Key Features:
✅ Registers Solace JMS as a table source
✅ Defines required & optional config parameters
✅ Creates a SolaceDynamicTableSource instance

4️⃣ SolaceDynamicTableSource.java
Purpose:
Implements ScanTableSource to provide runtime execution logic for consuming Solace messages in Flink SQL.

Key Features:
✅ Extracts Solace connection parameters
✅ Creates SolaceJMSQueueSource to consume messages
✅ Supports only "INSERT" mode currently (ChangelogMode.insertOnly())

## How These Classes Work Together
SolaceOptions → Defines configuration settings
SolaceJMSQueueSource → Implements the actual Flink source function for consuming Solace messages
SolaceJMSFactory → Registers Solace as a table source in Flink SQL
SolaceDynamicTableSource → Bridges between Flink SQL and JMS consumption
