package com.example.skywo.wifilistdemo.fg.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

import com.example.skywo.wifilistdemo.R;


public class WiFiSignalView extends android.support.v7.widget.AppCompatImageView {

    private static final String TAG = "WiFiSignalView";

    private static final int HEIGHT_NUMERATOR = 65;
    private static final int HEIGHT_DENOMINATOR = 90;
    private static final int HEIGHT_SIGNAL_1 = 10;
    private static final int HEIGHT_SIGNAL_2 = 20;
    private static final int HEIGHT_SIGNAL_3 = 34;
    private static final int HEIGHT_SIGNAL_4 = 49;

    private static final int FREE_COLOR = Color.parseColor("#00D196");
    private static final int NOT_FREE_COLOR = Color.parseColor("#2E2E2E");
    private static final int DANGER_COLOR = Color.parseColor("#F21600");
    private static final int BACKGROUND_COLOR = Color.parseColor("#F3F3F3");

    private int mSignalLevelHigh = HEIGHT_SIGNAL_1;
    private float mAnimationProgress = 0.0f;
    private boolean mIsAnimationType = false;
    private Paint mColorPaint;
    private Animation mAnimation;

    public WiFiSignalView(@NonNull Context context){
        super(context);
        initView();
    }

    public WiFiSignalView(@NonNull Context context, AttributeSet attributes) {
        super(context, attributes);
        initView();
    }

    public void setSignalLevel(int signalLevel){
        switch (signalLevel){
            case 2:
                mSignalLevelHigh = HEIGHT_SIGNAL_2;
                break;
            case 3:
                mSignalLevelHigh = HEIGHT_SIGNAL_3;
                break;
            case 4:
                mSignalLevelHigh = HEIGHT_SIGNAL_4;
                break;
            case 0:
            case 1:
            default:
                mSignalLevelHigh = HEIGHT_SIGNAL_1;
                break;
        }
        invalidate();
    }

    public void startOnceSignalAnimation(){
        mIsAnimationType = true;
        mAnimationProgress = 0.0f;
        mAnimation.setRepeatCount(0);
        mAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                clearAnimation();
                mIsAnimationType = false;
                invalidate();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        startAnimation(mAnimation);
        invalidate();
    }

    public void startSignalAnimation(){
        mIsAnimationType = true;
        mAnimationProgress = 0.0f;
        mAnimation.setRepeatCount(1000);
        mAnimation.setAnimationListener(null);
        startAnimation(mAnimation);
        invalidate();
    }

    public void stopSignalAnimation(){
        mIsAnimationType = false;
        clearAnimation();
        invalidate();
    }

    public void activeDanger(){
        mColorPaint.setColor(DANGER_COLOR);
        invalidate();
    }

    public void unActiveDanger(){
        mColorPaint.setColor(FREE_COLOR);
        invalidate();
    }

    public void setFree(){
        mColorPaint.setColor(FREE_COLOR);
        invalidate();
    }

    public void setNotFree(){
        mColorPaint.setColor(NOT_FREE_COLOR);
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas){
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        int drawWidth = width / 2;
        int drawHeight = HEIGHT_NUMERATOR * height / HEIGHT_DENOMINATOR ;
        Log.i(TAG, "onDraw mIsAnimationType=" + mIsAnimationType + " height=" + height + " drawHeight=" + drawHeight + " width=" + width
                + " drawWidth=" + drawWidth + " mAnimationProgress=" + mAnimationProgress + " mSignalLevelHigh=" + mSignalLevelHigh);
        if(mIsAnimationType){
            canvas.drawCircle(drawWidth, drawHeight, drawHeight * mAnimationProgress, mColorPaint);
        }else{
            int radius = mSignalLevelHigh * height / HEIGHT_DENOMINATOR;
            canvas.drawCircle(drawWidth, drawHeight, radius, mColorPaint);
        }
        super.onDraw(canvas);
    }

    private void initView(){
        mColorPaint = new Paint();
        mColorPaint.setAntiAlias(true);
        mColorPaint.setColor(FREE_COLOR);
        setScaleType(ImageView.ScaleType.FIT_XY);

        setImageDrawable(getContext().getResources().getDrawable(R.drawable.empty_wifi_signal_icon));

        mAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                Log.i(TAG, "applyTransformation interpolatedTime = " + interpolatedTime);
                mAnimationProgress = interpolatedTime;
                invalidate();
            }
        };
        mAnimation.setDuration(1000L);
        setBackgroundColor(BACKGROUND_COLOR);
    }
}

