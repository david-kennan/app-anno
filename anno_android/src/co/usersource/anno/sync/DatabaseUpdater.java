package co.usersource.anno.sync;

import co.usersource.annoplugin.datastore.TableCommentFeedbackAdapter;
import co.usersource.annoplugin.model.AnnoContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class DatabaseUpdater {
	
	private final String TAG = "DatabaseUpdater";
	private ContentResolver dbContext;
	
	public DatabaseUpdater(ContentResolver context) {
		dbContext = context;
	} 
	
	public void setRecordKey(String recordId, String key )
	{
		Uri updateUri = Uri.parse(AnnoContentProvider.COMMENT_PATH_URI.toString() + "/"	 + recordId);
		ContentValues values = new ContentValues();
		values.put(TableCommentFeedbackAdapter.COL_OBJECT_KEY, key);
		int count = dbContext.update(updateUri, values, null, null);
		
		if(count <= 0){
			Log.v(TAG, "Can't add key for record id = " + recordId);
		}
	}
	
	public Cursor getItemsAfterDate(String date)
	{
		String selection = null;
		if(null != date){
			selection = TableCommentFeedbackAdapter.COL_TIMESTAMP + " > '" + date + "'";
		}
		Cursor result = dbContext.query(AnnoContentProvider.COMMENT_PATH_URI, null, selection, null, null);
		return result;
	}
	
	public void createNewRecord(ContentValues record)
	{
		if(dbContext.insert(AnnoContentProvider.COMMENT_PATH_URI, record) == null){
			Log.v(TAG, "Can't insert a new item" + record.toString());
		}
	}

}
