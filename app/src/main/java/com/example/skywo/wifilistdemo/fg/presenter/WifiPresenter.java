package com.example.skywo.wifilistdemo.fg.presenter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;

import com.example.skywo.wifilistdemo.fg.model.impl.WifiSessionManager;

import java.util.List;

public interface WifiPresenter {

    void refreshConnectedWiFiInfo();

    List<ScanResult> getWifiScanResult(Context context);

    boolean isOpenWifi(Context context);

    void disconnect(Context context);

    WifiConfiguration isExsits(String ssid,Context context);

    WifiConfiguration createWifiConfig(String ssid,String password,WifiSessionManager.WifiCipherType wifiCipherType);

    void addNetWork(WifiConfiguration wifiConfiguration,Context context);

    WifiSessionManager.WifiCipherType getWifiCipher(String capabilities);

    int getLevelByGrade(int level);

    WifiInfo getConnectedWifiInfo(Context context);
}
