package com.example.skywo.wifilistdemo.fg.model;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.example.skywo.wifilistdemo.fg.model.impl.WifiSessionManager;

import java.util.List;

public interface WifiSession {
    List<ScanResult>  getWifiScanResult(Context context);

    boolean isWifiEnable(Context context);

    WifiInfo getConnectedWifiInfo(Context context);

    List getConfigurations(Context context);

    WifiConfiguration createWifiConfig(String SSID, String password, WifiSessionManager.WifiCipherType type);

    boolean addNetWork(WifiConfiguration config, Context context);

    WifiSessionManager.WifiCipherType getWifiCipher(String s);

    WifiConfiguration isExsits(String SSID, Context context);

    void openWifi(Context context);

    void closeWifi(Context context);

    boolean isOpenWifi(Context context);

    String getCapabilitiesString(String capabilities);

    boolean getIsWifiEnabled(Context context);

    int getLevelByGrade(int level);

    void startScanWifi(WifiManager manager);

    int getWifiState(WifiManager manager);

    void disconnect(Context context);

}
