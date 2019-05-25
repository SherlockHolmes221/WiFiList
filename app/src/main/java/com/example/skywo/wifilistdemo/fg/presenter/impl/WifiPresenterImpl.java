package com.example.skywo.wifilistdemo.fg.presenter.impl;

import android.content.Context;
import android.net.wifi.ScanResult;

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

    @Override
    public void refreshConnectedWiFiInfo() {
        wifiView.refreshConnectedWiFiInfo();
    }

    @Override
    public List<ScanResult>  getWifiScanResult(Context context) {
        return wifiSession.getWifiScanResult(context);
    }
}