/**
 */
package com.cns.plugin_clisitef;

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
    final JSONArray fargs = args;
    
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

          //-- Passar oe parâmetros configuração...
          yourIntent.putExtra("sitefIp", "192.168.2.85");
          yourIntent.putExtra("sitefEmpresa", "00000000");
          yourIntent.putExtra("sitefTerminal", "SE000001");
          yourIntent.putExtra("sitefParametros","[TipoPinPad=ANDROID_BT;]");

          //-- Passar oe parâmetros venda...
          yourIntent.putExtra("sitefValor", 12.40);
          yourIntent.putExtra("sitefCupomFiscal", 123456);
          yourIntent.putExtra("sitefData", "");
          yourIntent.putExtra("sitefHora","");
          yourIntent.putExtra("sitefRestricoes","");

          that.cordova.startActivityForResult(that, yourIntent, REQUEST_CODE);
        }
      });
    } else if(action.equals("vende")) {
      cordova.getThreadPool().execute(new Runnable() {
        public void run() {
          Intent yourIntent = new Intent(that.cordova.getActivity().getBaseContext(), XActivity.class);
          //that.cordova.getActivity().startActivity(yourIntent);
          //callbackContext.success(); // Thread-safe.

          try {
            JSONObject tefConfig = fargs.getJSONObject(0);
            JSONObject tefVenda = fargs.getJSONObject(1);
            
            //-- Passar oe parâmetros configuração...
            yourIntent.putExtra("sitefIp", tefConfig.getString("sitefIp"));
            yourIntent.putExtra("sitefEmpresa", tefConfig.getString("sitefEmpresa"));
            yourIntent.putExtra("sitefTerminal", tefConfig.getString("sitefTerminal"));
            yourIntent.putExtra("sitefParametros", tefConfig.getString("sitefParametros"));

            //-- Passar oe parâmetros venda...
            yourIntent.putExtra("sitefValor", tefVenda.getDouble("sitefValor"));
            yourIntent.putExtra("sitefCupomFiscal", tefVenda.getInt("sitefValor"));
            yourIntent.putExtra("sitefData", tefVenda.getString("sitefData"));
            yourIntent.putExtra("sitefHora", tefVenda.getString("sitefHora"));
            yourIntent.putExtra("sitefRestricoes", tefVenda.getString("sitefRestricoes"));
          } catch (JSONException e) {
          }

          that.cordova.startActivityForResult(that, yourIntent, REQUEST_CODE);
        }
      });
    }
    return true;
  }

  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (requestCode == REQUEST_CODE && this.callbackContext != null) {
      JSONObject ret = new JSONObject();
      try {
        ret.put("cancel",false);
        ret.put("success",false);
        ret.put("message","");
        ret.put("retCupomCliente","");
        ret.put("retCupomEstab","");
        if (intent == null) {
          ret.put("cancel",true);
        } else {
          if (intent.getExtras().getBoolean("retVendaOK", false)) {
            ret.put("success",true);
            ret.put("retCupomCliente",intent.getExtras().getString("retCupomCliente", ""));
            ret.put("retCupomEstab",intent.getExtras().getString("retCupomEstab", ""));
            ret.put("message",intent.getExtras().getString("retVendaMsg", ""));
          } else {
            ret.put("success",false);
            ret.put("message",intent.getExtras().getString("retVendaMsg", "ERRO Sem Retorno!!!"));
          }
        }
      } catch (JSONException e) {
      }
      this.callbackContext.success(ret);
    }
  }

}
