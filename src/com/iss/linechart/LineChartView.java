
package com.iss.linechart;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * 折线图表自定义View
 * 
 * @author hubing
 * @version 1.0.0 2016-1-12
 */

public class LineChartView extends FrameLayout {

    /** 网速单位 */
    private static final String MB = "Mbps";

    /** 坐标的线条颜色 */
    private int coordinateColor = 0xffeeeeee;

    /** 要显示的线条列表 */
    private ArrayList<Line> lines;

    /** 左侧坐标参考圆点颜色 */
    private int referCircleColor = 0xFF4cc2b6;

    /** 线条大小 */
    private int lineSize = 3;

    /** 小圆点半径 */
    private int circleRadius = 6;

    /** 显示的坐标基线数 */
    private int baseLineCount = 8;

    /** 垂直坐标圆点数 */
    private int verticalCircleCount = 10;

    /** 最大值 */
    private float maxValue = 100F;

    /** 控件高 */
    private int mHeight;

    /** 实际图表显示高度 */
    private int mRealChartHeight;

    /** 控件宽 */
    private int mWidth;

    /** 垂直坐标基线数组 */
    private CoordinateLine[] verticalLines;

    /** 垂直坐标圆点数组 */
    private Point[] verticalCircles;

    /** 画笔 */
    private Paint paint;

    private int paddingLeft;

    private int paddingRight;

    /** 是否需要显示点值 */
    private boolean isNeedShowPointValue = false;

    /** 是否启动动画 */
    private boolean isStartAnimotion = true;

