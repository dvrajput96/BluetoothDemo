package com.example.pc.bluetoothdemo;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pc on 2/8/18.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {


    private MainActivity mainActivity;
    private List<DeviceData> deviceDataList;

    public RecyclerAdapter(MainActivity mainActivity, List<DeviceData> deviceDataList) {
        this.mainActivity = mainActivity;
        this.deviceDataList = deviceDataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_adapter, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        final DeviceData deviceData = deviceDataList.get(position);

        holder.txtName.setText(deviceData.getName());
        holder.txtid.setText(deviceData.getMacID());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.openDetails(position, deviceData.getMacID(), deviceData.getName());
            }
        });


    }

    @Override
    public int getItemCount() {
        return deviceDataList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtid;
        TextView txtName;

        public MyViewHolder(View itemView) {
            super(itemView);
            txtName = (TextView) itemView.findViewById(R.id.txtname);
            txtid = (TextView) itemView.findViewById(R.id.txtid);

        }
    }
}
