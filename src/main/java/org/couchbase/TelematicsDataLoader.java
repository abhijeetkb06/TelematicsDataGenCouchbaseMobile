package org.couchbase;

import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.MutationResult;
import com.couchbase.lite.*;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * This initiates the process of bulk data insert in Couchbase using reactive apis.
 *
 * @author abhijeetbehera
 */
public class TelematicsDataLoader extends Thread {

	public TelematicsDataLoader() {
		super("DATA LOADER");
	}

	public void run() {
		try {
			while (true) {
				
				System.out.println("***************GENERATE MOCK DATA************** ");

				// Remove the user from shared queue and process
//				bulkInsert(tasksQueue.take());

				bulkInsert(generateMockDataParallel());
				System.out.println(" MOCK DATA GENERATED \n");
				System.out.println(" Thread Name: " + Thread.currentThread().getName());
//				Thread.sleep(100);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<MutableDocument> generateMockDataParallel() {

		return Flux.range(ConcurrencyConfig.MOCK_DATA_START_RANGE, ConcurrencyConfig.MOCK_DATA_END_RANGE)
				.parallel(ConcurrencyConfig.MOCK_DATA_PARALLELISM)
				.runOn(Schedulers.parallel())
				.map(i -> generateMockData(i))
				.doOnError(e -> Flux.empty())
				.sequential()
				.collectList()
				.block();
	}

	private static MutableDocument generateMockData(int index) {
		Random random = new Random();

		MutableDocument mutableDocument = new MutableDocument();

		mutableDocument.setString("UserId", UUID.randomUUID().toString());
		mutableDocument.setString("Username", "User" + random.nextInt(1000));
		mutableDocument.setString("Email", "user" + index + "@netflix.com");
		mutableDocument.setString("SubscriptionType", random.nextBoolean() ? "Premium" : "Standard");

		MutableArray favoriteGenres = new MutableArray();
		favoriteGenres.addString("Action");
		favoriteGenres.addString("Comedy");
		favoriteGenres.addString("Drama");
		mutableDocument.setArray("FavoriteGenres", favoriteGenres);

		MutableDictionary preferences = new MutableDictionary();
		preferences.setString("Language", random.nextBoolean() ? "English" : "Spanish");
		preferences.setInt("ParentalControlLevel", random.nextInt(5)); // 0-4 rating level
		mutableDocument.setDictionary("Preferences", preferences);

		return mutableDocument;
	}


	/*private static MutableDocument generateMockData(int index) {
		Random random = new Random();

		MutableDocument mutableDocument = new MutableDocument();

		mutableDocument.setString("MessageId", UUID.randomUUID().toString());
		mutableDocument.setString("DeviceId", "vehicle" + random.nextInt(100));
		mutableDocument.setString("EventTime", Instant.now().toString());

		MutableArray orgsArray = new MutableArray();
		orgsArray.addString("org1" + random.nextInt(1000));
		orgsArray.addString("org2" + random.nextInt(1000));
		mutableDocument.setArray("Orgs", orgsArray);

		MutableDictionary payload = new MutableDictionary();

		MutableDictionary telematics = new MutableDictionary();
		telematics.setString("vehicleId", "ABC" + random.nextInt(1000));

		MutableDictionary location = new MutableDictionary();
		location.setDouble("latitude", 30 + random.nextDouble() * 20);
		location.setDouble("longitude", -120 + random.nextDouble() * 60);
		telematics.setDictionary("location", location);

		telematics.setInt("speed", random.nextInt(100));
		telematics.setInt("fuelLevel", random.nextInt(100));
		telematics.setString("engineStatus", random.nextBoolean() ? "running" : "stopped");

		MutableDictionary tirePressure = new MutableDictionary();
		tirePressure.setInt("frontLeft", 28 + random.nextInt(10));
		tirePressure.setInt("frontRight", 28 + random.nextInt(10));
		tirePressure.setInt("rearLeft", 28 + random.nextInt(10));
		tirePressure.setInt("rearRight", 28 + random.nextInt(10));
		telematics.setDictionary("tirePressure", tirePressure);

		MutableDictionary driver = new MutableDictionary();
		driver.setString("name", "Driver" + random.nextInt(1000));
		driver.setString("licenseNumber", "ABC" + random.nextInt(1000));
		driver.setString("status", random.nextBoolean() ? "active" : "inactive");
		telematics.setDictionary("driver", driver);

		payload.setDictionary("telematics", telematics);
		mutableDocument.setDictionary("Payload", payload);

		return mutableDocument;
	}*/

	private void bulkInsert(List<MutableDocument> data) {
		AppServicesDatabaseManager dbConfig = AppServicesDatabaseManager.getInstance();
		Database db = dbConfig.getCBLiteDatabase();
		List<MutationResult> results = new ArrayList<>();

		for (MutableDocument doc : data) {
			try {
				db.save(doc);
			} catch (CouchbaseLiteException e) {
				e.printStackTrace();
			}
		}

		/*return Flux.fromIterable(data)
				.parallel(ConcurrencyConfig.BULK_INSERT_CONCURRENT_OPS)
				.runOn(Schedulers.boundedElastic()) // or one of your choice
				.flatMap(doc -> dbConfig.getCBLiteDatabase().save(doc))
		        .doOnError(e -> Flux.empty())
				.sequential()
				.collectList()
				.block();*/
	}
}
