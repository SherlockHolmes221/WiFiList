package com.example.skywo.wifilistdemo.fg.base;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;



public class BaseActivity extends AppCompatActivity{
    //权限请求码
    protected static final int PERMISSION_REQUEST_CODE = 0;

    /**
     * 检查是否已经授予权限
     *
     * @return
     */
    protected boolean checkPermission(String[] permisions) {
        for (String permission : permisions) {
            if (ActivityCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 申请权限
     */
    protected void requestPermission(String[] permisions) {
        ActivityCompat.requestPermissions(this,
                permisions, PERMISSION_REQUEST_CODE);
    }


    protected void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
