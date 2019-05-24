package com.example.skywo.wifilistdemo.fg.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.skywo.wifilistdemo.R;
import com.example.skywo.wifilistdemo.fg.bean.WifiBean;
import com.example.skywo.wifilistdemo.fg.view.WifiFrameLayout;

import java.util.List;

public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.MyViewHolder> {

    private Context mContext;
    private List<WifiBean> resultList;
    private onItemClickListener onItemClickListener;

    private static final String TAG = "WifiListAdapter";

    public void setOnItemClickListener(WifiListAdapter.onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public WifiListAdapter(Context mContext, List<WifiBean> resultList) {
        this.mContext = mContext;
        this.resultList = resultList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_wifi_list, parent, false);
        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final WifiBean bean = resultList.get(position);
        holder.tvItemWifiName.setText(bean.getWifiName().length() <= 20 ? bean.getWifiName() : bean.getWifiName().substring(20)+"...");
//        if(bean.isNeedPassword())
//            holder.tvItemWifiStatus.setText("(有密码)");
//        else
//            holder.tvItemWifiStatus.setText("(无密码)");

        //可以传递给adapter的数据都是经过处理的，已连接或者正在连接状态的wifi都是处于集合中的首位，所以可以写出如下判断
//        if(position == 0  && (WifiBean.WIFI_STATE_CONNECTING.equals(bean.getState()) || WifiBean.WIFI_STATE_CONNECT.equals(bean.getState()))){
//            holder.tvItemWifiName.setTextColor(mContext.getResources().getColor(R.color.homecolor1));
//           // holder.tvItemWifiStatus.setTextColor(mContext.getResources().getColor(R.color.homecolor1));
//        }else{
//            holder.tvItemWifiName.setTextColor(mContext.getResources().getColor(R.color.gray_home));
//            //holder.tvItemWifiStatus.setTextColor(mContext.getResources().getColor(R.color.gray_home));
//        }

        holder.flWiFiSignalView.update(bean.getLevelGrade(),bean.isNeedPassword());

        holder.itemview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onConnect(view,position,bean);
            }
        });

        if(bean.isNeedPassword()){
            holder.tvConnect.setVisibility(View.GONE);
        }else{
            holder.tvConnect.setVisibility(View.VISIBLE);
            holder.tvConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onConnect(view,position,bean);
                }
            });
        }

        holder.ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(position);
            }
        });

    }

    public void replaceAll(List<WifiBean> datas) {
        if (resultList.size() > 0) {
            resultList.clear();
        }
        resultList.addAll(datas);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder{

        View itemview;
        TextView tvItemWifiName;
       // TextView tvItemWifiStatus;
        WifiFrameLayout flWiFiSignalView;
        ImageView ivMore;

        TextView tvConnect;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemview = itemView;
            tvItemWifiName = itemView.findViewById(R.id.tv_item_wifi_name);
           // tvItemWifiStatus = itemView.findViewById(R.id.tv_item_wifi_status);
            flWiFiSignalView = itemView.findViewById(R.id.fl_item_icon);
            tvConnect = itemView.findViewById(R.id.tv_item_wifi_disconnect);
            ivMore = itemView.findViewById(R.id.iv_item_more);
        }
    }

    public interface onItemClickListener{
        void onItemClick(int position);
        void onConnect(View view,int position,Object o);
    }

}
