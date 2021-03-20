package com.ggordon.schad.kafka_producer;

import com.ggordon.schad.kafka_producer.cli.CliArguments;
import com.ggordon.schad.kafka_producer.consumers.SocketTextStreamConsumer;
import com.sampullara.cli.Args;

public class Driver {
	
	public static void main(String[] rawArgs) {
		Args.parseOrExit(CliArguments.class,rawArgs);
		SocketTextStreamConsumer consumer = new SocketTextStreamConsumer(
				CliArguments.clickStreamServerAddress,
				CliArguments.clickStreamServerPort,
				String.join(",", CliArguments.kafkaBootstrapServers),
				CliArguments.kafkaTopic
        );
		consumer.listen();
	}

}
