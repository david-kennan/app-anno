package co.usersource.annoplugin.view;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import co.usersource.annoplugin.AnnoPlugin;
import co.usersource.annoplugin.R;
import co.usersource.annoplugin.datastore.TableCommentFeedbackAdapter;
import co.usersource.annoplugin.model.AnnoContentProvider;

/**
 * Home screen. Displays a list of all comments, by clicking any comment,
 * comment detail can be viewed.
 * 
 * @author topcircler
 * 
 */
public class AnnoMainActivity extends FragmentActivity implements
    LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {
  /*
   * consider using section list.
   * 
   * TODO: android-section-list: http://code.google.com/p/android-section-list/
   */

  private static final String TAG = AnnoMainActivity.class.getSimpleName();

  /**
   * which fields to display in the list.
   */
  private static final String[] PROJECTION = {
      TableCommentFeedbackAdapter.COL_ID,
      TableCommentFeedbackAdapter.COL_COMMENT };
  /**
   * id to represent a loader process.
   */
  private static final int URL_LOADER_COMMENTS = 0;

  // view components.
  private SimpleCursorAdapter adapter;
  private ListView feedbackListView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.anno_home);

    getActionBar().setTitle(R.string.plugin_name);
    getActionBar().setLogo(R.drawable.anno_ic_launcher);

    setComponent();

    AnnoPlugin.setEnableGesture(this, R.id.gestures, true);
  }

  private void setComponent() {
    feedbackListView = (ListView) this.findViewById(R.id.feedbackList);
    String[] bindFrom = new String[] { TableCommentFeedbackAdapter.COL_COMMENT };
    int[] bindTo = new int[] { R.id.commentLabel };
    adapter = new SimpleCursorAdapter(getApplicationContext(),
        R.layout.comment_row, null, bindFrom, bindTo, 0);
    feedbackListView.setAdapter(adapter);
    feedbackListView.setOnItemClickListener(this);
  }

  /**
   * Loads all comments and display in this list once loads completes.
   */
  private void loadComments() {
    Log.d(TAG, "start query comments.");
    getSupportLoaderManager().restartLoader(URL_LOADER_COMMENTS, null, this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.anno_main, menu);
    return true;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // no need to close cursor here, since it's already done in onLoaderReset().
  }

  @Override
  protected void onResume() {
    super.onResume();
    loadComments();
  }

  @Override
  public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
    switch (loaderId) {
    case URL_LOADER_COMMENTS:
      return new CursorLoader(this, AnnoContentProvider.COMMENT_PATH_URI,
          PROJECTION, null, null, null);
    default:
      // An invalid id was passed in.
      return null;
    }
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    Log.d(TAG, "query finishes.");
    if (adapter != null && cursor != null) {
      adapter.swapCursor(cursor);
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    Log.d(TAG, "query reset.");
    if (adapter != null) {
      adapter.swapCursor(null);
    }
  }

  @Override
  public void onItemClick(AdapterView<?> adapterView, View v, int position,
      long id) {
    Log.d(TAG, String.format("item %s(id:%d) was clicked.", position, id));
    Intent intent = new Intent(this, FeedbackViewActivity.class);
    intent.putExtra(AnnoContentProvider.COMMENT_PATH,
        ContentUris.withAppendedId(AnnoContentProvider.COMMENT_PATH_URI, id));
    startActivity(intent);
  }
}
