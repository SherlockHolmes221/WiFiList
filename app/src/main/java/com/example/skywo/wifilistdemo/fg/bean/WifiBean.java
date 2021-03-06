package com.example.skywo.wifilistdemo.fg.bean;

public class WifiBean implements Comparable<WifiBean> {
    public static final String WIFI_STATE_CONNECT = "已连接";
    public static final String WIFI_STATE_CONNECTING = "正在连接";
    public static final String WIFI_STATE_DISCONNECT = "未连接";


    private String wifiName;
    private int level;
    private String state;  //已连接  正在连接  未连接 三种状态
    private String capabilities;//加密方式
    private boolean isNeedPassword;
    private int levelGrade;

    public WifiBean(WifiBean wifiBean) {
        setCapabilities(wifiBean.getCapabilities());
        setWifiName(wifiBean.getWifiName());
        setLevel(wifiBean.getLevel());
        setState(wifiBean.getState());
        setNeedPassword(wifiBean.isNeedPassword);
        setLevelGrade(wifiBean.getLevelGrade());
    }

    public WifiBean() {
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isNeedPassword() {
        return isNeedPassword;
    }

    public void setNeedPassword(boolean needPassword) {
        isNeedPassword = needPassword;
    }

    public int getLevelGrade() {
        return levelGrade;
    }

    public void setLevelGrade(int levelGrade) {
        this.levelGrade = levelGrade;
    }

    @Override
    public int compareTo(WifiBean o) {
        boolean b1 = this.isNeedPassword;
        boolean b2 = o.isNeedPassword;
        if(b1 && !b2){
            return 1;
        }else if(!b1 && b2){
            return -1;
        }else {
            int level1 = this.getLevel();
            int level2 =
                    o.getLevel();
            return level1 - level2;
        }
    }

    @Override
    public String toString() {
        return "WifiBean{" +
                "wifiName='" + wifiName + '\'' +
                ", level='" + level + '\'' +
                ", state='" + state + '\'' +
                ", capabilities='" + capabilities + '\'' +
                ", isNeedPassword=" + isNeedPassword +
                ", levelGrade=" + levelGrade +
                '}';
    }
}
