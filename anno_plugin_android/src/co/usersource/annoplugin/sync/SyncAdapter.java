package co.usersource.annoplugin.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import co.usersource.annoplugin.model.AnnoContentProvider;
import co.usersource.annoplugin.network.HttpConnector;
import co.usersource.annoplugin.network.IHttpConnectorAuthHandler;
import co.usersource.annoplugin.network.IHttpRequestHandler;
import co.usersource.annoplugin.utils.AccountUtils;
import co.usersource.annoplugin.utils.SystemUtils;

/**
 * This class implements synchronization with server.
 * 
 * @author Sergey Gadzhilov
 * @author topcircler
 * 
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
  public static final String JSON_REQUEST_PARAM_NAME = "jsonData";

  private static final String TAG = "AnnoSyncAdapter";

  private HttpConnector httpConnector;
  private RequestCreater request;
  private DatabaseUpdater db;

  /**
   * {@inheritDoc}
   */
  public SyncAdapter(Context context, boolean autoInitialize) {
    super(context, autoInitialize);
    db = new DatabaseUpdater(context);
  }

  public static void requestSync(Context context) {
    // if there is no network connectivity, no need to synchronize.
    if (!SystemUtils.isOnline(context)) {
      Log.i(TAG, "Device is offline, won't synchronize.");
      return;
    }

    Account[] accounts = AccountUtils.getAllAccounts(context, null);
    if (accounts == null || accounts.length == 0) {
      // TODO: consider asking user to add an account?
      Log.i(TAG, "No available google account, won't synchronize.");
      return;
    }

    Account accountUse = accounts[0];
    Log.i(TAG, "GAE Sync using account - " + accountUse);
    Bundle extras = new Bundle();
    extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
    ContentResolver.requestSync(accountUse, AnnoContentProvider.AUTHORITY,
        extras);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onPerformSync(Account account, Bundle extras, String authority,
      ContentProviderClient provider, SyncResult syncResult) {
    HttpConnector httpConnector = getHttpConnector();
    if (httpConnector.isAuthenticated()) {
      Log.d(TAG, "httpConnector.isAuthenticated()==true. Perform sync.");
      performSyncRoutines();
    } else {
      Log.d(TAG, "httpConnector.isAuthenticated()==false. Perform auth.");
      httpConnector
          .setHttpConnectorAuthHandler(new IHttpConnectorAuthHandler() {

            public void onAuthSuccess() {
              Log.i(TAG, "Synchronize - Authentication succeeded.");
              performSyncRoutines();
            }

            public void onAuthFail() {
              Log.i(TAG, "Synchronize - Authentication failed.");
              Toast.makeText(getContext(), "Auth to sync service failed",
                  Toast.LENGTH_LONG).show();
            }
          });
      httpConnector.authenticate(getContext(), account);
    }
  }

  private void performSyncRoutines() {
    Log.i(TAG, "Start synchronization");
    try {
      request = getLocalData();

      final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
      Log.i(TAG, "Send generateKeys request.");
      params.add(new BasicNameValuePair(SyncAdapter.JSON_REQUEST_PARAM_NAME,
          request.getKeysRequest().toString()));
      getHttpConnector().SendRequest("/sync", params,
          new IHttpRequestHandler() {

            public void onRequest(JSONObject response) {
              updateLocalKeys(response);
            }
          });

    } catch (IOException e) {
      Log.e(TAG, e.getMessage(), e);
    }
  }

  private void updateLocalKeys(JSONObject data) {
    Log.i(TAG, "Update local data with server keys.");
    Iterator<Map.Entry<String, String>> items = request.addKeys(data)
        .entrySet().iterator();
    while (items.hasNext()) {
      Map.Entry<String, String> item = items.next();
      String key = (String) item.getKey();
      String value = (String) item.getValue();
      Log.d(TAG, "Update local data with key:(" + key + "," + value + ")");
      db.setRecordKey(key, value);
    }
    sendItems();
  }

  public void sendItems() {
    final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
    JSONObject req = request.getNext();
    try {
      if (req != null) {
        String reqString = req.toString();
        Log.i(TAG, "Send comment.");
        Log.d(TAG, "request data: " + reqString);
        params.add(new BasicNameValuePair(SyncAdapter.JSON_REQUEST_PARAM_NAME,
            req.toString()));
        getHttpConnector().SendRequest("/sync", params,
            new IHttpRequestHandler() {
              public void onRequest(JSONObject response) {
                if (response != null) {
                  Log.d(TAG, "Send Comment response: " + response.toString());
                  db.updateLastSyncTime(System.currentTimeMillis());
                }
                sendItems();
              }
            });
      }
    } catch (IOException e) {
      Log.e(TAG, e.getMessage(), e);
    }
  }

  /**
   * This function reads information from local database.
   * 
   * @return local data in json format
   */
  private RequestCreater getLocalData() {
    Log.v(TAG, "Populate local data that not synched into request.");
    RequestCreater request = new RequestCreater(getContext());
    Long lastUpdateDate = db.getLastSyncTime();
    Cursor localData = db.getItemsAfterDate(lastUpdateDate);

    if (null != localData) {
      for (boolean isDataExist = localData.moveToFirst(); isDataExist; isDataExist = localData
          .moveToNext()) {
        request.addObject(localData);
      }
    }
    return request;
  }

  /**
   * @return the httpConnector
   */
  public HttpConnector getHttpConnector() {
    if (httpConnector == null) {
      httpConnector = new HttpConnector();
    }
    return httpConnector;
  }
}
