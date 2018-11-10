package com.github.zlpolygonview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class ZLPolygonView extends View {
    private Paint mPaint;
    private Path mPath;

    private int mInnerColor;
    private int mLineColor;
    private int mLineWidth;
    private int mDotNumber;
    private int mEdgeNumber;

    private List<Float> mPolygonValues;
    private List<String> mTextLabels;
    private int mTextSize;

    private List<Point> mOuterPoints;
    private List<Point> mInnerPoints;
    private List<Point> mTextPoints;

    private onClickPolygonListeren onClickPolygonListeren;

    public ZLPolygonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ZLPolygonView);
        mInnerColor = a.getColor(R.styleable.ZLPolygonView_InnerColor, Color.CYAN);
        mLineColor = a.getColor(R.styleable.ZLPolygonView_LineColor, Color.GRAY);
        mLineWidth = a.getColor(R.styleable.ZLPolygonView_LineWidth, 4);
        mDotNumber = a.getInt(R.styleable.ZLPolygonView_DotNumber, 4);
        mEdgeNumber = a.getInt(R.styleable.ZLPolygonView_EdgeNumber, 4);
        a.recycle();

        mOuterPoints = new ArrayList<>();
        mInnerPoints = new ArrayList<>();
        mTextPoints = new ArrayList<>();
        mTextSize = 50;//px2dp(context,25);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPath = new Path();
    }



    public void setPolygonValues(List<Float> polygonValues) {
        mPolygonValues = polygonValues;
        mDotNumber = mPolygonValues.size();
        postInvalidate();
    }

    public void setTextLabels(List<String> textLabels) {
        mTextLabels = textLabels;
        postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mOuterPoints.clear();
        mInnerPoints.clear();

        mPaint.setColor(mLineColor);
        mPaint.setStrokeWidth(mLineWidth);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        Point centerPoint = new Point(width/2, height/2);
        int radius = Math.min(width/2, height/2);
        int textRadius = radius-mTextSize/2;
        int outerRadius = radius-mTextSize;
        float innerAngle = (float) (360.f/mDotNumber);

        mPath.rewind();
        canvas.save();
        //绘制能力值
        if (mPolygonValues != null && mPolygonValues.size() > 0) {
            for (int i=0; i<mPolygonValues.size(); i++) {
                Float F = mPolygonValues.get(i);
                float f = F.floatValue();
                if (f > 1) {
                    f = 1;
                }
                if (f < 0) {
                    f = 0;
                }
                float currentRadius = (float) (outerRadius*f);
                float x = (float) (centerPoint.x-Math.cos(angleToRadian(90-innerAngle*i))*currentRadius);
                float y = (float) (centerPoint.y-Math.sin(angleToRadian(90-innerAngle*i))*currentRadius);
                if (i==0) {
                    mPath.moveTo(x,y);
                } else {
                    mPath.lineTo(x,y);
                }
                mInnerPoints.add(new Point((int)x,(int)y));
            }
            mPath.close();
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mInnerColor);
            canvas.drawPath(mPath , mPaint);
            canvas.restore();
        }

        //画边缘线
        for (int j=0; j<mEdgeNumber; j++) {
            mPath.rewind();
            canvas.save();
            for (int i=0; i<mDotNumber; i++) {
                float currentRadius = (float) (outerRadius*((j+1)*(1.0/mEdgeNumber)));
                float x = (float) (centerPoint.x-Math.cos(angleToRadian(90-innerAngle*i))*currentRadius);
                float y = (float) (centerPoint.y-Math.sin(angleToRadian(90-innerAngle*i))*currentRadius);
                if (i==0) {
                    mPath.moveTo(x,y);
                } else {
                    mPath.lineTo(x,y);
                }
                if ((j+1)*(1.0/mEdgeNumber)==1) {
                    mOuterPoints.add(new Point((int)x, (int)y));
                    float textX = (float) (centerPoint.x-Math.cos(angleToRadian(90-innerAngle*i))*textRadius);
                    float textY = (float) (centerPoint.y-Math.sin(angleToRadian(90-innerAngle*i))*textRadius);
                    mTextPoints.add(new Point((int)textX, (int)textY));
                }
            }
            mPath.close();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(mLineColor);
            canvas.drawPath(mPath , mPaint);
            canvas.restore();
        }

        //绘制对角线
        for (int i=0; i<mOuterPoints.size(); i++) {
            Point point = mOuterPoints.get(i);
            mPath.moveTo(centerPoint.x, centerPoint.y);
            mPath.lineTo(point.x, point.y);
            canvas.drawPath(mPath , mPaint);
        }

        //绘制文字
        for (int i=0; i<mTextPoints.size(); i++) {
            String str = "空";
            if (mTextLabels != null && i < mTextLabels.size()) {
                str = mTextLabels.get(i);
            }
            Point point = mTextPoints.get(i);
            Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
            Rect textRect = new Rect(
                    point.x-mTextSize/2,
                    point.y-mTextSize/2,
                    point.x+mTextSize,
                    point.y+mTextSize);
            int baseline = (textRect.bottom + textRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.YELLOW);
//            canvas.drawRect(textRect, mPaint);
//            mPaint.setTextSize(30f);
//            mPaint.setTextAlign(Paint.Align.CENTER);
//            mPaint.setStyle(Paint.Style.STROKE);
//            mPaint.setColor(Color.BLACK);
//            canvas.drawText(str, textRect.centerX(), baseline, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                for (int i=0; i<mInnerPoints.size(); i++) {
                    Point pt = mInnerPoints.get(i);
                    if (Math.abs(event.getX()-pt.x)<10
                            &&Math.abs(event.getY()-pt.y)<10) {
                        if (onClickPolygonListeren != null) {
                            onClickPolygonListeren.onClickPolygon(event,i);
                            Log.e("czl","---"+event.getX()+"----"+event.getY()
                            + "第" +i + "个点");
                            break;
                        }
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setOnClickPolygonListeren(onClickPolygonListeren onClickPolygonListeren) {
        this.onClickPolygonListeren = onClickPolygonListeren;
    }

    private double angleToRadian(double angle) {
        return angle * Math.PI / 180.0;
    }

    public int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public int px2dp(Context context, float pxValue) {
        final float scale =  context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    private interface onClickPolygonListeren {
        void onClickPolygon(MotionEvent event, int index);
    }
}
