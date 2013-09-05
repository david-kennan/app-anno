package co.usersource.annoplugin.view;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import co.usersource.annoplugin.AnnoPlugin;
import org.apache.cordova.DroidGap;
import android.gesture.GestureOverlayView;

import android.os.Bundle;


public class IntroActivity extends DroidGap
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.init();

        super.loadUrl("file:///android_asset/www/pages/intro/main.html");
    }

}
