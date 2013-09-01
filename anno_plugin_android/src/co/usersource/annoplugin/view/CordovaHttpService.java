package co.usersource.annoplugin.view;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.usersource.annoplugin.sync.AnnoHttpService;
import co.usersource.annoplugin.sync.AnnoHttpServiceImpl;
import co.usersource.annoplugin.sync.ResponseHandler;

public class CordovaHttpService extends CordovaPlugin {

  public static final String GET_ANNO_LIST = "get_anno_list";
  public static final String GET_ANNO_DETAIL = "get_anno_detail";
  public static final String UPDATE_APP_NAME = "update_app_name";
  public static final String ADD_FOLLOW_UP = "add_follow_up";
  public static final String REMOVE_FOLLOW_UP = "remove_follow_up";
  public static final String ADD_FLAG = "add_flag";
  public static final String REMOVE_FLAG = "remove_flag";
  public static final String ADD_VOTE = "add_vote";
  public static final String REMOVE_VOTE = "remove_vote";
  public static final String COUNT_VOTE = "count_vote";
  public static final String COUNT_FLAG = "count_flag";

  private AnnoHttpService service;

  public CordovaHttpService() {
    // service = new AnnoHttpServiceImpl(this.cordova.getActivity());
  }

  @Override
  public boolean execute(String action, JSONArray args,
      CallbackContext callbackContext) throws JSONException {
    if (service == null) {
      service = new AnnoHttpServiceImpl(this.cordova.getActivity());
    }
    if (GET_ANNO_LIST.equals(action)) {
      getAnnoList(args, callbackContext);
      return true;
    } else if (GET_ANNO_DETAIL.equals(action)) {
      getAnnoDetail(args, callbackContext);
      return true;
    } else if (UPDATE_APP_NAME.equals(action)) {
      updateAppName(args, callbackContext);
      return true;
    } else if (ADD_FOLLOW_UP.equals(action)) {
      addFollowUp(args, callbackContext);
      return true;
    } else if (REMOVE_FOLLOW_UP.equals(action)) {
      removeFollowUp(args, callbackContext);
      return true;
    } else if (ADD_FLAG.equals(action)) {
      addFlag(args, callbackContext);
      return true;
    } else if (REMOVE_FLAG.equals(action)) {
      removeFlag(args, callbackContext);
      return true;
    } else if (ADD_VOTE.equals(action)) {
      addVote(args, callbackContext);
      return true;
    } else if (REMOVE_VOTE.equals(action)) {
      removeVote(args, callbackContext);
      return true;
    } else if (COUNT_VOTE.equals(action)) {
      countVote(args, callbackContext);
      return true;
    } else if (COUNT_FLAG.equals(action)) {
      countFlag(args, callbackContext);
      return true;
    }
    return false;
  }

  private void countFlag(JSONArray args, final CallbackContext callbackContext)
      throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    service.countFlag(annoId, new CordovaResponseHandler(callbackContext));
  }

  private void countVote(JSONArray args, final CallbackContext callbackContext)
      throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    service.countVote(annoId, new CordovaResponseHandler(callbackContext));
  }

  private void removeVote(JSONArray args, final CallbackContext callbackContext)
      throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    service.removeVote(annoId, new CordovaResponseHandler(callbackContext));
  }

  private void addVote(JSONArray args, final CallbackContext callbackContext)
      throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    service.addVote(annoId, new CordovaResponseHandler(callbackContext));
  }

  private void removeFlag(JSONArray args, final CallbackContext callbackContext)
      throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    service.removeFlag(annoId, new CordovaResponseHandler(callbackContext));
  }

  private void addFlag(JSONArray args, final CallbackContext callbackContext)
      throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    service.addFlag(annoId, new CordovaResponseHandler(callbackContext));
  }

  private void removeFollowUp(JSONArray args,
      final CallbackContext callbackContext) {
    // TODO:
  }

  private void addFollowUp(JSONArray args, final CallbackContext callbackContext)
      throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    String comment = jobj.getString("comment");
    service.addFollowup(annoId, comment, new CordovaResponseHandler(
        callbackContext));
  }

  private void updateAppName(JSONArray args,
      final CallbackContext callbackContext) throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    String appName = jobj.getString("app_name");
    service.updateAppName(annoId, appName, new CordovaResponseHandler(
        callbackContext));
  }

  private void getAnnoDetail(JSONArray args,
      final CallbackContext callbackContext) throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    service.getAnnoDetail(annoId, new CordovaResponseHandler(callbackContext));
  }

  /**
   * 
   * @param args
   *          args is an long array, first element is offset, second element is
   *          limit.
   * @throws JSONException
   */
  private void getAnnoList(JSONArray args, final CallbackContext callbackContext)
      throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    long offset = jobj.getLong("offset");
    long limit = jobj.getLong("limit");
    service.getAnnoList(offset, limit, new CordovaResponseHandler(
        callbackContext));
  }

  private class CordovaResponseHandler implements ResponseHandler {

    private CallbackContext callbackContext;

    CordovaResponseHandler(CallbackContext callbackContext) {
      this.callbackContext = callbackContext;
    }

    @Override
    public void handleResponse(final JSONObject response) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          callbackContext.success(response);
        }
      });
    }

    @Override
    public void handleError(final Exception e) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          callbackContext.error(e.getMessage());
        }
      });
    }

  };
}
