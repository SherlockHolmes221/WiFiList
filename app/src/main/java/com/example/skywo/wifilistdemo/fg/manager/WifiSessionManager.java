package com.example.skywo.wifilistdemo.fg.manager;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.example.skywo.wifilistdemo.fg.bean.WifiBean;

import java.util.ArrayList;
import java.util.List;

public class WifiSessionManager {
    private static final String TAG = "WifiSessionManager";

    public enum WifiCipherType {
        WIFICIPHER_WEP,
        WIFICIPHER_WPA,
        WIFICIPHER_NOPASS, ////无需密码
        WIFICIPHER_INVALID
    }

    public WifiSessionManager() {
    }

    public static List<ScanResult> getWifiScanResult(Context context) {
        boolean b = context == null;
        return ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getScanResults();
    }

    public static boolean isWifiEnable(Context context) {
        return ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).isWifiEnabled();
    }

    public static WifiInfo getConnectedWifiInfo(Context context) {
        return ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
    }

    public static List getConfigurations(Context context) {
        return ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConfiguredNetworks();
    }


    public static WifiConfiguration createWifiConfig(String SSID, String password, WifiCipherType type) {

        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        if (type == WifiCipherType.WIFICIPHER_NOPASS) {
//            config.wepKeys[0] = "";  //注意这里
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            config.wepTxKeyIndex = 0;
        }

        if (type == WifiCipherType.WIFICIPHER_WEP) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }

        if (type == WifiCipherType.WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;

        }

        return config;

    }

    /**
     * 接入某个wifi热点
     */
    public static boolean addNetWork(WifiConfiguration config, Context context) {

        WifiManager wifimanager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

        WifiInfo wifiinfo = wifimanager.getConnectionInfo();

        if (null != wifiinfo) {
            wifimanager.disableNetwork(wifiinfo.getNetworkId());
        }

        boolean result = false;

        if (config.networkId > 0) {
            result = wifimanager.enableNetwork(config.networkId, true);
            wifimanager.updateNetwork(config);
        } else {

            int i = wifimanager.addNetwork(config);
            result = false;

            if (i > 0) {

                wifimanager.saveConfiguration();
                return wifimanager.enableNetwork(i, true);
            }
        }

        return result;

    }

    /**
     * 判断wifi热点支持的加密方式
     */
    public static WifiCipherType getWifiCipher(String s) {

        if (s.isEmpty()) {
            return WifiCipherType.WIFICIPHER_INVALID;
        } else if (s.contains("WEP")) {
            return WifiCipherType.WIFICIPHER_WEP;
        } else if (s.contains("WPA") || s.contains("WPA2") || s.contains("WPS")) {
            return WifiCipherType.WIFICIPHER_WPA;
        } else {
            return WifiCipherType.WIFICIPHER_NOPASS;
        }
    }

    //查看以前是否也配置过这个网络
    public static WifiConfiguration isExsits(String SSID, Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> existingConfigs = wifimanager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    /**
     * 注意:
     * Android 的 WiFi 连接, 大概可以分为如下两种情况:
     * a. 无密码的, 可直接连接, 连接过程中, 此热点一直有, 不管最后是否需要其他方式进行验证操作, 但凡连接成功, 即
     * 刻进行了对此热点的配置进行保存;
     * b. 有密码的, 暂且不论何种加密手段, 只要用户输入密码, 点击连接, 如果连接途中, 此热点一直有, 不论连接成功还
     * 是失败, 都即刻对此热点的配置进行了保存操作;  使用上述的方式获取到的WiFi的配置, 就是上面进行操作保存的WiFi配
     * 置;
     * c. 连接多个WiFi成功之后, 然后关闭WiFi, 下次开启WiFi的时候, 驱动会主动帮你连接这其中配置好的其中一个WiFi;
     * @param context
     */

    // 打开WIFI
    public static void openWifi(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifimanager.isWifiEnabled()) {
            wifimanager.setWifiEnabled(true);
        }
    }

    // 关闭WIFI
    public static void closeWifi(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifimanager.isWifiEnabled()) {
            wifimanager.setWifiEnabled(false);
        }
    }

    public static boolean isOpenWifi(Context context){
        WifiManager wifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean b = wifimanager.isWifiEnabled();
        return b;
    }

    /**
     * 将idAddress转化成string类型的Id字符串
     *
     * @param idString
     * @return
     */
    public static String getStringId(int idString) {
        StringBuffer sb = new StringBuffer();
        int b = (idString >> 0) & 0xff;
        sb.append(b + ".");
        b = (idString >> 8) & 0xff;
        sb.append(b + ".");
        b = (idString >> 16) & 0xff;
        sb.append(b + ".");
        b = (idString >> 24) & 0xff;
        sb.append(b);
        return sb.toString();
    }

    /**
     * 设置安全性
     *
     * @param capabilities
     * @return
     */
    public static String getCapabilitiesString(String capabilities) {
        if (capabilities.contains("WEP")) {
            return "WEP";
        } else if (capabilities.contains("WPA") || capabilities.contains("WPA2") || capabilities.contains("WPS")) {
            return "WPA/WPA2";
        } else {
            return "OPEN";
        }
    }

    public static boolean getIsWifiEnabled(Context context) {
        WifiManager wifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifimanager.isWifiEnabled();
    }

    public static void getReplace(Context context, List<WifiBean> list) {
        WifiInfo wifi = WifiSessionManager.getConnectedWifiInfo(context);
        List<WifiBean> listCopy = new ArrayList<>();
        listCopy.addAll(list);
        for (int i = 0; i < list.size(); i++) {
            if (("\"" + list.get(i).getWifiName() + "\"").equals(wifi.getSSID())) {
                listCopy.add(0, list.get(i));
                listCopy.remove(i + 1);
                listCopy.get(0).setState("已连接");
            }
        }
        list.clear();
        list.addAll(listCopy);
    }
    /**
     * 去除同名WIFI
     *
     * @param oldSr 需要去除同名的列表
     * @return 返回不包含同命的列表
     */
    public static List<ScanResult> noSameName(List<ScanResult> oldSr) {
        List<ScanResult> newSr = new ArrayList<ScanResult>();
        for (ScanResult result : oldSr) {
            if (!TextUtils.isEmpty(result.SSID) && !containName(newSr, result.SSID))
                newSr.add(result);
        }
        return newSr;
    }

    /**
     * 判断一个扫描结果中，是否包含了某个名称的WIFI
     * @param sr 扫描结果
     * @param name 要查询的名称
     * @return 返回true表示包含了该名称的WIFI，返回false表示不包含
     */
    public static boolean containName(List<ScanResult> sr, String name) {
        for (ScanResult result : sr) {
            if (!TextUtils.isEmpty(result.SSID) && result.SSID.equals(name))
                return true;
        }
        return false;
    }

    /**
     * 返回level 等级
     */
    public static int getLevel(int level){
        if (Math.abs(level) < 50) {
            return 1;
        } else if (Math.abs(level) < 75) {
            return 2;
        } else if (Math.abs(level) < 90) {
            return 3;
        } else {
            return 4;
        }
    }

    public static int getLevelByGrade(int level) {
        if(level < -85)
            return 1;
        else if(level < -70)
            return 2;
        else if(level < -55)
            return 3;

        else return 4;
    }


    // 开始扫描 WIFI.
    public static void startScanWifi(WifiManager manager) {
        if (manager != null) {
            manager.startScan();
        }
    }

    // 获取 WIFI 的状态.
    public static int getWifiState(WifiManager manager) {
        return manager == null ? WifiManager.WIFI_STATE_UNKNOWN : manager.getWifiState();
    }

    //断开连接
    public static void disconnect(Context context){
        WifiManager wifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifimanager.disconnect();
    }

/**
 * 注意:
 * WiFi 的状态目前有五种, 分别是:
 * WifiManager.WIFI_STATE_ENABLING: WiFi正要开启的状态, 是 Enabled 和 Disabled 的临界状态;
 *  WifiManager.WIFI_STATE_ENABLED: WiFi已经完全开启的状态;
 *  WifiManager.WIFI_STATE_DISABLING: WiFi正要关闭的状态, 是 Disabled 和 Enabled 的临界状态;
 *  WifiManager.WIFI_STATE_DISABLED: WiFi已经完全关闭的状态;
 *  WifiManager.WIFI_STATE_UNKNOWN: WiFi未知的状态, WiFi开启, 关闭过程中出现异常, 或是厂家未配备WiFi外挂模块会出现的情况;
 */
}
