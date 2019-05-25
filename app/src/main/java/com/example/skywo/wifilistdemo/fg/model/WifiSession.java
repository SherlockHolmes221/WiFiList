package com.example.skywo.wifilistdemo.fg.model;

import android.content.Context;
import android.net.wifi.ScanResult;

import java.util.List;

public interface WifiSession {
    List<ScanResult>  getWifiScanResult(Context context);
}
