/**
 * 
 */
package io.usersource.annoplugin.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;

/**
 * Image Utilities.
 * 
 * @author topcircler
 * 
 */
public final class ImageUtils {

  /**
   * Get bitmap from image view.
   * 
   * @param imageView
   *          imageView.
   * @return bitmap.
   */
  public static Bitmap getBitmapFromImageView(ViewGroup imageView) {
    Bitmap bitmap = ((BitmapDrawable) imageView.getBackground()).getBitmap();
    return bitmap;
  }

  /**
   * Compress the given bitmap and return a compressed one.
   * 
   * @param bitmap
   *          original bitmap.
   * @return compressed bitmap.
   */
  public static Bitmap compressBitmap(Bitmap bitmap) {
    // TODO: implement compress bitmap.
    return bitmap;
  }

}
