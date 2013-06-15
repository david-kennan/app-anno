/**
 * 
 */
package co.usersource.annoplugin.gesture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import co.usersource.annoplugin.R;
import co.usersource.annoplugin.utils.ScreenshotUtils;
import co.usersource.annoplugin.utils.ViewUtils;

/**
 * Screenshot gesture listener, detect and process spiral gesture.
 * 
 * @author topcircler
 * 
 */
public class ScreenshotGestureListener implements OnGesturePerformedListener {

  private static final String TAG = "ScreenshotGestureListener";

  private static final String FEEDBACK_ACTIVITY = "co.usersource.annoplugin.view.FeedbackEditActivity";
  private static final String GESTURE_NAME = "UserSource spiral";
  private static final String SCREENSHOTS_DIR_NAME = "Screenshots";

  private Activity activity;
  private GestureLibrary gestureLibrary = null;

  public ScreenshotGestureListener(Activity activity, int rawResourceId) {
    this.activity = activity;
    gestureLibrary = GestureLibraries.fromRawResource(activity, rawResourceId);
    gestureLibrary.load();
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.gesture.GestureOverlayView.OnGesturePerformedListener#
   * onGesturePerformed(android.gesture.GestureOverlayView,
   * android.gesture.Gesture)
   */
  @Override
  public void onGesturePerformed(GestureOverlayView arg0, Gesture gesture) {
    ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);
    ArrayList<String> predictionNames = new ArrayList<String>();
    if (predictions != null) {
      for (Prediction prediction : predictions) {
        predictionNames.add(prediction.name);
      }
      if (predictionNames.contains(GESTURE_NAME)) {
        String screenshotPath;
        try {
          screenshotPath = takeScreenshot();
          launchAnnoPlugin(screenshotPath);
        } catch (FileNotFoundException e) {
          Log.e(TAG, e.getMessage(), e);
          ViewUtils.displayError(activity, R.string.fail_take_screenshot);
        }
      }
    }
  }

  private void launchAnnoPlugin(String screenshotPath) {
    String packageName = activity.getPackageName();
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setClassName(packageName, FEEDBACK_ACTIVITY);
    intent.setType("image/*");
    File imageFile = new File(screenshotPath);
    Uri imageUri = Uri.parse("file://" + imageFile.getPath());
    intent.putExtra(Intent.EXTRA_STREAM, imageUri);
    activity.startActivity(intent);
  }

  private String takeScreenshot() throws FileNotFoundException {
    Bitmap b = ScreenshotUtils.takeScreenshot(activity);
    FileOutputStream fos = null;
    try {
      File screenshotDir = new File(
          Environment
              .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
          SCREENSHOTS_DIR_NAME);
      File screenshotPath = new File(screenshotDir,
          ScreenshotUtils.generateScreenshotName());
      fos = new FileOutputStream(screenshotPath);
      b.compress(Bitmap.CompressFormat.PNG, 100, fos);
      return screenshotPath.getAbsolutePath();
    } catch (FileNotFoundException e) {
      throw e;
    } finally {
      if (fos != null) {
        try {
          fos.flush();
          fos.close();
        } catch (IOException e) {
        }
      }
    }
  }
}
