package co.usersource.annoplugin.view;

import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import co.usersource.annoplugin.sync.AnnoHttpService;
import co.usersource.annoplugin.sync.AnnoHttpServiceImpl;
import co.usersource.annoplugin.sync.ResponseHandler;
import co.usersource.annoplugin.utils.AccountUtils;

/**
 * This is the interface to let html5 interact with. This is a cordova plugin,
 * more detail please refer to
 * http://docs.phonegap.com/en/2.2.0/guide_plugin-development_android_index
 * .md.html.
 * 
 * This plugin provides services that html5 will call from native Android SDK
 * codes.
 * 
 * @author topcircler
 * 
 */
public class CordovaHttpService extends CordovaPlugin {

  // plugin action names.
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
  public static final String GET_ACCOUNT_NAME = "get_account_name";
  public static final String EXIT_COMMUNITY = "exit_community";

  private AnnoHttpService service;

  public CordovaHttpService() {
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
    } else if (GET_ACCOUNT_NAME.equals(action)) {
      getAccountName(args, callbackContext);
      return true;
    } else if (EXIT_COMMUNITY.equals(action)) {
      exitCommunity(args, callbackContext);
      return true;
    }
    return false;
  }

  /**
   * Exit community and return back to anno home.
   * 
   * @param args
   * @param callbackContext
   */
  private void exitCommunity(JSONArray args, CallbackContext callbackContext) {
    this.cordova.getActivity().finish();
  }

  /**
   * Get google account that community will use to display current user.
   * 
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  private void getAccountName(JSONArray args, CallbackContext callbackContext)
      throws JSONException {
    Account[] accounts = AccountUtils.getAllAccounts(
        this.cordova.getActivity(), null);
    if (accounts == null || accounts.length == 0) {
      callbackContext.error("No available account.");
      return;
    }
    Account account = accounts[0];
    JSONObject jobj = new JSONObject();
    jobj.put("current_user", account.name);
    callbackContext.success(jobj);
  }

  /**
   * Get the number of the specified anno flags.
   * 
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  private void countFlag(JSONArray args, final CallbackContext callbackContext)
      throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    service.countFlag(annoId, new CordovaResponseHandler(callbackContext));
  }

  /**
   * Get the number of the specified anno votes.
   * 
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  private void countVote(JSONArray args, final CallbackContext callbackContext)
      throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    service.countVote(annoId, new CordovaResponseHandler(callbackContext));
  }

  /**
   * Remove a vote for a specified anno.
   * 
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  private void removeVote(JSONArray args, final CallbackContext callbackContext)
      throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    service.removeVote(annoId, new CordovaResponseHandler(callbackContext));
  }

  /**
   * Add a vote for a specified anno.
   * 
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  private void addVote(JSONArray args, final CallbackContext callbackContext)
      throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    service.addVote(annoId, new CordovaResponseHandler(callbackContext));
  }

  /**
   * Remove a flag for a specified anno.
   * 
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  private void removeFlag(JSONArray args, final CallbackContext callbackContext)
      throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    service.removeFlag(annoId, new CordovaResponseHandler(callbackContext));
  }

  /**
   * Add a flag for the specified anno.
   * 
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  private void addFlag(JSONArray args, final CallbackContext callbackContext)
      throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    service.addFlag(annoId, new CordovaResponseHandler(callbackContext));
  }

  /**
   * Remove follow up for a specified anno.
   * 
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  private void removeFollowUp(JSONArray args,
      final CallbackContext callbackContext) throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String followupId = jobj.getString("followup_id");
    service.removeFollowup(followupId, new CordovaResponseHandler(
        callbackContext));
  }

  /**
   * Add a follow up for a specified anno.
   * 
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  private void addFollowUp(JSONArray args, final CallbackContext callbackContext)
      throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    String comment = jobj.getString("comment");
    service.addFollowup(annoId, comment, new CordovaResponseHandler(
        callbackContext));
  }

  /**
   * Update application name for a specified anno.
   * 
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  private void updateAppName(JSONArray args,
      final CallbackContext callbackContext) throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    String appName = jobj.getString("app_name");
    service.updateAppName(annoId, appName, new CordovaResponseHandler(
        callbackContext));
  }

  /**
   * Get a certain anno detail.
   * 
   * @param args
   * @param callbackContext
   * @throws JSONException
   */
  private void getAnnoDetail(JSONArray args,
      final CallbackContext callbackContext) throws JSONException {
    JSONObject jobj = args.getJSONObject(0);
    String annoId = jobj.getString("anno_id");
    service.getAnnoDetail(annoId, new CordovaResponseHandler(callbackContext));
  }

  /**
   * Get all anno list.
   * 
   * @param args
   * @param callbackContext
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

  /**
   * A common response handler to call back to html5.
   * 
   * @author topcircler
   * 
   */
  private class CordovaResponseHandler implements ResponseHandler {

    private CallbackContext callbackContext;

    CordovaResponseHandler(CallbackContext callbackContext) {
      this.callbackContext = callbackContext;
    }

    @Override
    public void handleResponse(final JSONObject response) {
      // not run in main thread.
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
