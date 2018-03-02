package com.pens.afdolash.bytan_direct.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pens.afdolash.bytan_direct.R;
import com.pens.afdolash.bytan_direct.adapter.PeersAdapter;
import com.pens.afdolash.bytan_direct.fragment.DiscoveryFragment;
import com.pens.afdolash.bytan_direct.utils.WifiDirectBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

public class DiscoveryActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver = null;

    private final IntentFilter intentFilter = new IntentFilter();

    private boolean isWifiP2pEnabled = true;
    private boolean retryChannel = false;

    private DiscoveryFragment discoveryFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);

        // Add necessary intent values to be matched.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        discoveryFragment = new DiscoveryFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, discoveryFragment)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();

        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);

        try {
            unregisterReceiver(mReceiver);
            registerReceiver(mReceiver, intentFilter);
        } catch (Exception e) {
            registerReceiver(mReceiver, intentFilter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mReceiver);

            discoveryFragment.onStopDiscovery();
        }
        catch (Exception e) {
            Toast.makeText(this, R.string.unregistered_receiver, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onChannelDisconnected() {
        // We will try once more
        if (mManager != null && !retryChannel) {
            discoveryFragment.clearPeers();

            retryChannel = true;
            mManager.initialize(this, getMainLooper(), this);

            Toast.makeText(this, R.string.channel_lost, Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, R.string.channel_lost_permanent, Toast.LENGTH_LONG).show();
        }
    }


    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    /**
     * @return Get is WiFi P2P enable
     */
    public boolean isWifiP2pEnabled() {
        return isWifiP2pEnabled;
    }

    /**
     * @return Get Wifi P2P Manager
     */
    public WifiP2pManager getmManager() {
        return mManager;
    }

    /**
     * @return Get Wifi P2P Manager . Channel
     */
    public WifiP2pManager.Channel getmChannel() {
        return mChannel;
    }

    /**
     * @return Get DiscoveryFragment access from DiscoveryActivity
     */
    public DiscoveryFragment getDiscoveryFragment() {
        return discoveryFragment;
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        discoveryFragment.clearPeers();
    }
}
