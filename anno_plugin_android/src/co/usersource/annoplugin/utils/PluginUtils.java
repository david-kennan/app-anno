/**
 * 
 */
package co.usersource.annoplugin.utils;

/**
 * @author topcircler
 * 
 */
public class PluginUtils {

  public static final String LEVEL = "level";

  /**
   * This constant should be kept consistently with package name in Anno
   * standalone app.
   */
  private static final String ANNO_PACKAGE_NAME = "co.usersource.anno";

  public static boolean isAnno(String packageName) {
    return ANNO_PACKAGE_NAME.equals(packageName);
  }

}
