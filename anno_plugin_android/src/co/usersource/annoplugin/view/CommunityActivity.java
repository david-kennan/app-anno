package co.usersource.annoplugin.view;

import org.apache.cordova.DroidGap;

import android.os.Bundle;

public class CommunityActivity extends DroidGap {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    super.loadUrl("file:///android_asset/www/pages/community/main.html");
  }

}
