package com.example.skywo.wifilistdemo.fg.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.skywo.wifilistdemo.R;

public class WifiFrameLayout extends FrameLayout {
	WifiSignalView mSignalIcon = null;
	ImageView mSignalMarkIcon = null;
	private Context context;

	public WifiFrameLayout(Context context) {
		super(context);
		this.context = context;
		initUi();
	}

	public WifiFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initUi();
	}

	void initUi() {
		int size = dip2px(context, 26);
		int viewWidth = size;
		int viewHeight = size;
		if(getLayoutParams() != null){
			int width = getLayoutParams().width;
			int height = getLayoutParams().height;
			if(width > 0 && height > 0){
				viewWidth = width;
				viewHeight = height;
			}
		}
		mSignalIcon = new WifiSignalView(context);
		mSignalIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
		mSignalMarkIcon = new ImageView(context);
		mSignalMarkIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(viewWidth, viewHeight);
		addView(mSignalIcon, lp);
		addView(mSignalMarkIcon, lp);
	}

	public void update(int signalLevel,boolean needEnterPsk) {

		Log.i("SessionHeadView", "update signalLevel = " + signalLevel);
		mSignalIcon.setSignalLevel(signalLevel);

		if(!needEnterPsk){
			mSignalIcon.setFree();
			mSignalMarkIcon.setVisibility(View.INVISIBLE);
			//mSignalMarkIcon.setImageDrawable(getIconBySignalMark(false));
		}else {
			mSignalIcon.setNotFree();
			mSignalMarkIcon.setVisibility(View.VISIBLE);
			mSignalMarkIcon.setImageDrawable(getIconBySignalMark(true));
		}
	}
	
	private Drawable getIconBySignalMark(boolean isFree) {
		if(isFree){
			return getContext().getResources().getDrawable(R.drawable.wifi_icon_list_signal_key);
		}else{
			return getContext().getResources().getDrawable(R.drawable.wifi_icon_list_signal_lock);
		}
	}


	private int dip2px(Context context, float dipValue) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int)(dipValue * scale + 0.5F);
	}

}
