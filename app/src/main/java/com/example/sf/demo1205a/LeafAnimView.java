package com.example.sf.demo1205a;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by sf on 2017/12/5 0005.
 */

public class LeafAnimView extends View {
    private static final String TAG = "LeafAnimView";


    public static final int DEF_DURATION = 5000;

    private ValueAnimator mValueAnimator;
    private Paint mPaint;
    private LeafAtom mLeafAtom;

    private AnimatorListenerAdapter mListenerAdapter = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            if (null != mLeafAtom) {
                mLeafAtom.endAndClear();
            }
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
            super.onAnimationRepeat(animation);
            invalidate();
        }
    };

    public LeafAnimView(Context context) {
        super(context);
        init(context);
    }

    public LeafAnimView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LeafAnimView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.GREEN);
        //???什么意思 硬件加速？？
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        //动画引擎
        mValueAnimator = ValueAnimator.ofFloat(0);
        mValueAnimator.setDuration(30);
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.addListener(mListenerAdapter);
    }

    /*public LeafAnimView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }*/

    public void start() {
        if (mLeafAtom != null) {
            mLeafAtom.start();
        }
    }

    public void pause() {
        if (mLeafAtom != null) {
            mLeafAtom.pause();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.e(TAG, "onDetachedFromWindow");
        if (null != mValueAnimator) {
            mValueAnimator.end();
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            mValueAnimator.start();
        } else if (visibility == INVISIBLE) {
            mValueAnimator.end();
        }

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.e(TAG, "onLayout  : " + System.currentTimeMillis());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e(TAG, "onSizeChanged  : ");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (null == mLeafAtom) {
            //传入默认总时长
            mLeafAtom = new LeafAtom(getWidth(), getHeight(), DEF_DURATION);
        }

        if (!mValueAnimator.isStarted()) {
            //开始准备画
            mValueAnimator.start();
        }

        //开始画
        mLeafAtom.drawGraph(canvas, mPaint);
    }

    public void setTotalDuration(long totalDuration) {
        if (null == mLeafAtom) {
            mLeafAtom = new LeafAtom(getWidth(), getHeight(), totalDuration);
            return;
        }
        mLeafAtom.setTotalDuration(totalDuration);
    }
}
