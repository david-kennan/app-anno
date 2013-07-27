/**
 * 
 */
package co.usersource.annoplugin.datastore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import co.usersource.annoplugin.R;
import co.usersource.annoplugin.utils.AppConfig;
import co.usersource.annoplugin.utils.SystemUtils;

/**
 * Implement ImageManage interface, it saves and loads images from SD card data
 * directory.
 * 
 * @author topcircler
 * 
 */
public class FileImageManage implements ImageManage {

  private static final int COMPRESS_QUALITY = 90;

  private Context context;
  private AppConfig config;

  public FileImageManage(Context context, AppConfig config) {
    this.context = context;
    this.config = config;
  }

  @Override
  public String saveImage(Bitmap bitmap) throws IOException {
    String appLocation = config.getDataLocation();
    String screenshotDirName = config.getScreenshotDirName();
    String screenshotDirPath = new File(appLocation, screenshotDirName)
        .getAbsolutePath();
    SystemUtils.mkdirs(context, screenshotDirPath);
    checkEnoughSpace(bitmap.getByteCount());

    String imageKey = generateUniqueImageKey();
    FileOutputStream out = new FileOutputStream(new File(screenshotDirPath,
        imageKey));
    bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY, out);
    return imageKey;
  }

  /**
   * Save the image to the disk with specified key
   * @param bitmap image for saving
   * @param key for saving bitmap
   * @throws IOException
   */
  public void saveImageWithKey(Bitmap bitmap, String key) throws IOException {
    String appLocation = config.getDataLocation();
    String screenshotDirName = config.getScreenshotDirName();
    String screenshotDirPath = new File(appLocation, screenshotDirName).getAbsolutePath();
    SystemUtils.mkdirs(context, screenshotDirPath);
    checkEnoughSpace(bitmap.getByteCount());

    FileOutputStream out = new FileOutputStream(new File(screenshotDirPath,  key));
    bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY, out);
  }

  
  /**
   * Check if there is enough space on SD card.
   * 
   * @param size
   * @throws IOException
   * @throws NotFoundException
   */
  private void checkEnoughSpace(int size) throws IOException {
    if (SystemUtils.getFreeSDCardSpace() < size) {
      throw new IOException(context.getResources().getString(
          R.string.fail_enough_sdcard_space));
    }
  }

  /**
   * Generate a unique key for each image.
   * 
   * Temporarily use UUID to generate a random unique one.
   * 
   * @return image key.
   */
  private String generateUniqueImageKey() {
    return UUID.randomUUID().toString();
  }

  @Override
  public Bitmap loadImage(String key) {
    String appLocation = config.getDataLocation();
    String screenshotDirName = config.getScreenshotDirName();
    String screenshotDirPath = new File(appLocation, screenshotDirName)
        .getAbsolutePath();
    File imageFile = new File(screenshotDirPath, key);
    return BitmapFactory.decodeFile(imageFile.getAbsolutePath());
  }

}
