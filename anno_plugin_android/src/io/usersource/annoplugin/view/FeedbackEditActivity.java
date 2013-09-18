package io.usersource.annoplugin.view;

import io.usersource.annoplugin.AnnoPlugin;
import io.usersource.annoplugin.R;
import io.usersource.annoplugin.datastore.FileImageManage;
import io.usersource.annoplugin.datastore.ImageManage;
import io.usersource.annoplugin.datastore.TableCommentFeedbackAdapter;
import io.usersource.annoplugin.model.AnnoContentProvider;
import io.usersource.annoplugin.sync.AnnoSyncAdapter;
import io.usersource.annoplugin.utils.AppConfig;
import io.usersource.annoplugin.utils.ImageUtils;
import io.usersource.annoplugin.utils.PluginUtils;
import io.usersource.annoplugin.utils.SystemUtils;
import io.usersource.annoplugin.utils.ViewUtils;
import io.usersource.annoplugin.view.custom.CircleArrow;
import io.usersource.annoplugin.view.custom.CommentAreaLayout;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;

import android.app.ActionBar;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

/**
 * Edit feedback screen from share intent.
 * 
 * You can add comment for the chosen screenshot.
 * 
 * @author topcircler
 * 
 */
public class FeedbackEditActivity extends Activity {

  private static final String TAG = "FeedbackActivity";

  private ImageManage imageManage;
  private AsyncHandler handler;

  // view components.
  private CommentAreaLayout commentAreaLayout;
  private RelativeLayout imvScreenshot;
  private RelativeLayout outerBackground;
  private ActionBar actionBar;
  private EditText etComment;
  private Button btnComment;
  private Button btnGoHome;
  private CircleArrow circleArrow;

  /*
   * This is to control anno plugin recursive levels. For 3rd-party app, at most
   * 2-levels are allowed; For standalone alone, at most 1-level is allowed.
   */
  private int level = 0;

