### WiFi简易开发
#### 项目框架：
![frame](https://raw.githubusercontent.com/SherlockHolmes221/WiFiList/master/frame.jpg)
#### 权限
```aidl
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.WRITE_SECURE_SETTINGS" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```
安卓6.0以后要增加位置权限并动态申请

加密的类型
有线等效加密    WEP
WiFi访问保护   WPA
WiFi访问保护II WPA2

```aidl
WiFi的五种状态
WIFI_STATE_DISABLED    WLAN已经关闭
WIFI_STATE_DISABLING   WLAN正在关闭
WIFI_STATE_ENABLED     WLAN已经打开
WIFI_STATE_ENABLING    WLAN正在打开
WIFI_STATE_UNKNOWN     未知
```
#### WifiManager
```aidl
//获取系统的WifiManager
WifiManager wifimanager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
```

```
//是否开启了WiFi
List<ScanResult> result = wifimanager.isWifiEnabled();
```

```
//开启和关闭WiFi
wifimanager.setWifiEnabled(true);
wifimanager.setWifiEnabled(false);
```

```aidl
//扫描
wifimanager.startScan();
```

```aidl
//获取扫描的WiFi列表
List<ScanResult> result = wifimanager.getScanResults();
```

```
//连接的WiFi的信息
WifiInfo mWifiInfo = wifimanager.getConnectionInfo();
//mWifiInfo:
//SSID: Skyworth, BSSID: d8:15:0d:6c:20:48, Supplicant state: COMPLETED, RSSI: -54, Link speed: 72Mbps, Frequency: 2462MHz, Net ID: 27, Metered hint: false, score: 100
```

```
//获取已经保存过的/配置好的 WIFI 热点
List<WifiConfiguration> existingConfigs = wifimanager.getConfiguredNetworks();
```

```
//连接网络
wifimanager.enableNetwork(config.networkId, true);
```

```aidl
//添加网络
wifimanager.addNetwork(config);
```
```aidl
//禁用某个网络
wifimanager.disableNetwork(wifiinfo.getNetworkId());
```


```aidl
//断开连接
wifimanager.disconnect();
```


####
