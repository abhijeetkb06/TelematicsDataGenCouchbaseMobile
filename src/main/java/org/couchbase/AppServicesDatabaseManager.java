package org.couchbase;

import com.couchbase.lite.*;
import com.couchbase.lite.DatabaseConfiguration;
import reactor.util.annotation.NonNull;

import java.net.URI;
import java.net.URISyntaxException;

public class AppServicesDatabaseManager {
    private static final String APPSERVICES_USER = "abhijeet";
    private static final String APPSERVICES_PW = "Password@P1";
    private static Database telematicsCBLiteDatabase;
    private static String cbLiteDbName = "telematicsdata";

    private static AppServicesDatabaseManager instance = null;

    public static String appServicesEndpoint = "wss://d8n93kd4tzs3gokq.apps.cloud.couchbase.com:4984/userprofile";

    private static ListenerToken listenerToken;

    private static Replicator replicator;
    private static ListenerToken replicatorListenerToken;

    private AppServicesDatabaseManager() {

    }

    public static AppServicesDatabaseManager getInstance() {
        if (instance == null) {

            instance = new AppServicesDatabaseManager();
            CouchbaseLite.init();
            openOrCreateDatabase();
            startPushAndPullReplicationForCurrentUser(APPSERVICES_USER,APPSERVICES_PW);
        }

        return instance;
    }

    public Database getCBLiteDatabase() {
        return telematicsCBLiteDatabase;
    }

    public static void setAppServicesEndpoint(String appServicesUrl) {
        appServicesEndpoint = appServicesUrl;
    }

    private static void openOrCreateDatabase()
    {
        // Create a database
        DatabaseConfiguration cfg = new DatabaseConfiguration();

        try {
            telematicsCBLiteDatabase = new Database(cbLiteDbName, cfg);
            registerForDatabaseChanges();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    private static void registerForDatabaseChanges()
    {
        // Add database change listener
        listenerToken = telematicsCBLiteDatabase.addChangeListener(new DatabaseChangeListener() {
            @Override
            public void changed(final DatabaseChange change) {
                if (change != null) {
                    for(String docId : change.getDocumentIDs()) {
                        Document doc = telematicsCBLiteDatabase.getDocument(docId);
                        if (doc != null) {
                            System.out.println("DatabaseChangeEvent"+ "Document was added/updated");
                        }
                        else {

                            System.out.println("DatabaseChangeEvent"+"Document was deleted");
                        }
                    }
                }
            }
        });
    }

    public static void startPushAndPullReplicationForCurrentUser(String username, String password)

    {
        URI url = null;
        try {
            // This is sync gate way url
//            url = new URI(String.format("%s/%s", syncGatewayEndpoint, userProfileDbName));
            // This is app service url format
            url = new URI(String.format("%s", appServicesEndpoint));
            System.out.println("URL: " + url.toString());
        } catch (URISyntaxException e) {
            System.out.println("URL exception: " + url.toString());
            e.printStackTrace();
        }

        ReplicatorConfiguration config = new ReplicatorConfiguration(telematicsCBLiteDatabase, new URLEndpoint(url)); // <1>
        config.setType(ReplicatorType.PUSH_AND_PULL); // <2>
        config.setContinuous(true); // <3>

        config.setAuthenticator(new BasicAuthenticator(username, password.toCharArray())); // <4>
        //TODO: Configure channel and uncomment
//        config.setChannels(Arrays.asList("channel." + username)); // <5>

        replicator = new Replicator(config);

        replicatorListenerToken = replicator.addChangeListener(new ReplicatorChangeListener() {
            @Override
            public void changed(ReplicatorChange change) {

                if (change.getReplicator().getStatus().getActivityLevel().equals(ReplicatorActivityLevel.IDLE)) {
                    System.out.println("Replication IDLE Log");
                }
                if (change.getReplicator().getStatus().getActivityLevel().equals(ReplicatorActivityLevel.STOPPED)
                        || change.getReplicator().getStatus().getActivityLevel().equals(ReplicatorActivityLevel.OFFLINE)) {
                    System.out.println("Replication STOPPED Log");
                }
            }
        });

        replicatorListenerToken = replicator.addDocumentReplicationListener(new DocumentReplicationListener() {

            @Override
            public void replication(@NonNull DocumentReplication replication) {
                System.out.println("Replicated Document "+ "Outside");
                replication.getDocuments().listIterator().forEachRemaining(i -> {
                    System.out.println("Replicated Document "+ i.getID());
                });
            }
        });

        replicator.start();
    }

}
