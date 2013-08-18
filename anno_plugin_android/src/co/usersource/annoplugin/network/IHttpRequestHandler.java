/**
 * 
 */
package co.usersource.annoplugin.network;

import org.json.JSONObject;

/**
 * @author rsh
 *
 */
public interface IHttpRequestHandler {

	void onRequest(JSONObject response);

}
