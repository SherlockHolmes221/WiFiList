package com.example.skywo.wifilistdemo.fg.presenter;

import android.content.Context;
import android.net.wifi.ScanResult;

import java.util.List;

public interface WifiPresenter {

    void refreshConnectedWiFiInfo();

    List<ScanResult> getWifiScanResult(Context context);
}
