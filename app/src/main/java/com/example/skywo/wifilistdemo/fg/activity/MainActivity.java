package com.example.skywo.wifilistdemo.fg.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.skywo.wifilistdemo.R;
import com.example.skywo.wifilistdemo.fg.base.BaseActivity;
import com.example.skywo.wifilistdemo.fg.adapter.WifiListAdapter;
import com.example.skywo.wifilistdemo.fg.bean.WifiBean;
import com.example.skywo.wifilistdemo.fg.model.impl.WifiSessionManager;
import com.example.skywo.wifilistdemo.fg.presenter.WifiPresenter;
import com.example.skywo.wifilistdemo.fg.UILib.WifiLinkDialog;
import com.example.skywo.wifilistdemo.fg.presenter.impl.WifiPresenterImpl;
import com.example.skywo.wifilistdemo.fg.UILib.WifiSignalView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseActivity implements WifiView{

    private static final String TAG = "MainActivity";

    /***************权限申请******************/
    //两个危险权限需要动态申请
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private boolean mHasPermission;
    /***************权限申请******************/


    /***************wifi列表******************/
    List<WifiBean> wifiBeanList = new ArrayList<>();
    private WifiListAdapter adapter;
    private RecyclerView recyWifiList;
    private LinearLayoutManager mLinearLayoutManager;
    private int visibleItemCount,pastVisiblesItems,totalItemCount;
    /***************wifi列表******************/


    /***************wifi广播******************/
    private WifiBroadcastReceiver wifiReceiver;
    /***************wifi广播******************/


    /***************头部已连接wifi信息******************/
    private WifiBean connectedWifiItem;
    private TextView tvConnectInfo;
    private LinearLayout headInfoLinearLayout;
    private WifiSignalView headWifiSignalView;
    private TextView headConnectedWiFiName;
//    private TextView headDisconnectTv;
    private ImageView headMoreIv;
    private int connectType = 0;//1：连接成功？ 2 正在连接（如果wifi热点列表发生变需要该字段）
    /***************头部已连接wifi信息******************/


    /***************wifiPresenter******************/
    private WifiPresenter wifiPresenter;
    /***************wifiPresenter******************/


    /**
     * 权限申请的回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasAllPermission = true;
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    hasAllPermission = false;   //判断用户是否同意获取权限
                    break;
                }
            }
            //如果同意权限
            if (hasAllPermission) {
                mHasPermission = true;
                if (wifiPresenter.isOpenWifi(MainActivity.this)) {  //如果wifi开关是开 并且 已经获取权限
                    initUIAndEvent();
                } else {
                    showToast("请打开WiFi");
                }

            } else {  //用户不同意权限
                mHasPermission = false;
                showToast("获取权限失败");
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_wifi_list);

        //持有wifiPresenter
        wifiPresenter = new WifiPresenterImpl(MainActivity.this);

        //检查权限
        mHasPermission = checkPermission(NEEDED_PERMISSIONS);

        if (!mHasPermission && wifiPresenter.isOpenWifi(MainActivity.this)) {   //未获取权限，申请权限
            requestPermission(NEEDED_PERMISSIONS);
        } else if (mHasPermission && wifiPresenter.isOpenWifi(MainActivity.this)) {  //已经获取权限
            initUIAndEvent();

            //获取wifi列表
            wifiPresenter.getSortScanResult(MainActivity.this,
                    connectedWifiItem == null ? null : connectedWifiItem.getWifiName(),wifiBeanList);
            Log.e(TAG, "getAndSortScaResult: "+wifiBeanList.size());

        } else {
            showToast("请打开WiFi");
        }
    }


    /**
     * 初始化页面和点击事件
     */
    private void initUIAndEvent() {
        //头部连接wifi信息
        tvConnectInfo = findViewById(R.id.tv_connect_info);
        headInfoLinearLayout = findViewById(R.id.ly_head_info);
        headWifiSignalView = findViewById(R.id.fl_head_item_icon);
        headConnectedWiFiName = findViewById(R.id.tv_item_wifi_name);
//        headDisconnectTv = findViewById(R.id.tv_head_wifi_disconnect);
        headMoreIv = findViewById(R.id.iv_head_more);

        //开始时设置为不可见
        tvConnectInfo.setText("当前无连接WiFi");
        headInfoLinearLayout.setVisibility(View.GONE);

        //more
        headMoreIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(WifiDetailActivity.jumpToDetailPage(MainActivity.this,connectedWifiItem.getWifiName(),
                        connectedWifiItem.getLevelGrade(),connectedWifiItem.getCapabilities()));
            }
        });


        //断开连接的点击事件
