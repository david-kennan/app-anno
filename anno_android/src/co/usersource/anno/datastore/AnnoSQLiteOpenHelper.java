/**
 * 
 */
package co.usersource.anno.datastore;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This class intends to provide access and creation routines for Anno program
 * database.
 * 
 * @author topcircler
 * 
 */
public class AnnoSQLiteOpenHelper extends SQLiteOpenHelper {

  private static final String TAG = "AnnoSQLiteOpenHelper";

  public static final String DATABASE_NAME = "anno.db";
  /** Version for upgrade routines. */
  public static final int DATABASE_VERSION = 3;

  /* table adapters */
  private TableAdapter tableCommentFeedbackAdapter;

  public AnnoSQLiteOpenHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
    tableCommentFeedbackAdapter = new TableCommentFeedbackAdapter(this);
  }

  @Override
  public void onCreate(SQLiteDatabase database) {
    tableCommentFeedbackAdapter.onCreate(database);
  }

  @Override
  public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
    // initial version, not need to upgrade.
    final String addColumnSql = "alter table %s add column %s integer;";
    final String updateTwoValue = "update %s set %s = %s, %s = %s;";
    final String updateOneValue = "update %s set %s = %s;";
    if (oldVersion == 1 && newVersion == 2) {
      String sql = String.format(addColumnSql,
          TableCommentFeedbackAdapter.TABLE_NAME,
          TableCommentFeedbackAdapter.COL_POSITION_X);
      database.execSQL(sql);
      Log.d(TAG, "upgrade db:" + sql);
      sql = String.format(addColumnSql, TableCommentFeedbackAdapter.TABLE_NAME,
          TableCommentFeedbackAdapter.COL_POSITION_Y);
      database.execSQL(sql);
      Log.d(TAG, "upgrade db:" + sql);
      sql = String.format(updateOneValue,
          TableCommentFeedbackAdapter.TABLE_NAME,
          TableCommentFeedbackAdapter.COL_POSITION_X, 50,
          TableCommentFeedbackAdapter.COL_POSITION_Y, 100);
      database.execSQL(sql);
      Log.d(TAG, "upgrade db:" + sql);
    } else if (oldVersion == 2 && newVersion == 3) {
      String sql = String.format(addColumnSql,
          TableCommentFeedbackAdapter.TABLE_NAME,
          TableCommentFeedbackAdapter.COL_DIRECTION);
      database.execSQL(sql);
      Log.d(TAG, "upgrade db:" + sql);
      sql = String.format(updateTwoValue,
          TableCommentFeedbackAdapter.TABLE_NAME,
          TableCommentFeedbackAdapter.COL_DIRECTION, 0);
      database.execSQL(sql);
      Log.d(TAG, "upgrade db:" + sql);
    }
  }

  /**
   * @return the tableCommentFeedbackAdapter
   */
  public TableAdapter getTableCommentFeedbackAdapter() {
    return tableCommentFeedbackAdapter;
  }

  /**
   * Close database connection.
   */
  public void close() {
    if (getWritableDatabase().isOpen()) {
      getWritableDatabase().close();
    }
  }

}
