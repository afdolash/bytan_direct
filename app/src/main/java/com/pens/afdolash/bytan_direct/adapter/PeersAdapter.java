package com.pens.afdolash.bytan_direct.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pens.afdolash.bytan_direct.R;
import com.pens.afdolash.bytan_direct.activity.DiscoveryActivity;
import com.pens.afdolash.bytan_direct.utils.DeviceAction;

import java.util.List;

/**
 * Created by afdol on 2/26/2018.
 */

public class PeersAdapter extends RecyclerView.Adapter<PeersAdapter.ViewHolder> {

    private Context context;
    private List<WifiP2pDevice> p2pDevices;
    private WifiP2pManager manager;
    private WifiP2pDevice device;
    private WifiP2pManager.Channel channel;

    private DeviceAction deviceAction;

    public PeersAdapter(Context context, List<WifiP2pDevice> p2pDevices, WifiP2pManager manager, WifiP2pDevice device, WifiP2pManager.Channel channel) {
        this.context = context;
        this.p2pDevices = p2pDevices;
        this.manager = manager;
        this.device = device;
        this.channel = channel;

        deviceAction = new DeviceAction(context, manager, device, channel);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bt_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final WifiP2pDevice p2pDevice = p2pDevices.get(position);

        holder.tvName.setText(p2pDevice.deviceName);
        holder.tvStatus.setText(getDeviceStatus(p2pDevice.status));
        holder.cardPeer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = p2pDevice.deviceAddress;
                config.wps.setup = WpsInfo.PBC;

                deviceAction.connect(config);
            }
        });
        holder.cardPeer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                deviceAction.disconnect();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return p2pDevices.size();
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName, tvStatus;
        public RelativeLayout cardPeer;

        public ViewHolder(View itemView) {
            super(itemView);

            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvStatus = (TextView) itemView.findViewById(R.id.tv_status);
            cardPeer = (RelativeLayout) itemView.findViewById(R.id.card_peer);
        }
    }
}
