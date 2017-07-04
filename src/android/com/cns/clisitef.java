/**
 */
package com.cns;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.util.Log;

import java.util.Date;

public class clisitef extends CordovaPlugin {
  private static final String TAG = "clisitef";

  private CallbackContext callbackContext;

  public static final int REQUEST_CODE = 0x0ba7c0de;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Log.d(TAG, "Initializing clisitef");
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

    this.callbackContext = callbackContext;

    final CordovaPlugin that = this;

    if(action.equals("echo")) {
      String message = args.getString(0); 
      if (message != null && message.length() > 0) {
        final PluginResult result = new PluginResult(PluginResult.Status.OK, message);
        callbackContext.sendPluginResult(result);
      } else {
        final PluginResult result = new PluginResult(PluginResult.Status.OK, "Expected one non-empty string argument.");
        callbackContext.sendPluginResult(result);
      }      
    } else if(action.equals("getDate")) {
      // An example of returning data back to the web layer
      final PluginResult result = new PluginResult(PluginResult.Status.OK, (new Date()).toString());
      callbackContext.sendPluginResult(result);
    } else if(action.equals("test")) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          Intent yourIntent = new Intent(that.cordova.getActivity().getBaseContext(), XActivity.class);
          //that.cordova.getActivity().startActivity(yourIntent);
          //callbackContext.success(); // Thread-safe.

          that.cordova.startActivityForResult(that, yourIntent, REQUEST_CODE);
        }
      });
    }
    return true;
  }

  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (requestCode == REQUEST_CODE && this.callbackContext != null) {
      this.callbackContext.success("retorno");
    }
  }

}
