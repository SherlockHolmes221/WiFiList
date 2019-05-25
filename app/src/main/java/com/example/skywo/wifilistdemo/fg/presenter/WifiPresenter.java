package com.example.skywo.wifilistdemo.fg.presenter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;

import com.example.skywo.wifilistdemo.fg.bean.WifiBean;
import com.example.skywo.wifilistdemo.fg.model.impl.WifiSessionManager;

import java.util.List;

public interface WifiPresenter {

    void getSortScanResult(Context context,String ssid,List<WifiBean> wifiBeanList);

    void disconnected(WifiBean connectedWifiItem,List<WifiBean> wifiBeanList);

    void connectingOrConnected(Context context,WifiBean connectedWifiItem,List<WifiBean> wifiBeanList,int connectType);

    void wifiListChange(Context context,WifiBean connectedWifiItem,List<WifiBean> wifiBeanList,int connectType);

    boolean isOpenWifi(Context context);

    void disconnect(Context context);

    WifiConfiguration isExsits(String ssid,Context context);

    WifiConfiguration createWifiConfig(String ssid,String password,WifiSessionManager.WifiCipherType wifiCipherType);

    void addNetWork(WifiConfiguration wifiConfiguration,Context context);

    WifiSessionManager.WifiCipherType getWifiCipher(String capabilities);


}
