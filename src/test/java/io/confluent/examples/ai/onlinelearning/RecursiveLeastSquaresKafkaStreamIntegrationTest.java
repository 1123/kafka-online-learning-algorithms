package io.confluent.examples.ai.onlinelearning;

import kafka.utils.MockTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.integration.utils.EmbeddedKafkaCluster;
import org.apache.kafka.streams.integration.utils.IntegrationTestUtils;
import org.apache.kafka.test.TestUtils;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import smile.regression.RLS;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Slf4j
public class RecursiveLeastSquaresKafkaStreamIntegrationTest {

	@ClassRule
	public static final EmbeddedKafkaCluster CLUSTER = new EmbeddedKafkaCluster(1);

	private static final String TRAINING_DATA_TOPIC = "training-data-topic";
	private static final String MODEL_OUTPUT_TOPIC = "model-output-topic";

	private List<String> inputValues = Arrays.asList(
			"3,2",
			"4,2",
			"5,3",
			"6,3",
			"7,4",
			"8,4",
			"9,5",
			"10,5",
			"11,6",
			"12,6"
	);

	private RLS rls;

	@BeforeClass
	public static void startKafkaCluster() throws Exception {
		CLUSTER.createTopic(TRAINING_DATA_TOPIC);
		CLUSTER.createTopic(MODEL_OUTPUT_TOPIC);
	}

	private Properties streamsConfiguration() {
		Properties streamsConfiguration = new Properties();
		streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-rls-integration-test");
		streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
		streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
		streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, TestUtils.tempDirectory().getAbsolutePath());
		streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		return streamsConfiguration;
	}

	@Test
	public void theRecursiveLeastSquaresKafkaStreamShouldIncrementallyUpdateTheSlopeAndInterceptParameters()
			throws Exception {
		double[][] x = {{1.0}, {2.0}};
		double [] y = {1.0, 1.0};
		double learningFactor = 0.95;
		StreamsBuilder builder =
				RecursiveLeastSquaresKafkaStream.createStream(
						x, y, learningFactor, TRAINING_DATA_TOPIC, MODEL_OUTPUT_TOPIC
				);
		KafkaStreams streams = new KafkaStreams(builder.build(), streamsConfiguration());
		streams.cleanUp();
		streams.start();
		produceData();
		verify();
		streams.close();
	}

	private void produceData() throws ExecutionException, InterruptedException {
		Properties producerConfig = new Properties();
		producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
		producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
		producerConfig.put(ProducerConfig.RETRIES_CONFIG, 0);
		producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		IntegrationTestUtils.produceValuesSynchronously(TRAINING_DATA_TOPIC, inputValues, producerConfig, new MockTime());
	}

	private void verify() throws InterruptedException {
		Properties consumerConfig = new Properties();
		consumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, CLUSTER.bootstrapServers());
		consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG,
				"machine-learning-example-integration-test-standard-consumer");
		consumerConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		List<KeyValue<String, String>> response = IntegrationTestUtils
 				.waitUntilMinKeyValueRecordsReceived(consumerConfig, MODEL_OUTPUT_TOPIC, inputValues.size());
		response.forEach(keyVal -> log.info(keyVal.toString()));
	}

}

