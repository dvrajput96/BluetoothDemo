package com.example.pc.bluetoothdemo;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 2/9/18.
 */

public class RecyclerAvailListAdapter extends RecyclerView.Adapter<RecyclerAvailListAdapter.ViewHolder> {

    private MainActivity mainActivity;
    private List<DeviceData> deviceDataList;

    public RecyclerAvailListAdapter(MainActivity mainActivity, List<DeviceData> deviceDataList) {
        this.mainActivity = mainActivity;
        this.deviceDataList = deviceDataList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_nearby_device_adapter, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        DeviceData deviceData = deviceDataList.get(position);
        String deviceName = deviceData.getName();
        final String deviceID = deviceData.getMacID();

        if (deviceName != null) {
            holder.txtName.setText(deviceName);
        } else {
            holder.txtName.setText("Unkown device");
        }
        Log.d("TAG", "=" + deviceData.isHide());
        holder.txtPair.setVisibility(deviceData.getIsConnect() == 0 ? View.GONE : View.VISIBLE);
        holder.txtPair.setText(deviceData.getIsConnect() == 2 ? " Connected" : " Pairing...");

        holder.txtid.setText(deviceID);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.openData(position, deviceID);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deviceDataList.size();
    }

    public void addPairing(ArrayList<DeviceData> deviceData) {
        deviceDataList = deviceData;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtid;
        TextView txtName;
        TextView txtPair;

        public ViewHolder(View itemView) {
            super(itemView);
            txtName = (TextView) itemView.findViewById(R.id.txtdevicename);
            txtid = (TextView) itemView.findViewById(R.id.txtdeviceid);
            txtPair = (TextView) itemView.findViewById(R.id.txtpair);

        }
    }
}