  /**
   * token id represents inserting a comment in an async process.
   */
  private static final int TOKEN_INSERT_COMMENT = 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.feedback_edit_activity);

    AppConfig config = AppConfig.getInstance(this);
    imageManage = new FileImageManage(this, config);
    handler = new AsyncHandler(getContentResolver(), this);

    setComponents();
    handleIntent();

    AnnoPlugin.setEnableGesture(this, R.id.gestures, true);
  }

  private void handleIntent() {
    Intent intent = getIntent();
    String action = intent.getAction();
    String type = intent.getType();

    if (Intent.ACTION_SEND.equals(action) && type != null) {
      if (type.startsWith("image/")) {
        handleFromShareImage(intent);
      }
    }
  }

  private void setComponents() {
    imvScreenshot = (RelativeLayout) findViewById(R.id.imvScreenshot);
    etComment = (EditText) findViewById(R.id.etComment);
    btnComment = (Button) findViewById(R.id.btnComment);
    btnGoHome = (Button) findViewById(R.id.btnGoHome);
    commentAreaLayout = (CommentAreaLayout) findViewById(R.id.commentArea);
    actionBar = getActionBar();
    outerBackground = (RelativeLayout) findViewById(R.id.outer_bg);
    circleArrow = (CircleArrow) findViewById(R.id.circleArrow);
    circleArrow.setActivity(this);

    btnComment.setOnClickListener(sendCommentClickListener);
    btnGoHome.setOnClickListener(goHomeClickListener);
    etComment.setOnFocusChangeListener(commentBoxFocusListener);

    onComment();

    if (PluginUtils.isAnno(getPackageName())) {
      btnGoHome.setVisibility(View.GONE);
    }
  }

  private View.OnFocusChangeListener commentBoxFocusListener = new OnFocusChangeListener() {

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
      if (hasFocus) {
        commentAreaLayout.hideHomeButton();
      }
    }

  };

  private View.OnClickListener goHomeClickListener = new View.OnClickListener() {

    @Override
    public void onClick(View v) {
      String packageName = FeedbackEditActivity.this.getPackageName();
      // finish current activity.
      FeedbackEditActivity.this.finish();

      // launch anno home activity.
      Intent intent = new Intent();
      intent.setClassName(packageName,
          "io.usersource.annoplugin.view.AnnoMainActivity");
      intent.putExtra(PluginUtils.LEVEL, level);
      startActivity(intent);
    }
  };

  private View.OnClickListener sendCommentClickListener = new View.OnClickListener() {

    @Override
    public void onClick(View v) {
      try {
        // comment
        String comment = etComment.getText().toString();
        if (comment == null || comment.trim().isEmpty()) {
          ViewUtils.displayError(FeedbackEditActivity.this,
              R.string.invalid_comment_empty);
          return;
        }
        // image
        Bitmap bitmap = ImageUtils.compressBitmap(ImageUtils
            .getBitmapFromImageView(imvScreenshot));
        String imageKey;
        imageKey = imageManage.saveImage(bitmap);
        // coordinate
        float y = commentAreaLayout.getY();
        float x = commentAreaLayout.getCircleX();
        // direction
        boolean circleOnTop = commentAreaLayout.circleOnTop();
        // is moved
        boolean isMoved = circleArrow.isMoved();
        // level
        int level = getLevel();

        storeCommentInLocalDB(comment, imageKey, y, x, circleOnTop, isMoved,
            level);
      } catch (IOException e) {
        Log.e(TAG, e.getMessage());
        ViewUtils.displayError(FeedbackEditActivity.this, e.getMessage());
      } catch (Exception e) {
        // catch other exceptions, such as SQLException.
        if (e != null)
          Log.e(TAG, e.getMessage());
        ViewUtils.displayError(FeedbackEditActivity.this,
            R.string.fail_send_comment);
      }
    }
  };

  /**
   * Handle pressing 'comment' button.
   * 
   */
  private void onComment() {
    if (actionBar != null)
      actionBar.hide();
    commentAreaLayout.setVisibility(View.VISIBLE);
  }

  private void handleFromShareImage(Intent intent) {
    this.level = intent.getIntExtra(PluginUtils.LEVEL, 0) + 1;
    Log.d(TAG, "current level:" + this.level);
    if (this.level == 2) {
      outerBackground.setBackgroundColor(getResources().getColor(R.color.red));
      btnComment.setBackgroundResource(R.drawable.send_comment_button_l2);
      btnGoHome.setBackgroundResource(R.drawable.send_comment_button_l2);
      circleArrow.setCircleBackgroundColor(getResources().getColor(
          R.color.transparent_red));
      circleArrow.setCircleBorderColor(getResources().getColor(R.color.red));
    }

    Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
    if (imageUri != null) {
      ContentResolver rc = this.getContentResolver();
      BitmapDrawable drawable;
      try {
        drawable = new BitmapDrawable(getResources(),
            rc.openInputStream(imageUri));
        imvScreenshot.setBackgroundDrawable(drawable);
      } catch (FileNotFoundException e) {
        Log.e(TAG, e.getMessage(), e);
      }
    }
  }

  /**
   * Async handler for query manipulation.
   * 
   * @author topcircler
   * 
   */
  private static class AsyncHandler extends AsyncQueryHandler {

    private WeakReference<Activity> activityRef;

    public AsyncHandler(ContentResolver cr, Activity activity) {
      super(cr);
      this.activityRef = new WeakReference<Activity>(activity);
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
      super.onInsertComplete(token, cookie, uri);

      if (token == TOKEN_INSERT_COMMENT) {
        Log.d(TAG,
            "insert comment successfully. inserted uri:" + uri.toString());
        ViewUtils.displayInfo(activityRef.get(), R.string.success_send_comment);
        activityRef.get().finish();

        // if insert comment successfully, send it to GAE server asynchronizely.
        AnnoSyncAdapter.requestSync(activityRef.get().getApplicationContext());
      }
    }

  }

  public int getLevel() {
    return level;
  }

  private void storeCommentInLocalDB(String comment, String imageKey, float y,
      float x, boolean circleOnTop, boolean isMoved, int level)
      throws NameNotFoundException {
    ContentValues values = new ContentValues();
    values.put(TableCommentFeedbackAdapter.COL_COMMENT, comment);
    values.put(TableCommentFeedbackAdapter.COL_SCREENSHOT_KEY, imageKey);
    values.put(TableCommentFeedbackAdapter.COL_POSITION_X, x);
    values.put(TableCommentFeedbackAdapter.COL_POSITION_Y, y);
    values.put(TableCommentFeedbackAdapter.COL_DIRECTION, circleOnTop ? 0 : 1);
    values.put(TableCommentFeedbackAdapter.COL_MOVED, isMoved ? 1 : 0);
    values.put(TableCommentFeedbackAdapter.COL_LEVEL, level);
    values.put(TableCommentFeedbackAdapter.COL_APP_VERSION,
        SystemUtils.getAppVersion(FeedbackEditActivity.this));
    values.put(TableCommentFeedbackAdapter.COL_OS_VERSION,
        SystemUtils.getOSVersion());
    values.put(TableCommentFeedbackAdapter.COL_APP_NAME,
        SystemUtils.getAppName(FeedbackEditActivity.this));
    values.put(TableCommentFeedbackAdapter.COL_MODEL, SystemUtils.getModel());
    handler.startInsert(TOKEN_INSERT_COMMENT, null,
        AnnoContentProvider.COMMENT_PATH_URI, values);
  }

}