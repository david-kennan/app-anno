package co.usersource.anno.view;

import java.lang.ref.WeakReference;

import android.app.ActionBar;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import co.usersource.anno.R;
import co.usersource.annoplugin.datastore.FileImageManage;
import co.usersource.annoplugin.datastore.ImageManage;
import co.usersource.annoplugin.datastore.TableCommentFeedbackAdapter;
import co.usersource.annoplugin.model.AnnoContentProvider;
import co.usersource.annoplugin.utils.AppConfig;
import co.usersource.annoplugin.view.custom.CommentAreaLayout;

/**
 * View comment feedback.
 * 
 * You can view comment for the chosen comment.
 * 
 * @author topcircler
 * 
 */
public class FeedbackViewActivity extends Activity {

  private static final String TAG = "FeedbackViewActivity";

  private ImageManage imageManage;
  private AsyncHandler handler;

  // components.
  private RelativeLayout viewImvScreenshot;
  private CommentAreaLayout viewCommentArea;
  private EditText tvComment;
  private ActionBar actionBar;
  private Button btnComment;

  /**
   * token id represents retrieving comment in an async process.
   */
  private static final int TOKEN_GET_COMMENT = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.feedback_view_activity);

    AppConfig config = AppConfig.getInstance(this);
    imageManage = new FileImageManage(this, config);
    handler = new AsyncHandler(getContentResolver(), this);

    setComponents();
    handleIntent();
  }

  private void handleIntent() {
    Intent intent = getIntent();
    String[] projection = { TableCommentFeedbackAdapter.COL_ID,
        TableCommentFeedbackAdapter.COL_COMMENT,
        TableCommentFeedbackAdapter.COL_SCREENSHOT_KEY,
        TableCommentFeedbackAdapter.COL_POSITION_X,
        TableCommentFeedbackAdapter.COL_POSITION_Y,
        TableCommentFeedbackAdapter.COL_DIRECTION };
    Uri uri = intent.getParcelableExtra(AnnoContentProvider.COMMENT_PATH);
    handler.startQuery(TOKEN_GET_COMMENT, null, uri, projection, null, null,
        null);
  }

  private void setComponents() {
    viewImvScreenshot = (RelativeLayout) findViewById(R.id.viewImvScreenshot);
    tvComment = (EditText) findViewById(R.id.etComment);
    tvComment.setEnabled(false);
    viewCommentArea = (CommentAreaLayout) findViewById(R.id.viewCommentArea);
    viewCommentArea.setChangable(false);
    btnComment = (Button) findViewById(R.id.btnComment);
    btnComment.setVisibility(View.INVISIBLE);
    actionBar = getActionBar();
    actionBar.hide();
  }

  private static class AsyncHandler extends AsyncQueryHandler {

    private WeakReference<FeedbackViewActivity> activityRef;

    public AsyncHandler(ContentResolver cr, FeedbackViewActivity activity) {
      super(cr);
      activityRef = new WeakReference<FeedbackViewActivity>(activity);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
      super.onQueryComplete(token, cookie, cursor);

      FeedbackViewActivity activity = activityRef.get();
      if (token == TOKEN_GET_COMMENT) {
        if (cursor != null && cursor.moveToFirst()) {

          int idx = cursor
              .getColumnIndex(TableCommentFeedbackAdapter.COL_COMMENT);
          if (idx != -1) {
            String comment = cursor.getString(idx);
            activity.tvComment.setText(comment);
          }
          idx = cursor
              .getColumnIndex(TableCommentFeedbackAdapter.COL_SCREENSHOT_KEY);
          if (idx != -1) {
            String imageKey = cursor.getString(idx);
            BitmapDrawable drawable = new BitmapDrawable(
                activity.imageManage.loadImage(imageKey));
            activity.viewImvScreenshot.setBackgroundDrawable(drawable);
          }
          final int x, y, direction;
          int xIdx = cursor
              .getColumnIndex(TableCommentFeedbackAdapter.COL_POSITION_X);
          int yIdx = cursor
              .getColumnIndex(TableCommentFeedbackAdapter.COL_POSITION_Y);
          int directionIdx = cursor
              .getColumnIndex(TableCommentFeedbackAdapter.COL_DIRECTION);
          if (xIdx != -1 && yIdx != -1 && directionIdx != -1) {
            x = cursor.getInt(xIdx);
            y = cursor.getInt(yIdx);
            direction = cursor.getInt(directionIdx);
            final FeedbackViewActivity finalActivity = activity;
            activity.viewCommentArea.post(new Runnable() {

              @Override
              public void run() {
                finalActivity.viewCommentArea.locate(x, y, direction);
              }

            });
          }
        }
      }
      activity = null;
      if (!cursor.isClosed()) {
        cursor.close();
      }
    }
  };
}
