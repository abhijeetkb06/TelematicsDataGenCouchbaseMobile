package org.couchbase;

import com.couchbase.client.java.json.JsonObject;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.IntStream;

/**
 * This initiates the process of telematics data load generation in Couchbase.
 * 
 * @author abhijeetbehera
 */
public class Launcher {

	public static void main(String[] args) {

		ExecutorService executorService = Executors.newFixedThreadPool(ConcurrencyConfig.EXECUTOR_THREAD_POOL);

		IntStream.range(ConcurrencyConfig.CONSUMER_START_RANGE, ConcurrencyConfig.CONSUMER_END_RANGE)
				.forEach(i -> {
					executorService.execute(new TelematicsDataLoader());
				});
	}
}
