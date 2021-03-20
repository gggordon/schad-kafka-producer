package com.ggordon.schad.kafka_producer.consumers;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.ggordon.schad.kafka_producer.transformers.ClickStreamRecordTextTransformer;

public class SocketTextStreamConsumer {
	private String host;
	private int port;
	private String kafkaBootstrapServers;
	private String kafkaTopic;

	public SocketTextStreamConsumer(String clickStreamServerAddress, Integer clickStreamServerPort, String kafkaBootstrapServers, String kafkaTopic) {
		this.host = clickStreamServerAddress;
		this.port = clickStreamServerPort;
		this.kafkaBootstrapServers = kafkaBootstrapServers;
		this.kafkaTopic = kafkaTopic;
	}

	
	

	public void listen() {
		Scanner streamReader = null;
		Socket client = null;
		try {
			System.out.println("Connecting to "+host+" "+port);
			client = new Socket(host, port);
			streamReader = new Scanner(client.getInputStream());
			while (true) {
				String line = streamReader.nextLine();
				System.out.println(line);
				publishToKafKa(line);
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (client != null)
				client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void publishToKafKa(String line) {
		
		Thread b = new Thread(new Runnable() {
			public void run() {
				
				ProducerRecord<String, String> record = ClickStreamRecordTextTransformer.convertToProducerRecord(
						ClickStreamRecordTextTransformer.convertToProducerRecordData(
								ClickStreamRecordTextTransformer.convertToRecord(line)
						),
						kafkaTopic);
                try {
                	Producer<String, String> producer = getProducer();
					RecordMetadata metadata = producer.send(record).get();
					//producer.flush();
					//producer.close();
					System.out.println("Sent record ("+line+") : "+metadata.offset());
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		b.start();

	}

	private static Producer<String, String> producerInstance = null;
	public Producer<String, String> getProducer() {
		if(producerInstance == null) {
		Properties props = new Properties();
		props.put("bootstrap.servers", kafkaBootstrapServers);
		props.put("acks", "all");

		// If the request fails, the producer can automatically retry,
		props.put("retries", 0);

		// Specify buffer size in config
		props.put("batch.size", 16384);

		// Reduce the no of requests less than 0
		props.put("linger.ms", 1);

		// The buffer.memory controls the total amount of memory available to the
		// producer for buffering.
		props.put("buffer.memory", 33554432);

		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		
		producerInstance = new KafkaProducer
		         <String, String>(props);
		}
		return producerInstance;
	}

}
