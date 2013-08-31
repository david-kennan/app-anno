package co.usersource.annoplugin.view;

import android.telephony.TelephonyManager;
import android.webkit.WebView;
import org.apache.cordova.DroidGap;
import android.webkit.JavascriptInterface;

public class TestClass {
    private WebView mAppView;
    private DroidGap mGap;

    public TestClass(DroidGap gap, WebView view)
    {
        mAppView = view;
        mGap = gap;
    }

    @JavascriptInterface
    public String getTelephoneNumber(String a){
        return a+" dss.";
    }
}
