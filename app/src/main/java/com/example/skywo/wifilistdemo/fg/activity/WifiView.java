package com.example.skywo.wifilistdemo.fg.activity;

import com.example.skywo.wifilistdemo.fg.bean.WifiBean;

public interface WifiView {
    void refreshConnectedWiFiInfo(WifiBean connectedWifiItem);

    void updateList();
}
