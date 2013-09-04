package co.usersource.annoplugin.view;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import co.usersource.annoplugin.AnnoPlugin;
import org.apache.cordova.DroidGap;
import android.gesture.GestureOverlayView;

import android.os.Bundle;


public class CommunityActivity extends DroidGap
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.init();

        GestureOverlayView view = new GestureOverlayView(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

        setContentView(view);
        view.addView((View) appView.getParent()); //adds the PhoneGap browser
        view.getChildAt(0).setLayoutParams(new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, 1));

        setContentView(view);
        AnnoPlugin.setEnableGesture(this, view, true);
        super.loadUrl("file:///android_asset/www/pages/community/main.html");
    }

}
