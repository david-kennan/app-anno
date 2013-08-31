package co.usersource.annoplugin;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import co.usersource.annoplugin.sync.AnnoHttpService;
import co.usersource.annoplugin.sync.AnnoHttpServiceImpl;
import co.usersource.annoplugin.sync.ResponseHandler;

public class Test {

  public void test(Context context) {
    AnnoHttpService service = new AnnoHttpServiceImpl(context);
    service.getAnnoList(0, 100, new ResponseHandler() {

      @Override
      public void handleResponse(JSONObject response) {
        try {
          Log.d("Test", response.toString(2));
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void handleError(Exception e) {
        Log.e("Test", e.getMessage(), e);
      }

    });
  }

}