    /** 显示数据点的TextView */
    private TextView tvPoint;

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        // 设置此方法，让ViewGroup调用onDraw方法
        setWillNotDraw(false);
        setNeedShowPointValue(isNeedShowPointValue);
    }

    /**
     * 设置是否需要显示点的值
     * 
     * @param isNeedShowPointValue true表示触摸对应的点，显示相应的点数值
     * @author hubing
     */
    public void setNeedShowPointValue(boolean isNeedShowPointValue) {
        if (isNeedShowPointValue && tvPoint == null) {
            // 创建显示数据点的子控件
            createPointView(getContext());
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            addView(tvPoint, params);
        }
        this.isNeedShowPointValue = isNeedShowPointValue;
    }

    /**
     * 创建显示数据点的TextView
     * 
     * @param context 上下文对象
     * @author hubing
     */
    void createPointView(Context context) {
        tvPoint = new TextView(context);
        tvPoint.setBackgroundResource(R.drawable.point_select_bg);
        tvPoint.setGravity(Gravity.CENTER);
        tvPoint.setVisibility(View.GONE);
        tvPoint.setTextColor(0xffffffff);
    }

    /**
     * 显示数据点值
     * 
     * @param x 图表上数据点圆心x坐标
     * @param y 图表上数据点圆心y坐标
     * @param valueText 数据点的文本数值
     * @author hubing
     */
    private void showPointValue(float x, float y, String valueText) {
        if (isNeedShowPointValue) {
            // 计算并设置数据点显示位置
            float pointX = x + circleRadius - tvPoint.getBackground().getMinimumWidth() / 2;
            float pointY = y + circleRadius;

            tvPoint.setX(pointX);
            tvPoint.setY(pointY);
            tvPoint.setText(valueText);
            tvPoint.setVisibility(View.VISIBLE);
            // 开启显示动画
            startShowAnimation();
        }
    }

    /**
     * 启动显示数据点值的动画
     * 
     * @author hubing
     */
    private void startShowAnimation() {
        // x缩放
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(tvPoint, "scaleX", 0.2F, 1F);
        animatorX.setDuration(200);
        // y缩放
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(tvPoint, "scaleY", 0.2F, 1F);
        animatorY.setDuration(200);
        // 动画集合，让两个缩放动画一起播放
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorX, animatorY);
        set.start();
    }

    /**
     * 隐藏数据点值
     * 
     * @author hubing
     */
    private void hidePointValue() {
        if (isNeedShowPointValue && tvPoint.getVisibility() == View.VISIBLE) {
            // 启动隐藏动画
            startHideAnimation();
        }
    }

    /**
     * 启动隐藏数据点的动画
     * 
     * @author hubing
     */
    private void startHideAnimation() {
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(tvPoint, "scaleX", 1F, 0.2F);
        animatorX.setDuration(200);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(tvPoint, "scaleY", 1F, 0.2F);
        animatorY.setDuration(200);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animatorX, animatorY);
        set.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                tvPoint.setVisibility(View.GONE);
            }

        });
        set.start();
    }

    /**
     * 初始化参数
     * 
     * @author hubing
     */
    private void init() {
        // 创建画笔对象
        paint = new Paint();
        // 设置抗锯齿
        paint.setAntiAlias(true);
        // 设置线宽
        paint.setStrokeWidth(lineSize);

        lines = new ArrayList<Line>();

        // 初始化参照物
        initReference();
    }

    /**
     * 设置基线数
     * 
     * @param baseLineCount
     * @author hubing
     */
    public void setBaseLineCount(int baseLineCount) {
        this.baseLineCount = baseLineCount;
        // 重新初始化参照物
        initReference();
        // 计算坐标背景线位置
        calculateVerticalLines();
        // 计算垂直坐标点坐标
        calculateVerticalCircles();
        this.invalidate();
    }

    /**
     * 初始化参照物
     * 
     * @author hubing
     */
    private void initReference() {
        // 初始化坐标背景线
        verticalLines = new CoordinateLine[baseLineCount];
        CoordinateLine tempCoordinateLine;
        for (int i = 0; i < verticalLines.length; i++) {
            tempCoordinateLine = new CoordinateLine(0, 0, 0, 0);
            verticalLines[i] = tempCoordinateLine;
        }

        // 初始化垂直坐标圆点
        verticalCircles = new Point[verticalCircleCount];
        Point tempPoint;
        for (int i = 0; i < verticalCircles.length; i++) {
            tempPoint = new Point();
            verticalCircles[i] = tempPoint;
        }
    }

    /**
     * 设置最大值
     * 
     * @param maxValue
     * @author hubing
     */
    public void setMaxValue(float maxValue) {
        if (maxValue <= 0) {
            return;
        }
        this.maxValue = maxValue;
    }

    /**
     * 设置坐标线条的颜色
     * 
     * @param coordinateColor
     * @author hubing
     */
    public void setCoordinateColor(int coordinateColor) {
        this.coordinateColor = coordinateColor;
        invalidate();
    }

    /**
     * 设置左侧坐标参考圆点颜色
     * 
     * @param referCircleColor
     * @author hubing
     */
    public void setReferCircleColor(int referCircleColor) {
        this.referCircleColor = referCircleColor;
    }

    /**
     * 清除之前添加的所有线条
     * 
     * @author hubing
     */
    public void clearAllLines() {
        lines.clear();
    }

    /**
     * 添加线条到图表上显示
     * 
     * @param linePoints
     * @param lineColor
     * @author hubing
     */
    public void addLinePoints(IPointValue[] linePoints, int lineColor) {
        if (linePoints == null || linePoints.length == 0 || linePoints.length > baseLineCount) {
            String msg = linePoints == null ? "linePoints为: null" : "linePoints的size为: " + linePoints.length;
            throw new IllegalArgumentException("非法的参数," + msg);
        }
        CoordinatePoint[] coordinatePoints = new CoordinatePoint[linePoints.length];
        Line line = new Line(linePoints, coordinatePoints, lineColor);
        lines.add(line);
        // 计算线条对应的坐标点位置
        calculateLinePosition(line);

        hidePointValue();

        // 启动动画
        isStartAnimotion = true;
        // 重绘
        this.invalidate();
    }

    /**
     * 计算线条对应的坐标点位置
     * 
     * @param line
     * @author hubing
     */
    private void calculateLinePosition(Line line) {
        IPointValue[] linePoints = line.linePoints;
        CoordinatePoint[] coordinatePoints = line.coordinatePoints;
        for (int i = 0; i < coordinatePoints.length; i++) {
            coordinatePoints[i] = new CoordinatePoint();
            float value = linePoints[i].getValue();
            int y = mRealChartHeight - (int) (value / maxValue * mRealChartHeight);
            coordinatePoints[i].set(verticalLines[i].startX, y, value);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 判断触摸区域，看看是否触摸在了图表上的点上
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            // 先判断x触摸区域
            int x = (int) event.getX();
            if (lines.size() > 0) {
                Line tempLine = lines.get(0);
                for (int i = 0; i < tempLine.coordinatePoints.length; i++) {
                    CoordinatePoint point = tempLine.coordinatePoints[i];
                    float left = point.x - circleRadius * 3;
                    float right = point.x + circleRadius * 3;
                    if (x >= left && x <= right) {
                        // 判断y触摸区域
                        int y = (int) event.getY();
                        for (Line line : lines) {
                            CoordinatePoint point1 = line.coordinatePoints[i];
                            float top = point1.y - circleRadius * 3;
                            float bottom = point1.y + circleRadius * 3;
                            if (y >= top && y <= bottom) {
                                // 点在了点上，显示该点的数据
                                String valueText = (int) point1.value + MB;
                                showPointValue(point1.x, point1.y, valueText);
                                return true;
                            }
                        }
                    }
                }
            }
            // 点击其他区域，隐藏显示点数据
            hidePointValue();
        } else if (action == MotionEvent.ACTION_UP) {
            if (isClickable()) {
                return performClick();
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mHeight = getMeasuredHeight();
        mWidth = getMeasuredWidth();

        // 计算图表显示高度
        mRealChartHeight = mHeight - circleRadius * 2;

        paddingLeft = (int) (mWidth * 0.1);
        paddingRight = paddingLeft;

        // 计算坐标背景线位置
        calculateVerticalLines();

        // 计算垂直坐标点坐标
        calculateVerticalCircles();

        // 计算线条的位置(解决在View未测量出高度之前添加的线条无法显示的问题)
        for (int i = 0; i < lines.size(); i++) {
            // 计算线条对应的坐标点位置
            Line line = lines.get(i);
            calculateLinePosition(line);
        }

    }

    /**
     * 计算坐标背景线位置
     * 
     * @author hubing
     */
    private void calculateVerticalLines() {
        // 计算区间宽度
        int lineSpace = (mWidth - paddingLeft - paddingRight) / (baseLineCount - 1);
        // 计算坐标背景线位置
        for (int i = 0; i < verticalLines.length; i++) {
            int startX = paddingLeft + i * lineSpace;
            int startY = 0;
            int endX = startX;
            int endY = mHeight;
            verticalLines[i].set(startX, startY, endX, endY);
        }
    }

    /**
     * 计算左边垂直坐标点坐标
     * 
     * @author hubing
     */
    private void calculateVerticalCircles() {
        // 计算两个圆点之间的高度
        int verticalSpace = mRealChartHeight / (verticalCircleCount - 1);
        // 计算垂直坐标点坐标
        for (int i = 0; i < verticalCircles.length; i++) {
            int x = paddingLeft;
            int y;
            if (i == 0) {
                y = circleRadius;
            } else if (i == verticalCircles.length - 1) {
                y = mHeight - circleRadius;
            } else {
                y = verticalCircles[i - 1].y + verticalSpace;
            }
            verticalCircles[i].set(x, y);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isStartAnimotion) {
            isStartAnimotion = false;
            startDrawLineAnimation();
        } else {
            // 绘制坐标基准线
            drawCoordinateLine(canvas);
            // 绘制左侧坐标圆点
            drawVerticalCircle(canvas);
            // 画线条
            drawAllLine(canvas);
        }
    }

    /**
     * 开启绘制线条动画
     * 
     * @author hubing
     */
    private void startDrawLineAnimation() {
        // 创建所有线条动画集合
        ArrayList<Animator> lineAnimators = new ArrayList<Animator>();
        for (int i = 0; i < lines.size(); i++) {
            // 计算线条对应的坐标点移动动画
            Line line = lines.get(i);

            // 创建单条线条动画集合
            ArrayList<Animator> items = new ArrayList<Animator>();
            ValueAnimator animator;
            for (int p = 0; p < line.coordinatePoints.length; p++) {
                // 计算线条轨迹
                if (p == 0) {
                    line.path.moveTo(line.coordinatePoints[p].x, line.coordinatePoints[p].y);
                } else {
                    CoordinatePoint startValue = line.coordinatePoints[p - 1];
                    CoordinatePoint endValue = line.coordinatePoints[p];
                    
                    // 创建两点之间的线条绘制属性动画
                    animator = ValueAnimator.ofObject(new PointEvaluator(), startValue, endValue);
                    animator.setDuration(300);
                    animator.addUpdateListener(new LineAnimatorUpdateListener(line));
                    items.add(animator);
                }
            }
            AnimatorSet lineSet = new AnimatorSet();
            lineSet.playSequentially(items);

            // 添加单条线条动画
            lineAnimators.add(lineSet);
        }

        // 启动所有线条动画
        AnimatorSet set = new AnimatorSet();
        set.playTogether(lineAnimators);
        set.start();
    }

    /**
     * 线条动画更新监听器
     * 
     * @author hubing
     * @version [1.0.0.0, 2016-3-6]
     */
    class LineAnimatorUpdateListener implements AnimatorUpdateListener {

        private Line line;

        public LineAnimatorUpdateListener(Line line) {
            this.line = line;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            CoordinatePoint coordinatePoint = (CoordinatePoint) animation.getAnimatedValue();
            line.path.lineTo(coordinatePoint.x, coordinatePoint.y);
            invalidate();
        }

    }

    /**
     * 自定义坐标点估值器
     * 
     * @author hubing
     * @version [1.0.0.0, 2016-3-6]
     */
    static class PointEvaluator implements TypeEvaluator<CoordinatePoint> {

        @Override
        public CoordinatePoint evaluate(float fraction, CoordinatePoint startValue, CoordinatePoint endValue) {
            CoordinatePoint cp = new CoordinatePoint();
            cp.x = startValue.x + fraction * (endValue.x - startValue.x);
            cp.y = startValue.y + fraction * (endValue.y - startValue.y);
            return cp;
        }

    }

    /**
     * 画所有要显示的线条
     * 
     * @param canvas
     * @author hubing
     */
    private void drawAllLine(Canvas canvas) {
        for (int i = 0; i < lines.size(); i++) {
            Line line = lines.get(i);
            paint.setColor(line.color);
            // 设置画线条模式
            paint.setStyle(Style.STROKE);
            // 绘制线条
            canvas.drawPath(line.path, paint);
            drawLinePoint(canvas, line.coordinatePoints);
        }
    }

    /**
     * 画图表线条上对应的点
     * 
     * @param canvas
     * @param coordinatePoints
     * @author hubing
     */
    private void drawLinePoint(Canvas canvas, CoordinatePoint[] coordinatePoints) {
        if (coordinatePoints == null) {
            return;
        }
        paint.setStyle(Style.FILL);
        for (int i = 0; i < coordinatePoints.length; i++) {
            // 画点
            canvas.drawCircle(coordinatePoints[i].x, coordinatePoints[i].y, circleRadius, paint);
        }
    }

    /**
     * 绘制左侧坐标圆点
     * 
     * @param canvas
     * @author hubing
     */
    private void drawVerticalCircle(Canvas canvas) {
        if (verticalCircles == null) {
            return;
        }
        // 设置画笔颜色
        paint.setColor(referCircleColor);
        paint.setStyle(Style.FILL);
        for (int i = 0; i < verticalCircles.length; i++) {
            canvas.drawCircle(verticalCircles[i].x, verticalCircles[i].y, circleRadius, paint);
        }
    }

    /**
     * 绘制坐标基准线
     * 
     * @param canvas
     * @author hubing
     */
    private void drawCoordinateLine(Canvas canvas) {
        if (verticalLines == null) {
            return;
        }
        // 设置画笔颜色
        paint.setColor(coordinateColor);
        for (int i = 0; i < verticalLines.length; i++) {
            canvas.drawLine(verticalLines[i].startX, verticalLines[i].startY, verticalLines[i].endX, verticalLines[i].endY, paint);
        }
    }

    /**
     * 坐标线对象
     * 
     * @author hubing
     * @version [1.0.0.0, 2016-2-22]
     */
    class CoordinateLine {

        /** 起点x坐标 */
        public int startX;

        /** 起点y坐标 */
        public int startY;

        /** 终点x坐标 */
        public int endX;

        /** 终点y坐标 */
        public int endY;

        public CoordinateLine() {
        }

        public CoordinateLine(int startX, int startY, int endX, int endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        /**
         * 设置背景线坐标
         * 
         * @param startX 起点x坐标
         * @param startY 起点y坐标
         * @param endX 终点x坐标
         * @param endY 终点y坐标
         * @author hubing
         */
        public void set(int startX, int startY, int endX, int endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

    }

    /**
     * 要显示在图表上的数据值接口,用以获取要显示的数据值
     * 
     * @author hubing
     * @version [1.0.0.0, 2016-2-22]
     */
    public interface IPointValue {

        /**
         * 获取图表上展示的数据值
         * 
         * @return
         * @author hubing
         */
        float getValue();

    }

    /**
     * 线条对象
     * 
     * @author hubing
     * @version [1.0.0.0, 2016-2-23]
     */
    class Line {

        /** 线条颜色 */
        public int color;

        /** 坐标点对应的值 */
        public IPointValue[] linePoints;

        /** 线条路径 */
        public Path path = new Path();

        /** 线条的点数组 */
        public CoordinatePoint[] coordinatePoints;

        public Line() {
        }

        public Line(IPointValue[] linePoints, CoordinatePoint[] coordinatePoints, int color) {
            this.linePoints = linePoints;
            this.coordinatePoints = coordinatePoints;
            this.color = color;
        }

    }

    /**
     * 图表上显示的坐标点实体类
     * 
     * @author hubing
     * @version [1.0.0.0, 2016-2-22]
     */
    static class CoordinatePoint {

        /** 数据值圆心x坐标值 */
        public float x;

        /** 数据值圆心y坐标值 */
        public float y;

        /** 图表上展示的数据值 */
        public float value;

        public CoordinatePoint() {
        }

        public CoordinatePoint(float x, float y, float value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }

        /**
         * 设置图表上显示的坐标点对象值
         * 
         * @param x
         * @param y
         * @param value
         * @author hubing
         */
        public void set(float x, float y, float value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }

    }

}
