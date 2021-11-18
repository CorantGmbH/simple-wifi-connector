package de.corant.simplewificonnector;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.MacAddress;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This class echoes a string called from JavaScript.
 */
public class simplewificonnector extends CordovaPlugin {

    private final BroadcastReceiver networkChangedReceiver = new NetworkChangedReceiver();
    private static final IntentFilter NETWORK_STATE_CHANGED_FILTER = new IntentFilter();
    private ConnectivityManager.NetworkCallback networkCallback;
    private String TAG = "NETWORK";
    private String _ssid = "";
    private CallbackContext _callbackContext;
    private int disconnectCount = 0;

    static {
        NETWORK_STATE_CHANGED_FILTER.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        _callbackContext = callbackContext;
        Context context = this.cordova.getActivity().getApplicationContext();
        if (action.equals("connect")) {
            String ssid = args.getString(0);
            _ssid = ssid;
            String password = args.getString(1);
            this.connect(ssid, password, callbackContext, context);
            return true;
        }
        return false;
    }

    private void connect(String ssid, String password, CallbackContext callbackContext, Context context) {
        if (ssid != null && ssid.length() > 0) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                final NetworkSpecifier specifier =
                        new WifiNetworkSpecifier.Builder()
                                .setSsid(ssid)
                                .setWpa2Passphrase(password)
                                .build();
                final NetworkRequest request =
                        new NetworkRequest.Builder()
                                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                                .setNetworkSpecifier(specifier)
                                .build();
                final ConnectivityManager connectivityManager = (ConnectivityManager)
                        context.getSystemService(Context.CONNECTIVITY_SERVICE);

                networkCallback = new ConnectivityManager.NetworkCallback() {

                    @Override
                    public void onAvailable(Network network) {
                        connectivityManager.bindProcessToNetwork(network);
                        connectivityManager.setProcessDefaultNetwork(network);
                        Log.i("network", "isConnected");
                        callbackContext.success(ssid);
                    }

                    @Override
                    public void onUnavailable() {
                        Log.i("network", "connection error");
                        connectivityManager.unregisterNetworkCallback(networkCallback);
                        callbackContext.error("Network unavailable or wrong password");
                    }
                };

                connectivityManager.requestNetwork(request, networkCallback);
                //connectivityManager.unregisterNetworkCallback(networkCallback);
            } else {
                disconnectCount = 0;
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = String.format("\"%s\"", ssid);
                wifiConfig.preSharedKey = String.format("\"%s\"", password);
                WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
                cordova.getActivity().getApplicationContext().registerReceiver(networkChangedReceiver, NETWORK_STATE_CHANGED_FILTER);
                if (!wifiManager.isWifiEnabled())
                    wifiManager.setWifiEnabled(true);
                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disconnect();
                boolean success = wifiManager.enableNetwork(netId, true);
            }
        } else {
            callbackContext.error("Expected one non-empty wifi credentials");
        }
    }

    private class NetworkChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {

                Log.d(TAG, "NETWORK_STATE_CHANGED_ACTION");

                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                NetworkInfo.State state = networkInfo.getState();
                String newSSID = networkInfo.getExtraInfo();
                if(newSSID != null) {
                    newSSID = newSSID.replaceAll("\"", "");
                    ;
                    if (newSSID.equals(_ssid))
                        _callbackContext.success(_ssid);
                } else if (state.toString().equals("CONNECTED") && disconnectCount > 0) {
                    _callbackContext.success(_ssid);
                } else if (state.toString().equals("CONNECTED")) {
                    disconnectCount++;
                }
            }
        }
    }
}
