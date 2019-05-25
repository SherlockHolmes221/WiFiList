package com.example.skywo.wifilistdemo.fg.presenter.impl;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.util.Log;

import com.example.skywo.wifilistdemo.fg.activity.WifiView;
import com.example.skywo.wifilistdemo.fg.bean.WifiBean;
import com.example.skywo.wifilistdemo.fg.model.WifiSession;
import com.example.skywo.wifilistdemo.fg.model.impl.WifiSessionManager;
import com.example.skywo.wifilistdemo.fg.presenter.WifiPresenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WifiPresenterImpl implements WifiPresenter {
    private static final String TAG = "WifiPresenterImpl";

    private WifiView wifiView;
    private WifiSession wifiSession;

    public WifiPresenterImpl(WifiView wifiView) {
        this.wifiView = wifiView;
        wifiSession = new WifiSessionManager();
    }

    public WifiPresenterImpl() {
        wifiSession = new WifiSessionManager();
    }

    /**
     * 获取wifi列表然后将bean转成自己定义的WifiBean
     * 不改变头部wifi状态
     * @param context
     * @param ssid  头部wifi的ssid
     * @return
     */
    @Override
    public void getSortScanResult(Context context,String ssid,List<WifiBean> wifiBeanList) {
        List<ScanResult> scanResults = wifiSession.getWifiScanResult(context);
        Log.e(TAG, "getSortScanResult: "+scanResults.size() );

        if (scanResults != null && !scanResults.isEmpty()) {
            for (int i = 0; i < scanResults.size(); i++) {
                if(ssid != null && ssid.equals(scanResults.get(i).SSID)){
                    continue;
                }
                WifiBean wifiBean = new WifiBean();

                //获取SSID
                wifiBean.setWifiName(scanResults.get(i).SSID);
                //只要获取都假设设置成未连接，真正的状态都通过广播来确定
                wifiBean.setState(WifiBean.WIFI_STATE_DISCONNECT);

                String capabilities = scanResults.get(i).capabilities;
                wifiBean.setCapabilities(capabilities);

                //是否加密wifi
                if (wifiSession.getWifiCipher(capabilities) == WifiSessionManager.WifiCipherType.WIFICIPHER_NOPASS) {
                    wifiBean.setNeedPassword(false);
                } else
                    wifiBean.setNeedPassword(true);

                //信号强度
                int level =scanResults.get(i).level;
                wifiBean.setLevel(level);
                //Log.e(TAG, "getAndSortScaResult: "+level );
                //level等级
                wifiBean.setLevelGrade(wifiSession.getLevelByGrade(level));

                wifiBeanList.add(wifiBean);
            }//for
            //排序
            Collections.sort(wifiBeanList);
        }
    }


    @Override
    public void refreshConnectedWiFiInfo() {
        wifiView.refreshConnectedWiFiInfo();
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