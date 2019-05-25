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
     *
     * @param context
     * @param ssid  头部wifi的ssid
     * @return
     */
    @Override
    public void getSortScanResult(Context context,String ssid,List<WifiBean> wifiBeanList) {
        wifiBeanList.clear();
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
        wifiView.updateList();
    }

    /**
     * wifi没连接上
     *
     * @param connectedWifiItem
     * @param wifiBeanList
     */
    @Override
    public void disconnected(WifiBean connectedWifiItem, List<WifiBean> wifiBeanList) {
        if(connectedWifiItem != null) {
            wifiBeanList.add(new WifiBean(connectedWifiItem));
        }

        for (int i = 0; i < wifiBeanList.size(); i++) {//没连接上将 所有的连接状态都置为“未连接”
            wifiBeanList.get(i).setState(WifiBean.WIFI_STATE_DISCONNECT);
        }
        Collections.sort(wifiBeanList);
        wifiView.refreshConnectedWiFiInfo(connectedWifiItem);
        wifiView.updateList();
    }

    /**
     * wifi正在连接 or wifi已经连接
     *
     * @param context
     * @param connectedWifiItem
     * @param wifiBeanList
     */
    @Override
    public void connectingOrConnected(Context context,WifiBean connectedWifiItem,
                                      List<WifiBean> wifiBeanList,int connectType) {
        WifiInfo connectedWifiInfo = wifiSession.getConnectedWifiInfo(context);
        updateWifiInfo(connectedWifiInfo.getSSID(),connectType,connectedWifiItem,wifiBeanList);
        Log.e(TAG, connectedWifiInfo.getSSID());
    }

    @Override
    public void wifiListChange(Context context, WifiBean connectedWifiItem,
                               List<WifiBean> wifiBeanList,int connectType) {
        WifiInfo connectedWifiInfo = wifiSession.getConnectedWifiInfo(context);
        if (connectedWifiInfo != null) {
            updateWifiInfo(connectedWifiInfo.getSSID(), connectType,connectedWifiItem,wifiBeanList);
        }
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


    /**
     * wifi状态变化后，更新当前连接wifi信息和wifi列表
     *
     * @param wifiName
     * @param type
     * @param connectedWifiItem
     * @param wifiBeanList
     */
    public void updateWifiInfo(String wifiName, int type,
                               WifiBean connectedWifiItem,List<WifiBean> wifiBeanList) {
        int index = -1;
        if (wifiBeanList == null || wifiBeanList.size() == 0) {
            return;
        }

        //从已经连接到正在连接的状态,只需要更新头部信息
        if(connectedWifiItem != null &&
                ("\"" + connectedWifiItem.getWifiName() + "\"").equals(wifiName) &&
                connectedWifiItem.getState().equals(WifiBean.WIFI_STATE_CONNECTING) &&
                type == 1){
            connectedWifiItem.setState(WifiBean.WIFI_STATE_CONNECT);

            wifiView.refreshConnectedWiFiInfo(connectedWifiItem);
            return;
        }

        WifiBean connectedWifiItemTemp = new WifiBean();

        for (int i = 0; i < wifiBeanList.size(); i++) {
            wifiBeanList.get(i).setState(WifiBean.WIFI_STATE_DISCONNECT);

            WifiBean wifiBean = wifiBeanList.get(i);
            if (index == -1 && ("\"" + wifiBean.getWifiName() + "\"").equals(wifiName)) {

                index = i;
                int level = wifiBean.getLevel();
                connectedWifiItemTemp.setLevel(level);
                connectedWifiItemTemp.setWifiName(wifiBean.getWifiName());
                connectedWifiItemTemp.setLevelGrade(wifiSession.getLevelByGrade(level));
                connectedWifiItemTemp.setCapabilities(wifiBean.getCapabilities());
                if (type == 1) {
                    connectedWifiItemTemp.setState(WifiBean.WIFI_STATE_CONNECT);
                } else {
                    connectedWifiItemTemp.setState(WifiBean.WIFI_STATE_CONNECTING);
                }
            }
        }//for
        Log.e(TAG, "updateWifiInfo: "+index);
        if (index != -1) {
            wifiBeanList.remove(index);
            if(connectedWifiItem != null) {
                wifiBeanList.add(new WifiBean(connectedWifiItem));
                Collections.sort(wifiBeanList);
            }
            //更新头部item
            connectedWifiItem = connectedWifiItemTemp;
        }
        //更新头部wifi的UI
        Log.e(TAG, "updateWifiInfo: " );
        wifiView.refreshConnectedWiFiInfo(connectedWifiItem);
        wifiView.updateList();
    }

}