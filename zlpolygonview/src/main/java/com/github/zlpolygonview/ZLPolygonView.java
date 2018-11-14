package com.github.zlpolygonview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZLPolygonView extends View {
    private Paint mPaint;
    private Path mPath;
    private Context context;

    private int mInnerColor;
    private int mLineColor;
    private int mLineWidth;
    private int mDotNumber;
    private int mEdgeNumber;

    private List<Float> mPolygonValues = new ArrayList<>();
    private List<String> mTextLabels = new ArrayList<>();
    private int mTextSize;

    private List<Point> mOuterPoints;
    private List<Point> mInnerPoints;
    private List<Point> mTextPoints;

    private onClickPolygonListeren onClickPolygonListeren;

    public ZLPolygonView(Context context) {
        super(context);
        this.context = context;

        init();
    }

    public ZLPolygonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ZLPolygonView);
        mInnerColor = a.getColor(R.styleable.ZLPolygonView_InnerColor, Color.CYAN);
        mLineColor = a.getColor(R.styleable.ZLPolygonView_LineColor, Color.GRAY);
        mLineWidth = a.getColor(R.styleable.ZLPolygonView_LineWidth, 4);
        mDotNumber = a.getInt(R.styleable.ZLPolygonView_DotNumber, 4);
        mEdgeNumber = a.getInt(R.styleable.ZLPolygonView_EdgeNumber, 4);
        a.recycle();

        init();
    }

    private void init() {
        mOuterPoints = new ArrayList<>();
        mInnerPoints = new ArrayList<>();
        mTextPoints = new ArrayList<>();
        mTextSize = dp2px(context,20);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPath = new Path();
    }

    public void setPolygonValues(List<Float> polygonValues) {
        if (polygonValues.size() < 3) {
            return;
        }
        mPolygonValues.clear();
        mPolygonValues.addAll(polygonValues);
        mDotNumber = mPolygonValues.size();
        postInvalidate();
    }

    public void setTextLabels(List<String> textLabels) {
        mTextLabels.clear();
        mTextLabels.addAll(textLabels);
        postInvalidate();
    }

    public void setInnerColor(int innerColor) {
        this.mInnerColor = innerColor;
        postInvalidate();
    }

    public void setLineColor(int lineColor) {
        this.mLineColor = lineColor;
        postInvalidate();
    }

    public void setLineWidth(int lineWidth) {
        this.mLineWidth = lineWidth;
        postInvalidate();
    }

    public void setDotNumber(int dotNumber) {
        this.mDotNumber = dotNumber;
        postInvalidate();
    }

    public void setEdgeNumber(int edgeNumber) {
        this.mEdgeNumber = edgeNumber;
        postInvalidate();
    }

    public void setJsonString(String jsonString) {
        mPolygonValues.clear();
        mTextLabels.clear();
        Gson gson = new Gson();
        Type listType = new TypeToken<List<ZLPolygonObject>>() {}.getType();
        List<ZLPolygonObject> array = gson.fromJson(jsonString, listType);
        for (int i=0; i<array.size(); i++) {
            ZLPolygonObject object = array.get(i);
            mTextLabels.add(object.getText());
            mPolygonValues.add(object.getValue());
        }
        postInvalidate();
        //List array2 =(ArrayList)JSONArray.toCollection(array, HashMap.class);
        //System.out.println(array2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int minSize = Math.min(width, height);

        setMeasuredDimension(minSize, minSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mOuterPoints.clear();
        mInnerPoints.clear();
        mTextPoints.clear();

        mPaint.setColor(mLineColor);
        mPaint.setStrokeWidth(mLineWidth);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        Point centerPoint = new Point(width/2, height/2);
        int radius = Math.min(width/2, height/2);
        int textRadius = radius-mTextSize/2;
        int outerRadius = radius-mTextSize;
        float innerAngle = (float) (360.f/mDotNumber);
        Log.e("czl","size大小="+width+","+height);

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
                //Log.e("czl","第"+i+"个点:"+new Point((int)x,(int)y).toString());
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
                    Log.e("czl","边缘第"+i+"个点:"+new Point((int)x,(int)y).toString());
                    float textX = (float) (centerPoint.x-Math.cos(angleToRadian(90-innerAngle*i))*textRadius);
                    float textY = (float) (centerPoint.y-Math.sin(angleToRadian(90-innerAngle*i))*textRadius);
                    mTextPoints.add(new Point((int)textX, (int)textY));
                    Log.e("czl","文字第"+i+"个点:"+new Point((int)textX, (int)textY).toString());
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
            RectF textRect = new RectF(
                    point.x-mTextSize/2,
                    point.y-mTextSize/2,
                    point.x+mTextSize/2,
                    point.y+mTextSize/2);
            Log.e("czl","第"+i+"个矩形:"+textRect.toString());
            float baseline = (textRect.bottom + textRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
            mPaint.setTextSize(sp2px(12));
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(Color.BLACK);
            canvas.drawText(str, textRect.centerX(), baseline, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Log.e("czl","触摸的点:"+new Point((int)event.getX(), (int)event.getY()).toString());
                for (int i=0; i<mInnerPoints.size(); i++) {
                    Point pt = mInnerPoints.get(i);
                    if (Math.abs(event.getX()-pt.x)<dp2px(context, 15)
                            &&Math.abs(event.getY()-pt.y)<dp2px(context, 15)) {
                        if (onClickPolygonListeren != null) {
                            onClickPolygonListeren.onClickPolygon(event,i);
                            Log.e("czl","---"+event.getX()+"----"+event.getY() + "第" +i + "个点");
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

    public int dp2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public int sp2px(int spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public interface onClickPolygonListeren {
        void onClickPolygon(MotionEvent event, int index);
    }

    public static class ZLPolygonObject {
        private String text;
        private float value;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }
    }
}
