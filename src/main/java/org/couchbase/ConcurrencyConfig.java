package org.couchbase;

/**
 * This defines the concurrency parameters for executor thread pool, producer, consumer, mock data generation and bulk insert parallelism.
 *
 * @author abhijeetbehera
 */
public class ConcurrencyConfig {

    // Executor thread pool config and producer consumer thread range
    public static final int EXECUTOR_THREAD_POOL = 10;

    public static final int CONSUMER_START_RANGE = 0;
    public static final int CONSUMER_END_RANGE = 10;

    // Mock data generation parallelism
    public static final int MOCK_DATA_START_RANGE = 1;
    public static final int MOCK_DATA_END_RANGE = 1;
    public static final int MOCK_DATA_PARALLELISM = 1;

    // Bulk insert parallelism
    public static final int BULK_INSERT_CONCURRENT_OPS = 1;
}
