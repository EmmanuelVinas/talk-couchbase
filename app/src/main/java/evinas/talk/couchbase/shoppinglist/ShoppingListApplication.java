package evinas.talk.couchbase.shoppinglist;

import android.util.Log;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.replicator.Replication;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ShoppingListApplication extends android.app.Application {

    public final static String TAG = "DemoCouchbase";

    private final static String DATABASE_NAME = "shoppinglist";
    private final static String SYNC_URL = "http://127.0.0.1:4984/shoppingList";

    // Couchbase
    private Manager manager;
    private Database database;
    public Replication pullReplication;
    public Replication pushReplication;

    @Override
    public void onCreate() {
        super.onCreate();

        initDatabase();

        initReplications();
    }

    private void initDatabase() {
        try {

            Manager.enableLogging(TAG, Log.VERBOSE);

            manager = new Manager(new AndroidContext(getApplicationContext()), Manager.DEFAULT_OPTIONS);
        } catch (IOException e) {
            Log.e(TAG, "Cannot create Manager object", e);
            return;
        }

        try {
            database = manager.getDatabase(DATABASE_NAME);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot get Database", e);
            return;
        }
    }

    private void initReplications(){
        URL syncUrl;
        try {
            syncUrl = new URL(SYNC_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        pullReplication = database.createPullReplication(syncUrl);
        pullReplication.setContinuous(true);
        pullReplication.addChangeListener(getReplicationChangeListener("pull"));

        pushReplication = database.createPushReplication(syncUrl);
        pushReplication.setContinuous(true);
        pushReplication.addChangeListener(getReplicationChangeListener("pull"));

    }

    private Replication.ChangeListener getReplicationChangeListener(final String type) {
        return new Replication.ChangeListener() {

            @Override
            public void changed(Replication.ChangeEvent event) {
                Replication replication = event.getSource();
                if (event.getError() != null) {
                    Toast.makeText(ShoppingListApplication.this, "Replication Type:"+type + ", "+replication.getCompletedChangesCount()+"/"+replication.getChangesCount() +", status:"+ replication.getStatus(),
                            Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    public Database getDatabase(){
        return database;
    }

}
