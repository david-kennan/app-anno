/**
 * 
 */
package co.usersource.anno.datastore;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Leo
 * 
 */
public class TableCommentFeedbackAdapter extends AbstractTableAdapter {

  /* table column names. */
  public static final String COL_ID = "_id";
  public static final String COL_COMMENT = "comment";
  public static final String COL_SCREENSHOT_KEY = "screenshot_key";

  private static final String TABLE_NAME = "feedback_comment";

  public TableCommentFeedbackAdapter(SQLiteOpenHelper sqliteOpenHelper) {
    super(sqliteOpenHelper);
  }

  @Override
  public String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public List<String> getInitSqls() {
    String createTableSql = String
        .format(
            "create table %s (%s integer primary key autoincrement, %s text not null, %s text not null);",
            TABLE_NAME, COL_ID, COL_COMMENT, COL_SCREENSHOT_KEY);

    List<String> initSqls = new ArrayList<String>();
    initSqls.add(createTableSql);
    return initSqls;
  }

  @Override
  public long insert(ContentValues values) {
    SQLiteDatabase database = sqliteOpenHelper.getWritableDatabase();
    return database.insert(getTableName(), null, values);
  }

  @Override
  public int delete(String arg1, String[] args2) {
    throw new UnsupportedOperationException(
        "TableCommentFeedbackAdapter.delete Not Implemented.");
  }

  @Override
  public int update(ContentValues values, String selection,
      String[] selectionArgs) {

    throw new UnsupportedOperationException(
        "TableCommentFeedbackAdapter.update Not Implemented.");
  }

  @Override
  public Cursor query(String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {
    Cursor cursor = null;
    SQLiteDatabase database = this.sqliteOpenHelper.getReadableDatabase();
    cursor = database.query(getTableName(), projection, selection,
        selectionArgs, null, null, sortOrder);
    return cursor;
  }

}