package com.electronics.invento.wechatp2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

public class WifiBroadCastReceiver extends BroadcastReceiver {
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private HomeActivity mActivity;

    public WifiBroadCastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, HomeActivity activity) {
        super();
        this.mWifiP2pManager = wifiP2pManager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            //Check to see if Wi-Fi is enabled and notify appropriate activity
            //Broadcast when Wi-Fi P2P is enabled or disabled on the device.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "Wifi is ON", Toast.LENGTH_SHORT).show();
            } else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                Toast.makeText(context, "Wifi is OFF", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error(): WIFI_P2P_STATE_CHANGED_ACTION", Toast.LENGTH_SHORT).show();
                mActivity.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //Call WifiP2pManager.requestPeers() to get a list of current peers
            //Broadcast when you call discoverPeers().
            // You usually want to call requestPeers() to get an updated list of peers
            // if you handle this intent in your application.
            if (mWifiP2pManager != null) {
                mWifiP2pManager.requestPeers(mChannel, mActivity.peerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            //Broadcast when the state of the device's Wi-Fi connection changes.
            if (mWifiP2pManager == null) {
                return;
            }

            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                //mWifiP2pManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListener);
            } else {
                mActivity.textView_connectionStatus.setText("Device Disconnected");
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            //Respond to this device's wifi state changing
            //Broadcast when a device's details have changed, such as the device's name.

        } else {
            //It's a disconnect
            mActivity.resetData();
        }
    }
}