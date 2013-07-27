package co.usersource.anno.sync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.usersource.anno.network.HttpConnector;
import co.usersource.anno.network.IHttpConnectorAuthHandler;
import co.usersource.anno.network.IHttpRequestHandler;
import co.usersource.annoplugin.datastore.FileImageManage;
import co.usersource.annoplugin.datastore.TableCommentFeedbackAdapter;
import co.usersource.annoplugin.utils.AppConfig;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;



/**
 * This class implements synchronization with server.
 * 
 * @author Sergey Gadzhilov
 * 
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
	public static final String JSON_REQUEST_PARAM_NAME = "jsonData";
	
	private static final String TAG = "AnnoSyncAdapter";
	
	private ContentValues m_valuesForUpdate;
	private HttpConnector httpConnector;
	private String lastUpdateDate;
	private RequestCreater request;
	private DatabaseUpdater db;

       
    /**
     * {@inheritDoc}
     */
    public SyncAdapter(Context context, boolean autoInitialize) 
    {
        super(context, autoInitialize);
        m_valuesForUpdate = new ContentValues();
        db = new DatabaseUpdater(context.getContentResolver());
    }

	/**
	 * @return the httpConnector
	 */
	public HttpConnector getHttpConnector() {
		if(httpConnector==null)
		{
			httpConnector = new HttpConnector();
		}
		return httpConnector;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		if (getHttpConnector().isAuthenticated()) {
			Log.d(TAG, "httpConnector.isAuthenticated()==true. Perform sync.");
			performSyncRoutines();
		} else {
			Log.d(TAG, "httpConnector.isAuthenticated()==false. Perform auth.");
			getHttpConnector()
					.setHttpConnectorAuthHandler(new IHttpConnectorAuthHandler() {

						public void onAuthSuccess() {
							performSyncRoutines();
						}

						public void onAuthFail() {
							Toast.makeText(getContext(),
									"Auth to sync service failed",
									Toast.LENGTH_LONG).show();
						}
					});
			getHttpConnector().authenticate(getContext(), account);
		}
	}
	
	

	private void performSyncRoutines() {
		Log.v(TAG, "Start synchronization (performSyncRoutines)");
	try {
			request = getLocalData();
			
			final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(
					SyncAdapter.JSON_REQUEST_PARAM_NAME, request.getKeysRequest().toString()));
			getHttpConnector().SendRequest("/sync", params, new IHttpRequestHandler() {
				
				public void onRequest(JSONObject response) {
					updateLocalKeys(response);
				}
			});
			
		} catch (IOException e) {
			Log.v(TAG, "I/O excecption!!!");
			e.printStackTrace();
		}
	}
	
	private void updateLocalKeys(JSONObject data)
	{
		Iterator<Map.Entry<String, String>> items = request.addKeys(data).entrySet().iterator();
		
		while(items.hasNext())
		{
			Map.Entry<String, String> item = items.next();
			db.setRecordKey((String)item.getKey(), (String)item.getValue());
		}
		
		final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(SyncAdapter.JSON_REQUEST_PARAM_NAME, request.getServerDataRequest().toString()));
		try {
			getHttpConnector().SendRequest("/sync", params, new IHttpRequestHandler() {
				public void onRequest(JSONObject response) {
						updateLocalDatabase(response);
					}
				});
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sendItems();
	}
	
	
	public void sendItems()
	{
		final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		JSONObject req = request.getNext();
		try {
			if(req != null)
			{
				params.add(new BasicNameValuePair(SyncAdapter.JSON_REQUEST_PARAM_NAME, req.toString()));
				getHttpConnector().SendRequest("/sync", params, new IHttpRequestHandler() {
					public void onRequest(JSONObject response) {
							sendItems();
						}
					});
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    
	/**
	 * This function reads information from local database.
	 * @return local data in json format 
	 */
    private RequestCreater getLocalData()
    {
    	Log.v(TAG, "Start getLocalData");
    	RequestCreater request = new RequestCreater(getContext());
    	request.addUpdateDate(lastUpdateDate);
    	Cursor localData = db.getItemsAfterDate(lastUpdateDate);
    	
    	if(null != localData)
    	{
    		for(boolean isDataExist = localData.moveToFirst(); isDataExist; isDataExist = localData.moveToNext())
    		{
    			request.addObject(localData);
    		}
    	}
    	
    	return request;
    }
    
    
	/**
	 * Update local database with data from server
	 * 
	 * @param data
	 *            - json object with data from server
	 */
	private void updateLocalDatabase(JSONObject data) {
		
		Log.v(TAG, data.toString());
		try {
			
			lastUpdateDate = data.getString(RequestCreater.JSON_TIME_STAMP);
			JSONArray updatedObjects = data.getJSONArray(RequestCreater.JSON_UPDATED_OBJECTS);
			FileImageManage imgManager = new FileImageManage(getContext(), AppConfig.getInstance(getContext()));
			byte[] decodeByte = null;
			
			for(int i = 0; i < updatedObjects.length(); ++i)
			{
				addFieldToUpdate(TableCommentFeedbackAdapter.COL_COMMENT, updatedObjects.getJSONObject(i), null);
				addFieldToUpdate(TableCommentFeedbackAdapter.COL_SCREENSHOT_KEY, updatedObjects.getJSONObject(i), null);
				addFieldToUpdate(TableCommentFeedbackAdapter.COL_POSITION_X, updatedObjects.getJSONObject(i), null);
				addFieldToUpdate(TableCommentFeedbackAdapter.COL_POSITION_Y, updatedObjects.getJSONObject(i), null);
				addFieldToUpdate(TableCommentFeedbackAdapter.COL_DIRECTION, updatedObjects.getJSONObject(i), null);
				decodeByte = Base64.decode(updatedObjects.getJSONObject(i).getString(RequestCreater.JSON_IMAGE), 0);
				imgManager.saveImageWithKey(BitmapFactory.decodeByteArray(decodeByte, 0, decodeByte.length), 
						                    updatedObjects.getJSONObject(i).getString(TableCommentFeedbackAdapter.COL_SCREENSHOT_KEY));
				db.createNewRecord(m_valuesForUpdate);
			}
			
			sendItems();
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Add field to set for update local database
	 * 
	 * @param fieldName
	 *            - name of field in db
	 * @param value
	 *            - JSON object with values
	 * @param valueName
	 *            - value name in json object. If null then fieldName will be
	 *            use as name in json object
	 */
	public void addFieldToUpdate(String fieldName, JSONObject value, String valueName) 
	{
		String data;
		try {
			if (valueName != null) {
				data = value.getString(valueName);
			} else {
				data = value.getString(fieldName);
			}
			m_valuesForUpdate.put(fieldName, data);
		} catch (JSONException e) {
			Log.v(TAG, "Cannot add value for field " + fieldName);
			e.printStackTrace();
		}

	}
}
