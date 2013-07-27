/**
 * 
 */
package co.usersource.annoplugin.datastore;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Table adapter for comment manipulation.
 * 
 * @author topcircler
 * 
 */
public class TableCommentFeedbackAdapter extends AbstractTableAdapter {
  // TODO: when to close database connection.

  /* table column names. */
  public static final String COL_ID = "_id";
  public static final String COL_COMMENT = "comment";
  public static final String COL_SCREENSHOT_KEY = "screenshot_key";
  public static final String COL_POSITION_X = "x";
  public static final String COL_POSITION_Y = "y";
  public static final String COL_DIRECTION = "direction";
  public static final String COL_APP_VERSION = "app_version";
  public static final String COL_OS_VERSION = "os_version";
  public static final String COL_MOVED = "isMoved"; // 0 - not move, 1 - moved.
  public static final String COL_LEVEL = "level"; // 1 or 2.

  public static final String TABLE_NAME = "feedback_comment";

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
            "create table %s (%s integer primary key autoincrement, %s text not null, %s text not null, %s integer not null, %s integer not null, %s integer not null, %s text, %s text, %s integer not null, %s integer not null);",
            TABLE_NAME, COL_ID, COL_COMMENT, COL_SCREENSHOT_KEY,
            COL_POSITION_X, COL_POSITION_Y, COL_DIRECTION, COL_APP_VERSION,
            COL_OS_VERSION, COL_MOVED, COL_LEVEL);

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