//        headDisconnectTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                wifiPresenter.disconnect(MainActivity.this);
//                Log.i(TAG, "onClick: disconnect" );
//            }
//        });

        //wifi列表
        recyWifiList = findViewById(R.id.recy_list_wifi);
        adapter = new WifiListAdapter(this, wifiBeanList);
        mLinearLayoutManager = new LinearLayoutManager(this);
        recyWifiList.setLayoutManager(mLinearLayoutManager);
        recyWifiList.setAdapter(adapter);

        //下拉刷新
        recyWifiList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                visibleItemCount = mLinearLayoutManager.getChildCount();
                totalItemCount = mLinearLayoutManager.getItemCount();
                pastVisiblesItems = mLinearLayoutManager.findFirstVisibleItemPosition();

                if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                    Log.i(TAG, "拉到底部");
                     showToast("正在刷新WiFi");
                    //进行刷新wifi的操作
                    wifiPresenter.getSortScanResult(MainActivity.this,
                            connectedWifiItem == null ? null : connectedWifiItem.getWifiName(),wifiBeanList);
                    recyclerView.scrollToPosition(0);
                }
            }
        });

        //list的点击连接事件
        adapter.setOnItemClickListener(new WifiListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                startActivity(WifiDetailActivity.jumpToDetailPage(MainActivity.this,wifiBeanList.get(position).getWifiName(),
                        wifiBeanList.get(position).getLevelGrade(),wifiBeanList.get(position).getCapabilities()));
            }

            /**
             * 点击连接
             * 若点击的wifi未连接，需要密码则弹框
             * 不需要密码，检查是否之前配置过该网络，连接
             */
            @Override
            public void onConnect(View view, int position, Object o) {
                WifiBean wifiBean = wifiBeanList.get(position);
                //
                if (wifiBean.getState().equals(WifiBean.WIFI_STATE_DISCONNECT)) {

                    if (!wifiBeanList.get(position).isNeedPassword()) {//无需密码

                        //查看以前是否也配置过这个网络
                        WifiConfiguration tempConfig = wifiPresenter.isExsits(wifiBean.getWifiName(), MainActivity.this);
                        if (tempConfig == null) {
                            WifiConfiguration exsits = wifiPresenter.createWifiConfig(wifiBean.getWifiName(), null, WifiSessionManager.WifiCipherType.WIFICIPHER_NOPASS);
                            wifiPresenter.addNetWork(exsits, MainActivity.this);
                        } else {
                            wifiPresenter.addNetWork(tempConfig, MainActivity.this);
                        }
                    } else {   //需要密码，弹出输入密码dialog
                        noConfigurationWifi(position);
                    }
                } else if (wifiBean.getState().equals(WifiBean.WIFI_STATE_CONNECT)) {
                    showToast("已连接");
                }
            }
        });
    }


    //之前没配置过该网络， 弹出输入密码界面
    private void noConfigurationWifi(int position) {
        WifiLinkDialog linkDialog = new WifiLinkDialog(this, R.style.dialog_download,
                wifiBeanList.get(position).getWifiName(),
                wifiBeanList.get(position).getCapabilities());
        if (!linkDialog.isShowing()) {
            linkDialog.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //注册广播
        wifiReceiver = new WifiBroadcastReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//监听wifi是开关变化的状态
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//监听wifi连接状态广播,是否连接了一个有效路由
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//监听wifi列表变化（开启一个热点或者关闭一个热点）
        this.registerReceiver(wifiReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(wifiReceiver);
    }

    //监听wifi状态
    public class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (state) {
                    /**
                     * WIFI_STATE_DISABLED    WLAN已经关闭
                     * WIFI_STATE_DISABLING   WLAN正在关闭
                     * WIFI_STATE_ENABLED     WLAN已经打开
                     * WIFI_STATE_ENABLING    WLAN正在打开
                     * WIFI_STATE_UNKNOWN     未知
                     */
                    case WifiManager.WIFI_STATE_DISABLED: {
                        Log.i(TAG, "已经关闭");
                        showToast("WiFi处于关闭状态");
                        break;
                    }
                    case WifiManager.WIFI_STATE_DISABLING: {
                        Log.i(TAG, "正在关闭");
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLED: {
                        Log.i(TAG, "已经打开");
                        wifiPresenter.getSortScanResult(MainActivity.this,
                                connectedWifiItem == null ? null : connectedWifiItem.getWifiName(),wifiBeanList);
                        Log.e(TAG, "getAndSortScaResult: "+wifiBeanList.size());
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLING: {
                        Log.i(TAG, "正在打开");
                        break;
                    }
                    case WifiManager.WIFI_STATE_UNKNOWN: {
                        Log.i(TAG, "未知状态");
                        break;
                    }
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.i(TAG, "--NetworkInfo--" + info.toString());
                if (NetworkInfo.State.DISCONNECTED == info.getState()) {//wifi没连接上
                    Log.i(TAG, "wifi没连接上");

                    //未连接的逻辑
                    wifiPresenter.disconnected(connectedWifiItem,wifiBeanList);
                    adapter.notifyDataSetChanged();

                } else if (NetworkInfo.State.CONNECTED == info.getState()) {//wifi连接上了

                    Log.e(TAG, "wifi连接上了");
                    showToast("wifi已连接");

                    //已经连接的逻辑
                    connectType = 1;
                    wifiPresenter.connectingOrConnected(MainActivity.this,connectedWifiItem,wifiBeanList,connectType);

                } else if (NetworkInfo.State.CONNECTING == info.getState()) {//正在连接
                    Log.e(TAG, "wifi正在连接");

                    //正在连接的逻辑
                    connectType = 2;
                    wifiPresenter.connectingOrConnected(MainActivity.this,connectedWifiItem,wifiBeanList,connectType);

                }
            } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                Log.e(TAG, "网络列表变化了");
                wifiListChange();
            }
        }
    }

    /**
     * //网络状态发生改变
     */
    public void wifiListChange() {
        wifiPresenter.getSortScanResult(MainActivity.this,
                connectedWifiItem == null ? null : connectedWifiItem.getWifiName(),wifiBeanList);
        Log.e(TAG, "getAndSortScaResult: "+wifiBeanList.size());

        wifiPresenter.wifiListChange(MainActivity.this,connectedWifiItem,wifiBeanList,connectType);
    }

    /**
     * 更新头部状态信息
     */
    @Override
    public void refreshConnectedWiFiInfo(WifiBean connectedWifiItem) {
        Log.e(TAG, "refreshConnectedWiFiInfo: ");
        if(connectedWifiItem != null){
            this.connectedWifiItem = connectedWifiItem;
            if(connectedWifiItem.getState().equals(WifiBean.WIFI_STATE_CONNECT)){
                Log.e(TAG, "refreshConnectedWiFiInfo: "+"WIFI_STATE_CONNECT" );

                //更新状态信息
                tvConnectInfo.setText(R.string.wifi_connected);

                headInfoLinearLayout.setVisibility(View.VISIBLE);
                //更新WifiSignalView信息
                headWifiSignalView.setSignalLevel(connectedWifiItem.getLevelGrade());
                //停止动画
                headWifiSignalView.stopSignalAnimation();
                //更新ssid信息
                headConnectedWiFiName.setText(connectedWifiItem.getWifiName().length() <=20 ?
                        connectedWifiItem.getWifiName() : connectedWifiItem.getWifiName().substring(0,20)+"...");

                //headDisconnectTv.setVisibility(View.VISIBLE);
                headMoreIv.setVisibility(View.VISIBLE);

            }else if(connectedWifiItem.getState().equals(WifiBean.WIFI_STATE_CONNECTING)){
                Log.e(TAG, "refreshConnectedWiFiInfo: "+"WIFI_STATE_CONNECTING" );

                tvConnectInfo.setText(R.string.wifi_connecting);
                headInfoLinearLayout.setVisibility(View.VISIBLE);
                //更新WifiSignalView信息
                headWifiSignalView.setSignalLevel(connectedWifiItem.getLevelGrade());
                //开始动画
                headWifiSignalView.startSignalAnimation();
                //更新ssid信息
                headConnectedWiFiName.setText(connectedWifiItem.getWifiName().length() <=20 ?
                        connectedWifiItem.getWifiName() : connectedWifiItem.getWifiName().substring(20)+"...");

                //隐藏按钮
                //headDisconnectTv.setVisibility(View.GONE);
                headMoreIv.setVisibility(View.VISIBLE);
            }else {
                tvConnectInfo.setText(R.string.wifi_disconnected);
                headInfoLinearLayout.setVisibility(View.GONE);
            }
        }else {
            Log.e(TAG, "refreshConnectedWiFiInfo: "+"null" );
            headInfoLinearLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateList() {
        adapter.notifyDataSetChanged();
    }

}