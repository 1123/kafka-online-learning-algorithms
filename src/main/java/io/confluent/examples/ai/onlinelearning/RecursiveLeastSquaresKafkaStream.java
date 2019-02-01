package io.confluent.examples.ai.onlinelearning;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import smile.regression.RLS;

/**
 * <a href="https://en.wikipedia.org/wiki/Online_machine_learning">Online Machine Learning</a> algorithms are
 * particularly well suited to train models within Event Streaming architectures,
 * since they can process one event at a time, while continuously updating the trained model.
 *
 * This test case demonstrates how to train a
 * <a href="https://en.wikipedia.org/wiki/Linear_least_squares">Linear least Squares</a> estimator using the
 * <a href="https://en.wikipedia.org/wiki/Recursive_least_squares_filter">Recursive Least Squares</a>
 * method within a Kafka Streams application. Both the input stream data and the output model are received / published
 * from and to Apache Kafka.
 *
 * This example uses the recursive least squares implementation from
 * the <a href="https://github.com/haifengl/smile/">smile project</a>.
 *
 */

class RecursiveLeastSquaresKafkaStream {

	static StreamsBuilder createStream(
			double[][] x, double[] y, double lambda, String trainingDataTopic, String modelOutputTopic
	) {
		RLS rls = new RLS(x, y, lambda);
		final StreamsBuilder builder = new StreamsBuilder();
		final KStream<String, String> rlsStream =
				builder.stream(trainingDataTopic);
		rlsStream.mapValues(value -> {
			String[] lineSplit = value.split(",");
			double [] xs = { Double.parseDouble(lineSplit[0]) };
			rls.learn(
					xs,
					Double.parseDouble(lineSplit[1])
			);
			return String.format(
					"{\"slope\": %f, \"intercept\": %f}",
					rls.coefficients()[0],
					rls.coefficients()[1]
			);
		}).to(modelOutputTopic);
		return builder;
	}

}
