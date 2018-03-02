package com.pens.afdolash.bytan_direct.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pens.afdolash.bytan_direct.R;
import com.pens.afdolash.bytan_direct.activity.DiscoveryActivity;
import com.pens.afdolash.bytan_direct.adapter.PeersAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class DiscoveryFragment extends Fragment implements WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

    private List<WifiP2pDevice> mPeers = new ArrayList<>();
    private WifiP2pDevice mDevice;
    private WifiManager mWifiManager;

    private RecyclerView recyclerPeers;
    private ProgressDialog progressDialog = null;
    private LinearLayout linearMyDevice;
    private TextView tvMyName, tvMyMac;

    public DiscoveryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_discovery, container, false);

        mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        recyclerPeers = (RecyclerView) view.findViewById(R.id.recycler_peers);
        recyclerPeers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPeers.setAdapter(new PeersAdapter(getContext(), mPeers, ((DiscoveryActivity) getActivity()).getmManager(), mDevice, ((DiscoveryActivity) getActivity()).getmChannel()));

        linearMyDevice = (LinearLayout) view.findViewById(R.id.linear_my_devices);
        tvMyName = (TextView) view.findViewById(R.id.tv_name);
        tvMyMac = (TextView) view.findViewById(R.id.tv_mac);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Check Android Wifi p2p is support or not
        if (!mWifiManager.isP2pSupported()) {
            Toast.makeText(getContext(), R.string.p2p_supported, Toast.LENGTH_SHORT).show();
            return ;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check Wifi is turn on
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }

        progressDialog = ProgressDialog.show(
                getContext(),
                "Press back to cancel",
                "Finding peers",
                true,
                true,
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {

                    }
                });

        this.onInitiateDiscovery();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mWifiManager.setWifiEnabled(false);
    }


    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void clearPeers() {
        mPeers.clear();
        recyclerPeers.getAdapter().notifyDataSetChanged();
    }


    /**
     * Update UI for this device.
     * @param device WifiP2pDevice object
     */
    public void updateThisDevice(WifiP2pDevice device) {
        String[] macAddress = mWifiManager.getConnectionInfo().getMacAddress().split(":");
        String myMacAddress = macAddress[0];

        for (int i = 1; i < macAddress.length; i++) {
            myMacAddress += " : "+ macAddress[i];
        }

        tvMyName.setText(device.deviceName);
        tvMyMac.setText(myMacAddress);

        Toast.makeText(getContext(), device.deviceName +" - "+ getDeviceStatus(device.status), Toast.LENGTH_SHORT).show();
    }

    private static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }


    /**
     * Discovery P2P devices.
     */
    public void onInitiateDiscovery() {
        if (!((DiscoveryActivity) getActivity()).isWifiP2pEnabled()) {
            progressDialog.dismiss();
            Toast.makeText(getContext(), R.string.p2p_off_warning, Toast.LENGTH_SHORT).show();
            return;
        }

        ((DiscoveryActivity) getActivity()).getmManager().discoverPeers(((DiscoveryActivity) getActivity()).getmChannel(), new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Discovery Initiated.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                onInitiateDiscovery();
                Toast.makeText(getContext(), "Discovery Failed : " + reasonCode, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Stopping discovery P2P devices.
     */
    public void onStopDiscovery() {
        ((DiscoveryActivity) getActivity()).getmManager().stopPeerDiscovery(((DiscoveryActivity) getActivity()).getmChannel(), new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Stopping peer discovery.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(getContext(), "Stop peer discovery failed. Reason code : "+ reasonCode, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        mPeers.clear();
        mPeers.addAll(wifiP2pDeviceList.getDeviceList());
        recyclerPeers.getAdapter().notifyDataSetChanged();

        if (mPeers.size() == 0) {
            Toast.makeText(getContext(), R.string.no_device, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        // The owner IP is now known.
        Toast.makeText(getContext(), getResources().getString(R.string.group_owner_text) + ((wifiP2pInfo.isGroupOwner == true) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no)), Toast.LENGTH_SHORT).show();

        // InetAddress from WifiP2pInfo struct.
        Toast.makeText(getContext(), "Group Owner IP - " + wifiP2pInfo.groupOwnerAddress.getHostAddress(), Toast.LENGTH_SHORT).show();
    }
}
