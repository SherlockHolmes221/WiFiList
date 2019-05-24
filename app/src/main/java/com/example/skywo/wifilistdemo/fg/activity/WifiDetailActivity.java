package com.example.skywo.wifilistdemo.fg.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.example.skywo.wifilistdemo.R;

public class WifiDetailActivity extends AppCompatActivity {
    private static final String SSID = "ssid";
    private static final String LEVEL = "level";
    private static final String CAPABILITY = "capabilitity";

    private String ssid = "";
    private int level ;
    private String capabilitity;

    private TextView tvSSidName;
    private TextView tvLevel;
    private TextView tvCapabilitity;

    private static final String TAG = "WifiDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_wifi_detail);
        Intent intent = getIntent();
        ssid = intent.getStringExtra(SSID) == null ? "":intent.getStringExtra(SSID) ;
        level = intent.getIntExtra(LEVEL,0);
        capabilitity = intent.getStringExtra(CAPABILITY) == null ? "" : intent.getStringExtra(CAPABILITY) ;
        initUI();

    }

    private void initUI() {
        tvSSidName = findViewById(R.id.detail_wifi_name);
        tvLevel = findViewById(R.id.detail_wifi_level);
        tvCapabilitity = findViewById(R.id.detail_wifi_passsword);

        tvSSidName.setText(ssid);
        tvLevel.setText(String.valueOf(level));


        if (capabilitity.contains("WEP")) {
            tvCapabilitity.setText("WEP");
        }else if(capabilitity.contains("WPA") || capabilitity.contains("WPA2")){
            tvCapabilitity.setText("WPA/WPA2");
        }else if(capabilitity.contains("WPS")){
            tvCapabilitity.setText("WPS");
        }else
            tvCapabilitity.setText("无密码");
    }

    public static Intent jumpToDetailPage(Context context, String ssid, int level, String capability) {
        Intent intent = new Intent(context, WifiDetailActivity.class);
        Log.e(TAG, "jumpToDetailPage: " + ssid);
        intent.putExtra(SSID, ssid);
        intent.putExtra(LEVEL, level);
        intent.putExtra(CAPABILITY, capability);
        return intent;
    }

}
