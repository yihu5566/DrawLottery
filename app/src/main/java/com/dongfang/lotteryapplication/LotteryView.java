package com.dongfang.lotteryapplication;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Looper;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @Author : dongfang
 * Created Time : 2018-11-13  10:17
 * Description:
 */
public class LotteryView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    /**
     * 绘制中奖的矩形区域
     */
    private Paint mPaint;
    /**
     * 绘制开始按钮
     */
    private Paint mButtonPaint;
    /**
     * 绘制边框
     */
    private Paint mBorderPaint;
    /**
     * 绘制阴影
     */
    private Paint mShadePaint;

    /**
     * 绘制文字
     */
    private TextPaint mTextPaint;

    private ArrayList<Rect> mRectList;
    private int awardCount = 8;
    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    /**
     * 列数
     */
    private int rowCount;
    /**
     * 文字的颜色
     */
    private int fontColor;
    /**
     * 文字的字体大小
     */
    private int fontSize;
    /**
     * 背景颜色
     */
    private int backgroundColor;
    /**
     * 每个矩形方框的尺寸
     */
    private int everyWidth;

    private int currentCount = 0;


    /**
     * 开奖
     */
    private int currentStopCount = 0;

    private boolean isDrawing;
    private Thread drawThread;


    private List<String> awardList;
    private ObjectAnimator mRunningAnimator;

    public static final int IS_LOTTERYING = 1;
    public static final int IS_DEFAULT = 0;
    public static final int IS_RESULT = 2;

    public int lotteryState = IS_DEFAULT;
    /**
     * 抽奖按钮的半径
     */
    private int radius = 100;
    /**
     * 抽奖按钮是否可用
     */
    private boolean isEnable = true;

    private String[] str = {"奖品1", "奖品2", "奖品3", "奖品4", "奖品5", "奖品6", "奖品7", "奖品8"};
    /**
     * 实际绘制区域的宽度
     */
    private int realityWidth;
    /**
     * 开奖动画,这里需要加
     */
    private ObjectAnimator mStopAnimator;

    private Region mButtonRegion;
    private ValueAnimator mResultingAnimator;

    public LotteryView(Context context) {
        this(context, null);

    }

    public LotteryView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LotteryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCustomAttrs(context, attrs);
        init();
    }

    private void initCustomAttrs(Context context, AttributeSet attrs) {
        //获取自定义属性。
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LotteryViewAttrs);
        //获取默认行数
        rowCount = ta.getInteger(R.styleable.LotteryViewAttrs_rowCount, 4);
        fontSize = (int) ta.getDimension(R.styleable.LotteryViewAttrs_fontSize, 16);
        fontColor = ta.getColor(R.styleable.LotteryViewAttrs_fontColor, Color.BLUE);
        backgroundColor = ta.getColor(R.styleable.LotteryViewAttrs_backgroundColor, Color.GREEN);

        ta.recycle();

    }


    public void setAwardList(List<String> awardList) {
        if (awardList == null || awardList.size() == 0) {
            LogUtil.d("请传入正确的数据");
            return;
        }
        this.awardList = awardList;
        awardCount = awardList.size();
    }


    public void setCurrentCount(int currentCount) {
        if (this.currentCount == mRectList.size()) {
            this.currentCount = 0;
        } else {
            this.currentCount++;

        }
        LogUtil.d("setCurrentCount--" + currentCount);
        mRectList.clear();
        try {
            // 这个就相当于帧频了，数值越小画面就越流畅
            mCanvas = mHolder.lockCanvas();
//            mCanvas.save();
//            drawShade(mCanvas);
//            mCanvas.restore();
            draw();
            Thread.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LogUtil.d("run_finally--unlockCanvasAndPost：");
            mHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    public void setCurrentStopCount(int currentStopCount) {
        LogUtil.d("setcurrentStopCount--" + currentStopCount);
        this.currentStopCount = currentStopCount;
        setCurrentCount(currentCount++);
    }

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);

        mRectList = new ArrayList<>();
        awardList = new ArrayList();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(backgroundColor);
        mPaint.setStyle(Paint.Style.FILL);

        mButtonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mButtonPaint.setColor(Color.RED);
        mButtonPaint.setStyle(Paint.Style.FILL);

        mBorderPaint = new Paint();
        mBorderPaint.setStrokeWidth(3);
        mBorderPaint.setColor(Color.WHITE);
        mBorderPaint.setStyle(Paint.Style.STROKE);

        mShadePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadePaint.setColor(Color.parseColor("#66888888"));
        mShadePaint.setStyle(Paint.Style.FILL);

        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(fontColor);
        mTextPaint.setTextSize(fontSize);
        awardList.addAll(Arrays.asList(str));
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LogUtil.d("surfaceCreated--调用surfaceCreated");
        isDrawing = true;
        drawThread = new Thread(this);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LogUtil.d("surfaceChanged--调用surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogUtil.d("surfaceDestroyed--调用surfaceDestroyed");
        currentCount = 0;
        if (mRunningAnimator != null) {
            mRunningAnimator.cancel();
            mRunningAnimator.removeAllListeners();
        }
        isDrawing = false;
//        mHolder.removeCallback(this);
//        mHolder = null;
//        mCanvas = null;
        mRectList.clear();
        drawThread = null;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mButtonRegion.contains(x, y)) {
                    LogUtil.d("onTouchEvent-X:" + x + "Y:" + y);

                }
                break;
            case MotionEvent.ACTION_UP:
                if (mButtonRegion.contains(x, y)) {
                    LogUtil.d("onTouchEvent-X:" + x + "Y:" + y);
                    if (isEnable) {
                        if (lotteryState == IS_DEFAULT) {
                            startLottery();
                        } else if (lotteryState == IS_LOTTERYING) {
                            stopLottery();
                        }
                    }
                }
                break;
            default:
                break;
        }
        return true;

    }

    @Override
    public void run() {
        while (isDrawing) {
            try {
                // 这个就相当于帧频了，数值越小画面就越流畅
                mCanvas = mHolder.lockCanvas();
                draw();
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                LogUtil.d("run_finally--unlockCanvasAndPost：");
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }

    }


    /**
     * 让阴影滚动起来
     *
     * @param
     */
    private void startLottery() {
        lotteryState = IS_LOTTERYING;
        drawLotteryButton(mCanvas);
        if (currentCount > mRectList.size()) {
            return;
        }
        if (mRunningAnimator != null) {
            currentCount = 0;
            mRunningAnimator.cancel();
        }
//        int timeResult = testRandom3() * 1000;
        //由于属性动画中，当达到最终值会立刻跳到下一次循环，所以需要补1
        mRunningAnimator = ObjectAnimator.ofInt(this, "currentCount", 0, 1);
        mRunningAnimator.setRepeatMode(ValueAnimator.RESTART);
        mRunningAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mRunningAnimator.setDuration(3000);
        mRunningAnimator.setInterpolator(new LinearInterpolator());
        mRunningAnimator.start();

    }

    /**
     * 重置抽奖状态
     */
    public void resetLottery() {
        lotteryState = IS_DEFAULT;
        drawLotteryButton(mCanvas);
        isDrawing = true;
        isEnable = true;
        currentCount = 0;
        if (mRunningAnimator != null) {
            mRunningAnimator.cancel();
        }
        mRectList.clear();
        try {
            // 这个就相当于帧频了，数值越小画面就越流畅
            mCanvas = mHolder.lockCanvas();
            draw();
            Thread.sleep(10);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LogUtil.d("run_finally--unlockCanvasAndPost：");
            mHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    private void stopLottery() {
        LogUtil.d("stopLottery--开奖了");
        isDrawing = true;
        isEnable = false;
        lotteryState = IS_RESULT;
        int timeResult = testRandom3() * 500;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mRunningAnimator != null) {
                    mRunningAnimator.setDuration(5000);
                    mRunningAnimator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {

                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            if (currentCount != mRectList.size()) {
                                Toast.makeText(getContext(), "中奖了：" + awardList.get(currentCount), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    mRunningAnimator.cancel();
                }
            }
        }, timeResult);

    }

    private int testRandom3() {
        Random random = new Random();
        return random.nextInt(10);
    }


    /**
     * 绘制开始
     */
    private void draw() {
        //计算出抽奖块的位置
        calculate();
        //绘制抽奖的背景
        drawBackground(mCanvas);
        //绘制开始按钮
        drawLotteryButton(mCanvas);
        //绘制遮罩
        drawShade(mCanvas);
    }

    private void drawLotteryButton(Canvas canvas) {
        mButtonRegion = new Region(realityWidth / 2 - radius / 2, realityWidth / 2 - radius / 2, realityWidth / 2 + radius / 2, realityWidth / 2 + radius / 2);
        canvas.drawCircle(realityWidth / 2, realityWidth / 2, radius, mButtonPaint);
        if (lotteryState == IS_LOTTERYING) {
            Point point = calculateTextLocation(mButtonRegion.getBounds(), "STOP");
            canvas.drawText("STOP", point.x, point.y, mTextPaint);
        } else {
            Point point = calculateTextLocation(mButtonRegion.getBounds(), "GO");
            canvas.drawText("GO", point.x, point.y, mTextPaint);

        }
    }

    private Point calculateTextLocation(Rect rectF1, String text) {
        Point point = new Point();
        //矩形区域的宽度
        int rectWidth = rectF1.right - rectF1.left;
        Rect rect = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), rect);
        int textWidth = rect.width();
        int textHeight = rect.height();

        float heih = rectF1.top + rectWidth / 2 + textHeight / 2;
        point.set(rectF1.left+(rectWidth - textWidth)/2, (int) heih);

        return point;

    }


    private void drawShade(Canvas mCanvas) {
        LogUtil.d("开始绘制阴影图" + currentCount);
        if (mRectList.size() > currentCount) {
            mCanvas.drawRect(mRectList.get(currentCount), mShadePaint);
        }
        if (mRectList.size() == rowCount * 4) {
            isDrawing = false;
        }
    }

    /**
     * 计算需要多少个奖品块，奖品平均分配到4个边上
     */
    private void calculate() {
        if (mCanvas.getWidth() < mCanvas.getHeight()) {
            everyWidth = mCanvas.getWidth() / (rowCount + 1);
        } else {
            everyWidth = mCanvas.getHeight() / (rowCount + 1);
        }
        realityWidth = everyWidth * (rowCount + 1);

        int left = -everyWidth;
        int top = 0;
        int right = 0;
        int bottom = everyWidth;
        for (int i = 0; i < rowCount; i++) {
            left += everyWidth;
            right += everyWidth;
            Rect rect = new Rect(left, top, right, bottom);
            mRectList.add(rect);
        }
//        LogUtil.d("calculate1--mRectList长度：" + mRectList.size());

        left = rowCount * everyWidth;
        top = -everyWidth;
        right = (rowCount + 1) * everyWidth;
        bottom = 0;
        for (int i = 0; i < rowCount; i++) {
            top += everyWidth;
            bottom += everyWidth;
            Rect rect = new Rect(left, top, right, bottom);
            mRectList.add(rect);
//            LogUtil.d("calculate2--top:" + rect.top + "bottom:" + rect.bottom);

        }
//        LogUtil.d("calculate2--mRectList长度：" + mRectList.size());

        left = (rowCount + 1) * everyWidth;
        top = rowCount * everyWidth;
        right = (rowCount + 2) * everyWidth;
        bottom = (rowCount + 1) * everyWidth;
        for (int i = 0; i < rowCount; i++) {
            left -= everyWidth;
            right -= everyWidth;
            Rect rect = new Rect(left, top, right, bottom);
            mRectList.add(rect);
//            LogUtil.d("calculate3--left:" + rect.left + "right:" + rect.right);

        }
//        LogUtil.d("calculate3--mRectList长度：" + mRectList.size());

        left = 0;
        top = (rowCount + 1) * everyWidth;
        right = everyWidth;
        bottom = (rowCount + 2) * everyWidth;
        for (int i = 0; i < rowCount; i++) {
            top -= everyWidth;
            bottom -= everyWidth;
            Rect rect = new Rect(left, top, right, bottom);
            mRectList.add(rect);
//            LogUtil.d("calculate4--top:" + rect.top + "bottom:" + rect.bottom);

        }
//        LogUtil.d("calculate4--mRectList长度：" + mRectList.size());

    }


    private void drawBackground(Canvas canvas) {
//        LogUtil.d("mRectList长度：" + mRectList.size());
        canvas.drawRect(new Rect(0, 0, mCanvas.getWidth(), canvas.getHeight()), mPaint);
        for (int i = 0; i < mRectList.size(); i++) {
//            LogUtil.d("开始绘制第：" + i);
            Rect rectF1 = mRectList.get(i);
            canvas.drawRect(rectF1, mPaint);
            canvas.drawRect(rectF1, mBorderPaint);
            //计算文字的位置
            if (i < awardCount) {
                Point point = calculateTextLocation(rectF1, awardList.get(i));
                mCanvas.drawText(awardList.get(i), point.x, point.y, mTextPaint);
            } else {
                Point point = calculateTextLocation(rectF1, awardList.get(i - awardCount));
                mCanvas.drawText(awardList.get(i - awardCount), point.x, point.y, mTextPaint);

            }
        }

    }


//    private int generateResult() {
//        //产生抽奖结果
//        Random random = new Random();
//        float r = random.nextFloat();
//        float total = 0;
//        float lastTotal = 0;
//        int size = awardList.size();
//        for (int i = 0; i < size; i++) {
////            total += awardNam[i].getRate();
//            if (r >= lastTotal && r <= total) {
//                return i;
//            }
//            lastTotal = total;
//        }
//        return -1;
//    }


}
