package com.example.skywo.wifilistdemo.fg.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.skywo.wifilistdemo.fg.adapter.WifiListAdapter;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;

public class WiFiListView extends ListView implements AbsListView.OnScrollListener {

    private static final String TAG = "WiFiListView";

    private static final int MSG_SCORLL = 2;

    private int mRefreshViewHeight;
    private View mRefreshView;
    private PushDownListener mPushDownListener;
//    private ListViewImageLoader mListViewImageLoader;
    private WifiListAdapter mQListAdapter;
    private boolean mIsEnablePerformanceModel;
    private OnScrollListener mOutOnScrollListenerList;

    private ElasticityScrollerListener mElasticityScrollListener;
    private GestureDetector mGestureDetector;

    private boolean mElasticityScrolled;

    public WiFiListView(Context context) {
        super(context);
        initStyle(context);
    }

    public WiFiListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initStyle(context);
    }

    /**
     * 定义列表样式
     *
     * @param context
     */
    protected void initStyle(Context context) {
        setDividerHeight(0);
        setCacheColorHint(0X00000000);
        setSelector(android.R.color.transparent);
        setOnScrollListener(this);
        mGestureDetector = new GestureDetector(new ScrollGestureListener());
        mInterpolator = new DecelerateInterpolator();

//        try {
//            Field f = View.class.getDeclaredField("mScrollCache");
//            f.setAccessible(true);
//            Object scrollabilityCache = f.get(this);
//            f = f.getType().getDeclaredField("scrollBar");
//            f.setAccessible(true);
//            Object scrollBarDrawable = f.get(scrollabilityCache);
//            f = f.getType().getDeclaredField("mVerticalThumb");
//            f.setAccessible(true);
//            Drawable drawable = (Drawable) f.get(scrollBarDrawable);
//            drawable = UIConfig
//                    .getUILibDrawable(context, R.drawable.scroll_bar);
//            f.set(scrollBarDrawable, drawable);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        if(l != this){
            mOutOnScrollListenerList = l;
        }else{
            super.setOnScrollListener(l);
        }
    }

    private void handleRelease () {
        mIsScrolling = false;
        mScrollStartTime = 0;
        mFromScrollDistance = getScrollY();
        mToScrollDistance = 0;

        // 如果刷新View出现了，则需要额外处理
        if (!mIsPushShowHeaderView) {
            return;
        }

        mIsPushShowHeaderView = false;
        if (getVisualScrollY() <= -mRefreshViewHeight) {
            // 让它等待刷新完成
            mToScrollDistance = 0;
            mIsStaticShowHeaderView = true;
        } else {
            // 让它隐藏
            mToScrollDistance = mRefreshViewHeight;
            mIsStaticShowHeaderView = false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mEnableElasticityScroll && mGestureDetector.onTouchEvent(ev)) {
            return true;
        }
        if (mIsScrolling) {
            return true;
        }
        return safeSuperonInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int actionMasked = ev.getActionMasked();
        if (mElasticityScrolled && (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL)) {
            Log.i(TAG, "补充遗失的idle事件");
            onScrollStateChanged(this, SCROLL_STATE_IDLE);
            mElasticityScrolled = false;
        }

        if (mEnableElasticityScroll && mGestureDetector.onTouchEvent(ev) ) {
            return true;
        }

        if (ev.getPointerCount() > 1) {
            return false;
        }

        int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action != MotionEvent.ACTION_CANCEL && action != MotionEvent.ACTION_UP) {
            if (mIsScrolling) {
                Log.i(TAG, "发现弹性下拉中，不补充ACTION_CANCEL");
                // ev.setAction(MotionEvent.ACTION_CANCEL);
                return true;
            }
            return safeSuperonTouchEvent(ev);
        }

        if (!mIsScrolling) {
            return safeSuperonTouchEvent(ev);
        }

        //ev.setAction(MotionEvent.ACTION_CANCEL);
        //safeSuperonTouchEvent(ev);

        loadCurrentScreenItemIcon();

        handleRelease();

        Message msg = mHandler.obtainMessage();
        msg.what = MSG_SCORLL;
        mHandler.sendMessage(msg);

        return true;
    }

    private boolean safeSuperonTouchEvent(MotionEvent ev) {
//        if (UIConfig.isDebuggable()) {
//            return super.onTouchEvent(ev);
//        }
        boolean handled = false;
        try {
            handled = super.onTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handled;
    }

    private boolean safeSuperonInterceptTouchEvent(MotionEvent ev) {
//        if (UIConfig.isDebuggable()) {
//            return super.onInterceptTouchEvent(ev);
//        }
        boolean handled = false;
        try {
            handled = super.onInterceptTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handled;
    }

    /**
     * 返回是否开启了弹性滑动
     * @return
     */
    public boolean isEnableElasticityScroll() {
        return mEnableElasticityScroll;
    }

    /**
     * 设置是开开启弹性滑动
     * @param enableElasticityScroll
     */
    public void setEnableElasticityScroll(boolean enableElasticityScroll) {
        this.mEnableElasticityScroll = enableElasticityScroll;
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        mQListAdapter = (WifiListAdapter)adapter;
//        mQListAdapter.setListView(this);
//        if(mIsEnablePerformanceModel){
//            mListViewImageLoader = new ListViewImageLoader();
//            mQListAdapter.setListViewImageLoader(this, mListViewImageLoader);
//        }
        super.setAdapter(adapter);
    }
//
//    /**
//     * 销毁listview的资源
//     */
//    public void onDestroy(){
//        if(mListViewImageLoader != null){
//            mListViewImageLoader.onServiceStop();
//            mListViewImageLoader = null;
//        }
//    }

    /**
     * 设置激活性能模式，（激活性能模式之后需要BaseItemModel实现ImageModel数据实体）
     * @return
     */
    public void setIsEnablePerformanceModel(boolean isEnablePerformanceModel){
        this.mIsEnablePerformanceModel = isEnablePerformanceModel;
    }

    /**
     * 设置下拉刷新的视图VIEW
     * @param refreshView
     */
    public void setDownPushRefresh(View refreshView){
        if(refreshView == null){
            return;
        }
        mRefreshView  = refreshView;
        measureView(refreshView);
        mRefreshViewHeight = refreshView.getMeasuredHeight();
        //开始设置不可见
        refreshView.setPadding(0, -mRefreshViewHeight, 0, 0);
        refreshView.invalidate();
    }

    // 此方法直接照搬自网络上的一个下拉刷新的demo，此处是“估计”headView的width以及height
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_SCORLL:
                    long now = System.currentTimeMillis();
                    if (mScrollStartTime == 0) {
                        mScrollStartTime = now;
                    }

                    float f = (now - mScrollStartTime) / 200.0f;
                    if (f < 0.0f) {
                        f = 0.0f;
                    } else if (f > 1.0f) {
                        f = 1.0f;
                    }
                    f = mInterpolator.getInterpolation(f);
                    int scrollY = 0;
                    if (f <= 0.0f) {
                        scrollY = mFromScrollDistance;
                    } else if (f >= 1.0f) {
                        scrollY = mToScrollDistance;
                    } else {
                        scrollY = (int) (mFromScrollDistance +
                                (mToScrollDistance - mFromScrollDistance) * f);
                    }

                    scrollTo(0, scrollY);

                    notifyElasticityScroll(true);

                    if (scrollY != mToScrollDistance) {
                        mHandler.obtainMessage(MSG_SCORLL).sendToTarget();
                    } else {
                        if (mIsTopScroll) {
                            setSelection(0);
                        } else if(mIsBottomScroll) {
                            setSelection(getCount() - 1);
                        }
                        resetScrollState(!mIsStaticShowHeaderView);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 让下拉刷新的headerview消失
     */
    public void dismissPushDownRefreshView() {
        if (getFirstVisiblePosition() > 0) {
            resetScrollState(true);
            return;
        }
        mIsStaticShowHeaderView = false;
        mFromScrollDistance = getScrollY();
        mToScrollDistance = mRefreshViewHeight;
        mHandler.obtainMessage(MSG_SCORLL).sendToTarget();
    }

    public void pullToRefresh() {
        mIsTopScroll = true;
        handlePullToRefresh();
        doScorll(-mRefreshViewHeight * 2);
        handleRelease();
        Message msg = mHandler.obtainMessage();
        msg.what = MSG_SCORLL;
        mHandler.sendMessage(msg);
    }

    /**
     * 设置弹性滑动监听者
     * @param elasticityScrollerListener
     */
    public void setElasticityScrollerListener(ElasticityScrollerListener elasticityScrollerListener){
        mElasticityScrollListener = elasticityScrollerListener;
    }

    protected void setPushDownListener(PushDownListener pushDownListener) {
        mPushDownListener = pushDownListener;
    }

    public interface ElasticityScrollerListener{

        /**
         * 回调弹性滑动的距离
         * @param scrollDis
         *        弹性滑动距离值
         * @param isTopPush
         *        是否在顶端弹性滑动   否则就是在低端
         * @param isTopPushDown
         *        是否向下滑动      否则就是向上滑动
         * @param pushDownViewH
         *        下拉刷新的view的视图高度
         */
        public void onElasticityScroll(int scrollDis, boolean isTopPush, boolean isTopPushDown, int pushDownViewH);

    }

    protected interface PushDownListener{

        /**
         * 下拉刷新出现
         */
        public void onPushDownAppear();

        /**
         * 下拉刷新消失
         */
        public void onPushDownDismiss();

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch(scrollState){
            case SCROLL_STATE_IDLE:
                if (mQListAdapter != null) {
                    Log.e(TAG, "onScrollStateChanged: "+SCROLL_STATE_IDLE);
                    //mQListAdapter.setIsFling(false);
                    loadCurrentScreenItemIcon();
                }
                break;
            case SCROLL_STATE_TOUCH_SCROLL:
                if (mQListAdapter != null) {
                    Log.e(TAG, "onScrollStateChanged: "+SCROLL_STATE_TOUCH_SCROLL);
                    //mQListAdapter.setIsScorllOnec(true);
                }
                break;
            case SCROLL_STATE_FLING:
                if (mQListAdapter != null) {
                    Log.e(TAG, "onScrollStateChanged: "+SCROLL_STATE_FLING);
                    //mQListAdapter.setIsFling(true);
                    //mQListAdapter.setIsScorllOnec(true);
                }
                break;
        }
        if(mOutOnScrollListenerList != null){
            mOutOnScrollListenerList.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if(mOutOnScrollListenerList != null){
            mOutOnScrollListenerList.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    private void loadCurrentScreenItemIcon(){
        int firstPos = getFirstVisiblePosition() - getHeaderViewsCount();
        int lastPos = getLastVisiblePosition() - getHeaderViewsCount();

        int adjust = 0;
        if (firstPos < 0) {
            adjust = -firstPos;
            firstPos = 0;
        }

//        if (lastPos >= mQListAdapter.getData().size()) {
//            lastPos = mQListAdapter.getData().size() - 1;
//        }
//
//        for (int i = firstPos; i <= lastPos; i++) {
//            ImageView iconView = null;
//            View item = getChildAt(adjust + i - firstPos);
//            if (item != null && item instanceof IUpdateableIconItem) {
//                iconView = ((IUpdateableIconItem) item).getIconView();
//            }
//
//            BaseItemModel model = mQListAdapter.getData().get(i);
//            ImageModel imageModel = model.getImageModel();
//            if (imageModel != null) {
//                if (imageModel.hasImage() && iconView != null) {
//                    mQListAdapter.asyncLoadImage(imageModel, iconView, false);
//                    mQListAdapter.preLoad();
//                } else if (mListViewImageLoader != null) {
//                    SoftReference<Drawable> ref = imageModel.getIcon();
//                    if (ref == null || ref.get() == null) {
//                        mListViewImageLoader.addTask(model);
//                    }
//                }
//            }
//        }
    }

    /**是否激活了弹性滑动**/
    private boolean mEnableElasticityScroll = true;

    private boolean mIsScrolling;
    private boolean mIsTopScroll;
    private boolean mIsBottomScroll;

    private boolean mIsStaticShowHeaderView;
    private boolean mIsPushShowHeaderView;

    private long mScrollStartTime;
    private int mFromScrollDistance;
    private int mToScrollDistance;

    private Interpolator mInterpolator;

    private void doScorll(float distanceY) {
        if (!mIsScrolling) {
            mIsScrolling = true;
            mElasticityScrolled = true;
        }

        // 给下拉刷新加点阻力
        distanceY *= 0.6f;
        int oldScrollY = getScrollY();
        int newScrollY = oldScrollY + (int) distanceY;
        if (oldScrollY > 0 && newScrollY < 0) {
            newScrollY = 0;
        } else if (oldScrollY < 0 && newScrollY > 0) {
            newScrollY = 0;
        }
        scrollTo(0, newScrollY);

        notifyElasticityScroll(false);
    }

    private void handlePullToRefresh() {
        if (mRefreshView == null) {
            return;
        }

        if (mIsStaticShowHeaderView) {
            return;
        }

        if (mIsPushShowHeaderView) {
            return;
        }

        mIsPushShowHeaderView = true;
        // a trick，将下拉刷新view置为可见
        mRefreshView.setPadding(0, 0, 0, 0);
        // 可见之后，先滑动到屏幕之外
        scrollBy(0, mRefreshViewHeight);

        if (mPushDownListener != null) {
            mPushDownListener.onPushDownAppear();
        }
    }

    private void resetScrollState(boolean dismissRefresh) {
        mIsTopScroll = false;
        mIsBottomScroll = false;
        mIsScrolling = false;

        mIsStaticShowHeaderView = false;
        mIsPushShowHeaderView = false;

        mFromScrollDistance = 0;
        mToScrollDistance = 0;

        mScrollStartTime = 0;

        if (dismissRefresh && mRefreshView != null) {
            if (mRefreshView.getPaddingTop() == 0) {
                // 如果刷新view原先是可见的，现在设置为不可见
                mRefreshView.setPadding(0, -mRefreshViewHeight, 0, 0);
                if (getScrollY() != 0) {
                    // 这里的逻辑是：如果scrollY为0，隐藏刷新view，列表自动恢复正常
                    // 如果scrollY不为0，隐藏刷新view，则需向下滑动以调整
                    scrollBy(0, -mRefreshViewHeight);
                }

                if(mPushDownListener != null){
                    mPushDownListener.onPushDownDismiss();
                }
            }
        }
    }

    private int getVisualScrollY() {
        int scrollDistance = getScrollY();
        if (mRefreshView != null && mRefreshView.getPaddingTop() == 0) {
            // 下拉刷新在外部并不是当作header view来看的，因此这里需要减去它的高度
            scrollDistance -= mRefreshViewHeight;
        }
        return scrollDistance;
    }

    private void notifyElasticityScroll(boolean flip) {
        if (mElasticityScrollListener != null) {
            int scrollDistance = getVisualScrollY();
            boolean isTopPushDown = flip ? scrollDistance > 0 : scrollDistance < 0;
            mElasticityScrollListener.onElasticityScroll(
                    scrollDistance, mIsTopScroll,
                    isTopPushDown, mRefreshViewHeight);
        }
    }

    private class ScrollGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.i(TAG, "onScroll mIsScrolling=" + mIsScrolling);
            // 暂时不处理多点触摸的情况
            if ((e1 != null && e1.getPointerCount() > 1) ||
                    (e2 != null && e2.getPointerCount() > 1))
            {
                return false;
            }

            int firstPos = getFirstVisiblePosition();
            int lastPos = getLastVisiblePosition();

            if (isOverTheFirstItem(firstPos, lastPos)) {
                Log.i(TAG, "onScroll isOverTheFirstItem");
                if (!mIsScrolling && distanceY < 0) {
                    // 如果还没开始，并且是向下滑动，则开始滑动
                    mIsTopScroll = true;
                    handlePullToRefresh();
                    doScorll(distanceY);
                    return true;
                } else if (mIsScrolling) {
                    // 如果已经开始，不管是向上还是向下，随手指上下动即可
                    doScorll(distanceY);
                    return true;
                }
            } else if(isOverTheLastItem(firstPos, lastPos)) {
                Log.i(TAG, "onScroll isOverTheLastItem");
                if (!mIsScrolling && distanceY > 0) {
                    // 如果还没开始，并且是向上滑动，则开始滑动
                    mIsBottomScroll = true;
                    doScorll(distanceY);
                    return true;
                } else if (mIsScrolling) {
                    // 如果已经开始，不管是向上还是向下，随手指上下动即可
                    doScorll(distanceY);
                    return true;
                }
            }
            Log.i(TAG, "onScroll resetScrollState");
            resetScrollState(false);
            return false;
        }

        private boolean isOverTheFirstItem(int firstPos, int lastPos) {
            if (firstPos != 0) {
                return false;
            }
            View firstItemView = getChildAt(0);
            if (firstItemView == null) {
                return false;
            }
            int visualScrollY = getScrollY();
            if (mRefreshView != null) {
                visualScrollY = visualScrollY - mRefreshViewHeight - mRefreshView.getPaddingTop();
            }
            if (firstItemView.getTop() - visualScrollY < 0) {
                return false;
            }
            if (mIsScrolling && firstItemView.getTop() - visualScrollY == 0) {
                return false;
            }
            return true;
        }

        private boolean isOverTheLastItem(int firstPos, int lastPos) {
            int itemCount = getCount();
            if (lastPos != itemCount - 1) {
                return false;
            }
            View lastItemView = getChildAt(getChildCount() - 1);
            if (lastItemView == null) {
                return false;
            }
            boolean overLast = lastItemView.getBottom() - getScrollY() > getBottom() - getTop();
            if (overLast) {
                return false;
            }
            boolean justLast = lastItemView.getBottom() - getScrollY() == getBottom() - getTop();
            if (mIsScrolling && justLast) {
                return false;
            }
            return true;
        }
    }

//    @Override
//    public boolean post(final Runnable action) {
//        Runnable runnable  = UILibSMUtil.getPostRunnable(action, this);
//        return super.post(runnable);
//    }

    private boolean mIsDisableChildrenDrawingCache = true;

    public void setDisableChildrenDrawingCache(boolean disable) {
        mIsDisableChildrenDrawingCache = disable;
    }

    @Override
    protected void setChildrenDrawingCacheEnabled(boolean enabled) {
        if (mIsDisableChildrenDrawingCache) {
            super.setChildrenDrawingCacheEnabled(false);
        } else {
            super.setChildrenDrawingCacheEnabled(enabled);
        }
    }

    @Override
    protected void setChildrenDrawingOrderEnabled(boolean enabled) {
        if (mIsDisableChildrenDrawingCache) {
            super.setChildrenDrawingOrderEnabled(false);
        } else {
            super.setChildrenDrawingOrderEnabled(enabled);
        }
    }

    @Override
    protected void setChildrenDrawnWithCacheEnabled(boolean enabled) {
        if (mIsDisableChildrenDrawingCache) {
            super.setChildrenDrawnWithCacheEnabled(false);
        } else {
            super.setChildrenDrawnWithCacheEnabled(enabled);
        }
    }
}
