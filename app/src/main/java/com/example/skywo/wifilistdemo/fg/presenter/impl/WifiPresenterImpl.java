package com.example.skywo.wifilistdemo.fg.presenter.impl;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;

import com.example.skywo.wifilistdemo.fg.activity.WifiView;
import com.example.skywo.wifilistdemo.fg.model.WifiSession;
import com.example.skywo.wifilistdemo.fg.model.impl.WifiSessionManager;
import com.example.skywo.wifilistdemo.fg.presenter.WifiPresenter;

import java.util.List;

public class WifiPresenterImpl implements WifiPresenter {

    private WifiView wifiView;
    private WifiSession wifiSession;

    public WifiPresenterImpl(WifiView wifiView) {
        this.wifiView = wifiView;
        wifiSession = new WifiSessionManager();
    }

    public WifiPresenterImpl() {
        wifiSession = new WifiSessionManager();
    }

    @Override
    public void refreshConnectedWiFiInfo() {
        wifiView.refreshConnectedWiFiInfo();
    }

    @Override
    public List<ScanResult>  getWifiScanResult(Context context) {
        return wifiSession.getWifiScanResult(context);
    }

    @Override
    public boolean isOpenWifi(Context context) {
        return wifiSession.isOpenWifi(context);
    }

    @Override
    public void disconnect(Context context) {
        wifiSession.disconnect(context);
    }

    @Override
    public WifiConfiguration isExsits(String ssid, Context context) {
        return wifiSession.isExsits(ssid,context);
    }

    @Override
    public WifiConfiguration createWifiConfig(String ssid, String password, WifiSessionManager.WifiCipherType wifiCipherType) {
        return wifiSession.createWifiConfig(ssid,password,wifiCipherType);
    }

    @Override
    public void addNetWork(WifiConfiguration wifiConfiguration, Context context) {
        wifiSession.addNetWork(wifiConfiguration,context);
    }

    @Override
    public WifiSessionManager.WifiCipherType getWifiCipher(String capabilities) {
        return wifiSession.getWifiCipher(capabilities);
    }

    @Override
    public int getLevelByGrade(int level) {
        return wifiSession.getLevelByGrade(level);
    }

    @Override
    public WifiInfo getConnectedWifiInfo(Context context) {
        return wifiSession.getConnectedWifiInfo(context);
    }


}