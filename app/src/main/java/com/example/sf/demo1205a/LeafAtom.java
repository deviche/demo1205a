package com.example.sf.demo1205a;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Build;
import android.util.Log;
import android.view.animation.LinearInterpolator;

/**
 * Created by sf on 2017/12/5 0005.
 */

public class LeafAtom {
    public static final float PETIOLE_RATIO = 0.1f;
    public static final String TAG = "LeafAtom ";
    public static final int EXPERIENCE_OFFFSET = 25;
    private float mX;
    private float mY;
    private float mWidth;
    private float mHeight;
    private PointF mBezierBottom;
    private PointF mBezierControl;
    private PointF mBezierTop;

    private long mPetioleTime;//叶柄动画时间
    private long mArcTime;//左右轮廓弧线的时间
    private long mLastLineTime;// 最后一段弧线的时间

    private float mVeinBottomY;// 叶脉最低点的Y坐标
    private float mOneNodeY;// 第一个分叉点y坐标
    private float mTwoNodeY;//第二个分叉点坐标

    private Path mMainPath;

    private ValueAnimator mPetioleAnim;
    private ValueAnimator mArcAnim;
    private ValueAnimator mArcRightAnim;
    private ValueAnimator mLastAnim;

    private AnimatorSet mEngine;//集合动画，发动引擎
    private Path mOneLpath;
    private Path mOneRpath;
    private Path mTwoLpath;
    private Path mTwoRpath;

    public LeafAtom(int width, int height, long totalDuration) {
        mWidth = width;
        mHeight = height;
        setStepTime(totalDuration);

        mBezierBottom = new PointF(mWidth * 0.5f, mHeight * (1 - PETIOLE_RATIO));
        mBezierControl = new PointF(0, mHeight * (1 - 3 * PETIOLE_RATIO));
        mBezierTop = new PointF(mWidth * 0.5f, 0);

        mVeinBottomY = mHeight * (1 - PETIOLE_RATIO) - 10;
        mOneNodeY = mVeinBottomY * 4 / 5;
        mTwoNodeY = mVeinBottomY * 2 / 5;

        initEngine();
        setOrignalStatus();


    }

