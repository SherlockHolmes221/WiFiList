package com.example.skywo.wifilistdemo.fg;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.skywo.wifilistdemo.R;
import com.example.skywo.wifilistdemo.fg.adapter.WifiListAdapter;
import com.example.skywo.wifilistdemo.fg.bean.WifiBean;
import com.example.skywo.wifilistdemo.fg.manager.WifiSessionManager;
import com.example.skywo.wifilistdemo.fg.widget.WiFiListView;
import com.example.skywo.wifilistdemo.fg.widget.WifiLinkDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    //权限请求码
    private static final int PERMISSION_REQUEST_CODE = 0;
    //两个危险权限需要动态申请
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private boolean mHasPermission;

    ProgressBar pbWifiLoading;

    List<WifiBean> realWifiList = new ArrayList<>();

    private WifiListAdapter adapter;
    private RecyclerView recyWifiList;

    //private WiFiListView wiFiListView;

    private WifiBroadcastReceiver wifiReceiver;

    private int connectType = 0;//1：连接成功？ 2 正在连接（如果wifi热点列表发生变需要该字段）

    private LinearLayoutManager mLinearLayoutManager;
    private int visibleItemCount,pastVisiblesItems,totalItemCount;
    private boolean isEnd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //检查权限
        mHasPermission = checkPermission();

        if (!mHasPermission && WifiSessionManager.isOpenWifi(MainActivity.this)) {  //未获取权限，申请权限
            requestPermission();
        } else if (mHasPermission && WifiSessionManager.isOpenWifi(MainActivity.this)) {  //已经获取权限
            initUI();
        } else {
            showToast("请打开WiFi");
        }
    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private void initUI() {
        pbWifiLoading = findViewById(R.id.pb_wifi_loading);
        hidingProgressBar();

        adapter = new WifiListAdapter(this, realWifiList);

//        wiFiListView = findViewById(R.id.list_view_show_items);
//
//        wiFiListView.setAdapter(adapter);

        recyWifiList = findViewById(R.id.recy_list_wifi);

        mLinearLayoutManager = new LinearLayoutManager(this);
        recyWifiList.setLayoutManager(mLinearLayoutManager);
        recyWifiList.setAdapter(adapter);

//        //滑动的点击事件
//        recyWifiList.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                switch(newState) {
//                    case 0:
//                        Log.i(TAG, "recyclerview已经停止滚动");
//                        //开始更新
//                        break;
//                    case 1:
//                        Log.i(TAG, "recyclerview正在被拖拽");
//                        break;
//                    case 2:
//                        Log.i(TAG, "recyclerview正在依靠惯性滚动");
//                        break;
//                }
//            }
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//            }
//        });

        recyWifiList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                visibleItemCount = mLinearLayoutManager.getChildCount();
                totalItemCount = mLinearLayoutManager.getItemCount();
                pastVisiblesItems = mLinearLayoutManager.findFirstVisibleItemPosition();

                if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                    isEnd = true;
                    Log.i(TAG, "Last Item Wow !");
                    showToast("正在刷新WiFi");
                    //进行刷新wifi的操作
                    getAndSortScaResult();
                }else
                    isEnd = false;
            }
        });

         //获取和进行排序
        getAndSortScaResult();

        /**
         * 点击item
         * 若点击的wifi未连接，需要密码则弹框
         * 不需要密码，检查是否之前配置过该网络，连接
         */
        adapter.setOnItemClickListener(new WifiListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, Object o) {
                WifiBean wifiBean = realWifiList.get(postion);
                //
                if (wifiBean.getState().equals(WifiBean.WIFI_STATE_UNCONNECT)) {
                    String capabilities = realWifiList.get(postion).getCapabilities();

                    if (!realWifiList.get(postion).isNeedPassword()) {//无需密码

                        //查看以前是否也配置过这个网络
                        WifiConfiguration tempConfig = WifiSessionManager.isExsits(wifiBean.getWifiName(), MainActivity.this);
                        if (tempConfig == null) {
                            WifiConfiguration exsits = WifiSessionManager.createWifiConfig(wifiBean.getWifiName(), null, WifiSessionManager.WifiCipherType.WIFICIPHER_NOPASS);
                            WifiSessionManager.addNetWork(exsits, MainActivity.this);
                        } else {
                            WifiSessionManager.addNetWork(tempConfig, MainActivity.this);
                        }
                    } else {   //需要密码，弹出输入密码dialog
                        noConfigurationWifi(postion);
                    }
                } else if (wifiBean.getState().equals(WifiBean.WIFI_STATE_CONNECT)) {
                    showToast("已连接");
                }
            }
        });
    }

    //之前没配置过该网络， 弹出输入密码界面
    private void noConfigurationWifi(int position) {
        WifiLinkDialog linkDialog = new WifiLinkDialog(this, R.style.dialog_download, realWifiList.get(position).getWifiName(),
                realWifiList.get(position).getCapabilities());
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
                        Log.d(TAG, "已经关闭");
                        Toast.makeText(MainActivity.this, "WIFI处于关闭状态", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case WifiManager.WIFI_STATE_DISABLING: {
                        Log.d(TAG, "正在关闭");
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLED: {
                        Log.d(TAG, "已经打开");
                        getAndSortScaResult();
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLING: {
                        Log.d(TAG, "正在打开");
                        break;
                    }
                    case WifiManager.WIFI_STATE_UNKNOWN: {
                        Log.d(TAG, "未知状态");
                        break;
                    }
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.d(TAG, "--NetworkInfo--" + info.toString());
                if (NetworkInfo.State.DISCONNECTED == info.getState()) {//wifi没连接上
                    Log.d(TAG, "wifi没连接上");
                    hidingProgressBar();
                    for (int i = 0; i < realWifiList.size(); i++) {//没连接上将 所有的连接状态都置为“未连接”
                        realWifiList.get(i).setState(WifiBean.WIFI_STATE_UNCONNECT);
                    }
                    adapter.notifyDataSetChanged();
                } else if (NetworkInfo.State.CONNECTED == info.getState()) {//wifi连接上了
                    Log.d(TAG, "wifi连接上了");
                    hidingProgressBar();
                    WifiInfo connectedWifiInfo = WifiSessionManager.getConnectedWifiInfo(MainActivity.this);
                    //连接成功 跳转界面 传递ip地址
                    showToast("wifi连接上了");
                    connectType = 1;
                    wifiListSet(connectedWifiInfo.getSSID(), connectType);
                } else if (NetworkInfo.State.CONNECTING == info.getState()) {//正在连接
                    Log.d(TAG, "wifi正在连接");
                    showProgressBar();
                    WifiInfo connectedWifiInfo = WifiSessionManager.getConnectedWifiInfo(MainActivity.this);
                    connectType = 2;
                    wifiListSet(connectedWifiInfo.getSSID(), connectType);
                }
            } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                Log.d(TAG, "网络列表变化了");
                wifiListChange();
            }
        }
    }

    /**
     * //网络状态发生改变 调用此方法！
     */
    public void wifiListChange() {
        getAndSortScaResult();
        WifiInfo connectedWifiInfo = WifiSessionManager.getConnectedWifiInfo(this);
        if (connectedWifiInfo != null) {
            wifiListSet(connectedWifiInfo.getSSID(), connectType);
        }
    }

    /**
     * 将"已连接"或者"正在连接"的wifi热点放置在第一个位置
     *
     * @param wifiName
     * @param type
     */
    public void wifiListSet(String wifiName, int type) {
        int index = -1;
        WifiBean wifiInfo = new WifiBean();
        if (isNullOrEmpty(realWifiList)) {
            return;
        }

        for (int i = 0; i < realWifiList.size(); i++) {
            realWifiList.get(i).setState(WifiBean.WIFI_STATE_UNCONNECT);
        }
        Collections.sort(realWifiList);//根据信号强度排序

        for (int i = 0; i < realWifiList.size(); i++) {
            WifiBean wifiBean = realWifiList.get(i);
            if (index == -1 && ("\"" + wifiBean.getWifiName() + "\"").equals(wifiName)) {
                index = i;
                int level = wifiBean.getLevel();
//                Log.e(TAG, String.valueOf(level));
                wifiInfo.setLevel(level);
                wifiInfo.setWifiName(wifiBean.getWifiName());
                //wifiInfo.setLevelGrade(3);
                wifiInfo.setLevelGrade(WifiSessionManager.getLevelByGrade(level));
                wifiInfo.setCapabilities(wifiBean.getCapabilities());
                if (type == 1) {
                    wifiInfo.setState(WifiBean.WIFI_STATE_CONNECT);
                } else {
                    wifiInfo.setState(WifiBean.WIFI_STATE_ON_CONNECTING);
                }
            }
        }//for
        if (index != -1) {
            realWifiList.remove(index);
            realWifiList.add(0, wifiInfo);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 检查是否已经授予权限
     *
     * @return
     */
    private boolean checkPermission() {
        for (String permission : NEEDED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 申请权限
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                NEEDED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    /**
     * 获取wifi列表然后将bean转成自己定义的WifiBean
     */
    public void getAndSortScaResult() {

        //获取扫描到的wifi的list
        List<ScanResult> scanResults = WifiSessionManager.noSameName(WifiSessionManager.getWifiScanResult(this));

        realWifiList.clear();
        if (!isNullOrEmpty(scanResults)) {
            for (int i = 0; i < scanResults.size(); i++) {
                WifiBean wifiBean = new WifiBean();

                wifiBean.setWifiName(scanResults.get(i).SSID);

                wifiBean.setState(WifiBean.WIFI_STATE_UNCONNECT);   //只要获取都假设设置成未连接，真正的状态都通过广播来确定

                String capabilities = scanResults.get(i).capabilities;
                wifiBean.setCapabilities(capabilities);

                if (WifiSessionManager.getWifiCipher(capabilities) == WifiSessionManager.WifiCipherType.WIFICIPHER_NOPASS) {
                    wifiBean.setNeedPassword(false);
                } else
                    wifiBean.setNeedPassword(true);

                int level =scanResults.get(i).level;
                wifiBean.setLevel(level);
                Log.e(TAG, "getAndSortScaResult: "+level );

                //level等级
                wifiBean.setLevelGrade(WifiSessionManager.getLevelByGrade(level));

                realWifiList.add(wifiBean);
            }//for
            //排序
            Collections.sort(realWifiList);
            adapter.notifyDataSetChanged();
        }
    }

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
                if (WifiSessionManager.isOpenWifi(MainActivity.this)) {  //如果wifi开关是开 并且 已经获取权限
                    initUI();
                } else {
                    showToast("请打开WiFi");
                }

            } else {  //用户不同意权限
                mHasPermission = false;
                showToast("获取权限失败");
            }
        }
    }


    public void showProgressBar() {
        pbWifiLoading.setVisibility(View.VISIBLE);
    }

    public void hidingProgressBar() {
        pbWifiLoading.setVisibility(View.GONE);
    }

    public static boolean isNullOrEmpty(Collection c) {
        if (null == c || c.isEmpty()) {
            return true;
        }
        return false;
    }

}