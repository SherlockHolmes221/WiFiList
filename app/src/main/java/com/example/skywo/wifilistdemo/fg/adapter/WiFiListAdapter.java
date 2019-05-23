package com.example.skywo.wifilistdemo.fg.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.example.skywo.wifilistdemo.R;
import com.example.skywo.wifilistdemo.fg.MainActivity;
import com.example.skywo.wifilistdemo.fg.bean.WifiBean;

import java.util.List;

public class WiFiListAdapter extends RecyclerView.Adapter<WiFiListAdapter.MyViewHolder> {

    private Context mContext;
    private List<WifiBean> resultList;
    private onItemClickListener onItemClickListener;

    public void setOnItemClickListener(WiFiListAdapter.onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public WiFiListAdapter(Context mContext, List<WifiBean> resultList) {
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
        holder.tvItemWifiName.setText(bean.getWifiName());
        if(bean.isNeedPassword())
            holder.tvItemWifiStatus.setText("(有密码)");
        else
            holder.tvItemWifiStatus.setText("(无密码)");

        //可以传递给adapter的数据都是经过处理的，已连接或者正在连接状态的wifi都是处于集合中的首位，所以可以写出如下判断
        if(position == 0  && (MainActivity.WIFI_STATE_ON_CONNECTING.equals(bean.getState()) || MainActivity.WIFI_STATE_CONNECT.equals(bean.getState()))){
            holder.tvItemWifiName.setTextColor(mContext.getResources().getColor(R.color.homecolor1));
            holder.tvItemWifiStatus.setTextColor(mContext.getResources().getColor(R.color.homecolor1));
        }else{
            holder.tvItemWifiName.setTextColor(mContext.getResources().getColor(R.color.gray_home));
            holder.tvItemWifiStatus.setTextColor(mContext.getResources().getColor(R.color.gray_home));
        }

        holder.itemview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onItemClickListener.onItemClick(view,position,bean);
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
        TextView tvItemWifiName, tvItemWifiStatus;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemview = itemView;
            tvItemWifiName = (TextView) itemView.findViewById(R.id.tv_item_wifi_name);
            tvItemWifiStatus = (TextView) itemView.findViewById(R.id.tv_item_wifi_status);
        }

    }

    public interface onItemClickListener{
        void onItemClick(View view, int postion, Object o);
    }

}