    /**
     * 初始化path引擎
     */
    private void initEngine() {
        //叶柄动画，从下到上画
        mPetioleAnim = ValueAnimator.ofFloat(mHeight, mHeight * (1 - PETIOLE_RATIO));
        mArcAnim = ValueAnimator.ofFloat(0, 1.0f).setDuration(mArcTime);
        mLastAnim = ValueAnimator.ofFloat(mVeinBottomY, 0).setDuration(mLastLineTime);

        mPetioleAnim.setInterpolator(new LinearInterpolator());
        mArcAnim.setInterpolator(new LinearInterpolator());
        mLastAnim.setInterpolator(new LinearInterpolator());

        mArcRightAnim = mArcAnim.clone();

        mPetioleAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mY = (float) animation.getAnimatedValue();
            }
        });

        mArcAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                computeArcPointF(animation, true);
            }
        });

        mArcRightAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                computeArcPointF(animation, false);
            }
        });

        mLastAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mY = (float) animation.getAnimatedValue();
                //Math.toRadians(30) ：30度转换为弧度
                float tan = (float) Math.tan(Math.toRadians(30));
                if (mY <= mOneNodeY && mY > mTwoNodeY) {
                    mOneLpath.moveTo(mX, mOneNodeY);
                    mOneRpath.moveTo(mX, mOneNodeY);
                    mMainPath.addPath(mOneLpath, 0, EXPERIENCE_OFFFSET);
                    mMainPath.addPath(mOneRpath, 0, EXPERIENCE_OFFFSET);

                    float gapY = mOneNodeY - mY;
                    mOneLpath.rLineTo(-gapY * tan, -gapY);
                    mOneRpath.lineTo(mX + gapY * tan, mY);
                } else if (mY <= mTwoNodeY) {
                    mTwoLpath.moveTo(mX, mTwoNodeY);
                    mTwoRpath.moveTo(mX, mTwoNodeY);
                    //第二个节点 ，为了避免超出叶子，取差值的一半
                    float gapY = (mTwoNodeY - mY) * 0.5f;
                    mMainPath.addPath(mTwoLpath, 0, EXPERIENCE_OFFFSET);
                    mMainPath.addPath(mTwoRpath, 0, EXPERIENCE_OFFFSET);
                    mTwoLpath.rLineTo(-gapY * tan, -gapY);
                    mTwoRpath.rLineTo(gapY * tan, -gapY);
                }
            }
        });
        mEngine = new AnimatorSet();
        mEngine.playSequentially(mPetioleAnim, mArcAnim, mArcRightAnim, mLastAnim);
        mEngine.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setOrignalStatus();
            }
        });
    }


    private void computeArcPointF(ValueAnimator animation, boolean isLeft) {
        float ratio = (float) animation.getAnimatedValue();

        PointF bezierStart = isLeft ? mBezierBottom : mBezierTop;
        PointF bezierControl = isLeft ? mBezierControl : new PointF(mWidth, mHeight * (1 - 3 * PETIOLE_RATIO));
        PointF bezierEnd = isLeft ? mBezierTop : new PointF(mWidth / 2, mVeinBottomY);

        PointF pointF = calculateCurPoint(ratio, bezierStart, bezierControl, bezierEnd);
        mX = pointF.x;
        mY = pointF.y;
    }

    private PointF calculateCurPoint(float ratio, PointF p0, PointF p1, PointF p2) {
        PointF point = new PointF();
        float temp = 1 - ratio;

        //这个算法是二阶贝塞尔曲线的定义
        point.x = temp * temp * p0.x + 2 * temp * ratio * p1.x + ratio * ratio * p2.x;
        point.y = temp * temp * p0.y + 2 * temp * ratio * p1.y + ratio * ratio * p2.y;
        return point;
    }

    private void setOrignalStatus() {
        mX = mWidth * 0.5f;
        mY = mHeight;
        if (null == mMainPath) {
            mMainPath = new Path();
            mOneLpath = new Path();
            mOneRpath = new Path();
            mTwoLpath = new Path();
            mTwoRpath = new Path();
        }

        mMainPath.rewind();
        mOneLpath.rewind();
        mOneRpath.rewind();
        mTwoLpath.rewind();
        mTwoRpath.rewind();

        mMainPath.moveTo(mX, mY);

    }

    private void setStepTime(long totalDuration) {
        mPetioleTime = (long) (totalDuration * PETIOLE_RATIO);//绘制叶柄时间
        mArcTime = (long) (totalDuration * (1 - PETIOLE_RATIO) * 0.4f);//左右轮廓时间
        mLastLineTime = totalDuration - mPetioleTime - mArcTime * 2;
    }

    public void endAndClear() {
        mPetioleAnim.end();
        mArcAnim.end();
        mLastAnim.end();
        mEngine.end();
        setOrignalStatus();
    }

    public void start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (mEngine.isPaused()) {
                mEngine.resume();
                return;
            }
        }

        if (mEngine.isRunning()) {
            mEngine.end();
        }
        mEngine.start();
    }

    public void pause() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            mEngine.pause();
        }
    }

    public void setTotalDuration(long totalDuration) {
        setStepTime(totalDuration);
    }

    public void drawGraph(Canvas canvas, Paint mPaint) {
        if (mEngine.isStarted()) {
            canvas.drawPath(mMainPath, mPaint);
            mMainPath.lineTo(mX, mY);
            Log.e(TAG, "mX = " + mX + "；mY = " + mY);

        } else {
            mEngine.start();
        }
    }

    public void setWidthAndHeight(int width, int height) {
        mWidth = width;
        mHeight = height;
    }
}
