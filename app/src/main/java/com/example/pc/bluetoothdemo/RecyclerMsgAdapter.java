package com.example.pc.bluetoothdemo;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by android on 14/2/18.
 */

public class RecyclerMsgAdapter extends RecyclerView.Adapter<RecyclerMsgAdapter.MyViewHolder> {

    private ChatActivity chatActivity;
    private ArrayList<DeviceData> msgInfo;

    public RecyclerMsgAdapter(ChatActivity chatActivity, ArrayList<DeviceData> msgInfo) {
        this.chatActivity = chatActivity;
        this.msgInfo = msgInfo;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        final DeviceData deviceData = msgInfo.get(position);

        if (!deviceData.isReceiver()) {
            holder.txtMessageForMe.setVisibility(View.GONE);
            holder.txtMsgForReceiver.setVisibility(View.VISIBLE);
            holder.txtMsgForReceiver.setText(deviceData.getMsg());
        } else {
            holder.txtMsgForReceiver.setVisibility(View.GONE);
            holder.txtMessageForMe.setVisibility(View.VISIBLE);
            holder.txtMessageForMe.setText(deviceData.getMsg());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatActivity.openDetails(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return msgInfo.size();
    }

    public void addMsg(DeviceData deviceData) {

        msgInfo.add(deviceData);
        notifyDataSetChanged();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtMsgForReceiver;
        TextView txtMessageForMe;

        public MyViewHolder(View itemView) {
            super(itemView);
            txtMsgForReceiver = (TextView) itemView.findViewById(R.id.txtmsgforreceiver);
            txtMessageForMe = (TextView) itemView.findViewById(R.id.txtmsgforme);
        }
    }
}
