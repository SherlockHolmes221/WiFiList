package com.example.skywo.wifilistdemo;

import android.annotation.SuppressLint;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.skywo.wifilistdemo.fg.adapter.WiFiListAdapter_old;
import com.example.skywo.wifilistdemo.fg.manager.WiFiSessionManager_old;

import java.util.ArrayList;
import java.util.List;

public class MainActivity_old extends AppCompatActivity {

    private WiFiSessionManager_old wiFiSessionManagerOld;
    private ListView listView;
    private WiFiListAdapter_old myAdapter;
    private List<ScanResult> results = new ArrayList<>();
    private static final String WIFI_LOCK_NAME = "wifilock";
    private static final String TAG = "MainActivity_old";

    @SuppressLint("WifiManagerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_old);

        initUI();

        wiFiSessionManagerOld = WiFiSessionManager_old.getInstance(this);

        if(wiFiSessionManagerOld.enable()){
            Log.i(TAG, "当前WiFi可用");

            //SSID: Skyworth, BSSID: d8:15:0d:6c:20:48, Supplicant state: COMPLETED, RSSI: -54, Link speed: 72Mbps, Frequency: 2462MHz, Net ID: 27, Metered hint: false, score: 100
            Log.i(TAG,getWiFiInfo());
            // 创建wifi锁
            wiFiSessionManagerOld.creatWifiLock(WIFI_LOCK_NAME);
            // 锁定WifiLock
            wiFiSessionManagerOld.acquireWifiLock();
            searchWiFi();
            showToast("开始搜索WiFi");
        }else {
            Log.i(TAG, "当前WiFi不可用");
            //逻辑判断
            showToast("当前WiFi不可用");
        }
    }

    private void initUI() {
        listView =  findViewById(R.id.listView);
        myAdapter = new WiFiListAdapter_old(this, results);
        listView.setAdapter(myAdapter);

        findViewById(R.id.search_wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchWiFi();
            }
        });
    }

    private void showToast(String context){
        Toast.makeText(this,context,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //判断wifi是否被lock锁持用
        wiFiSessionManagerOld.releaseWifiLock();
    }

    /**
     * 开启WiFi
     * @param view
     */
    public void openWiFi(View view) {
        // 判断是否已经打开WiFi
        wiFiSessionManagerOld.openWifi();
    }

    /**
     * 关闭WiFi
     *
     * @param view
     */
    public void closeWiFi(View view) {
        wiFiSessionManagerOld.closeWifi();
    }

    /**
     * 获取当前连接的Wifi的信息
     *
     */
    public String getWiFiInfo() {
        return wiFiSessionManagerOld.getWifiInfo();
    }



    /**
     * 搜索WiFi
     */
    public void searchWiFi() {
        Log.i(TAG, "searchWiFi");
        if (!wiFiSessionManagerOld.enable()) {
            return;
        }
        // 开始扫描
        wiFiSessionManagerOld.startScan();
        // mWiFiManager.getScanResults()获取搜索的WiFi内容
        // 返回结果是当前设备所在区域能搜索出来的WiFi列表
       // results = wiFiSessionManagerOld.getWifiList();
        Log.i(TAG, "results.size:"+ results.size());
        for(ScanResult s : results){
            Log.i(TAG,s.SSID);
        }

        // 获取扫描已经保存的wifi列表配置集合
        List<WifiConfiguration> configuration = wiFiSessionManagerOld.getConfiguration();

//    for (WifiConfiguration configuration:wifiConfigs)
//    {
//      //configuration.SSID - wifi网络名称
//      //configuration.BSSID - wifi网络mac地址
//    }
        //myAdapter.notifyDataSetChanged();
    }

    private int networkid = -1;

    /**
     * 连接到某一个Wifi网络
     */
    public void connCustomNetWork(View view) {
        // 添加一个WiFi网络,如果网络不再当前方位内，就没有办法连接
        //networkid = mWiFiManager.addNetwork(wifiConfigs.get(0));
        //mWiFiManager.enableNetwork(networkid,true);

        // 连接到一个有效网络 - 1900

        WifiConfiguration configuration =
                wiFiSessionManagerOld.createWifiConfig("mac’s MacBook Pro", "", WiFiSessionManager_old.Data.WIFI_CIPHER_NOPASS);

//    WifiConfiguration configuration = createWifiConfig("360免费1",null,Data.WIFI_CIPHER_NOPASS);

        // 添加到Wifi网络
       // networkid = wiFiSessionManagerOld.addWiFiNetwork(configuration);
    }

}
