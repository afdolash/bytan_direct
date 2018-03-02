package com.pens.afdolash.bytan_direct.utils;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;


/**
 * Created by afdol on 2/27/2018.
 */

public class DeviceAction {
    private Context context;
    private WifiP2pManager manager;
    private WifiP2pDevice device;
    private WifiP2pManager.Channel channel;

    public DeviceAction(Context context, WifiP2pManager manager, WifiP2pDevice device, WifiP2pManager.Channel channel) {
        this.context = context;
        this.manager = manager;
        this.device = device;
        this.channel = channel;
    }

    public void showDetails(WifiP2pDevice device) {
        Toast.makeText(context, device.toString(), Toast.LENGTH_SHORT).show();
    }

    public void connect(final WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(context, "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void disconnect() {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(context, "Disconnect failed. Reason code : "+ reasonCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Toast.makeText(context, "Disconnected!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void cancelDisconnect() {
        if (manager != null) {
            if (device == null || device.status == WifiP2pDevice.CONNECTED) {
                disconnect();
            }
            else if (device.status == WifiP2pDevice.AVAILABLE || device.status == WifiP2pDevice.INVITED) {
                manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, "Aborting connection", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(context, "Connect abort request failed. Reason Code: " + reasonCode, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
