package co.usersource.annoplugin.sync;

import org.json.JSONObject;

public interface ResponseHandler {

  void handleResponse(JSONObject response);

  void handleError(Exception e);

}
