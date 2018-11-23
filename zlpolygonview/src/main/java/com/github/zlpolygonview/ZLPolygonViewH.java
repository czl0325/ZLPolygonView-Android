package com.github.zlpolygonview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ZLPolygonViewH extends SurfaceView implements SurfaceHolder.Callback {
    private Paint mPaint;
    private Path mPath;
    private Context context;
    private SurfaceHolder mHolder;
    private Canvas mCanvas;

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

    private ZLPolygonView.onClickPolygonListeren onClickPolygonListeren;

    public ZLPolygonViewH(Context context) {
        this(context, null);
    }

    public ZLPolygonViewH(Context context, AttributeSet attrs) {
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

        mHolder = getHolder();
        mHolder.addCallback(this);
        setZOrderOnTop(true);
        setFocusable(true);
        setFocusableInTouchMode(true);

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
        drawCanvas(mHolder);
    }

    public void setTextLabels(List<String> textLabels) {
        mTextLabels.clear();
        mTextLabels.addAll(textLabels);
        drawCanvas(mHolder);
    }

    public void setInnerColor(int innerColor) {
        this.mInnerColor = innerColor;
        drawCanvas(mHolder);
    }

    public void setLineColor(int lineColor) {
        this.mLineColor = lineColor;
        drawCanvas(mHolder);
    }

    public void setLineWidth(int lineWidth) {
        this.mLineWidth = lineWidth;
        drawCanvas(mHolder);
    }

    public void setDotNumber(int dotNumber) {
        this.mDotNumber = dotNumber;
        drawCanvas(mHolder);
    }

    public void setEdgeNumber(int edgeNumber) {
        this.mEdgeNumber = edgeNumber;
        drawCanvas(mHolder);
    }

    public void setJsonString(String jsonString) {
        mPolygonValues.clear();
        mTextLabels.clear();
        Gson gson = new Gson();
        Type listType = new TypeToken<List<ZLPolygonView.ZLPolygonObject>>() {}.getType();
        List<ZLPolygonView.ZLPolygonObject> array = gson.fromJson(jsonString, listType);
        for (int i=0; i<array.size(); i++) {
            ZLPolygonView.ZLPolygonObject object = array.get(i);
            mTextLabels.add(object.getText());
            mPolygonValues.add(object.getValue());
        }
        drawCanvas(mHolder);
        //List array2 =(ArrayList)JSONArray.toCollection(array, HashMap.class);
        //System.out.println(array2);
    }

    public void setOnClickPolygonListeren(ZLPolygonView.onClickPolygonListeren onClickPolygonListeren) {
        this.onClickPolygonListeren = onClickPolygonListeren;
    }

    private void drawCanvas(SurfaceHolder holder) {
        try {
            mCanvas = holder.lockCanvas();

            //加上这句才能清空画布
            mCanvas.drawColor(Color.WHITE);
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);

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
            mCanvas.save();
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
                mCanvas.drawPath(mPath , mPaint);
                mCanvas.restore();
            }

            //画边缘线
            for (int j=0; j<mEdgeNumber; j++) {
                mPath.rewind();
                mCanvas.save();
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
                mCanvas.drawPath(mPath , mPaint);
                mCanvas.restore();
            }

            //绘制对角线
            for (int i=0; i<mOuterPoints.size(); i++) {
                Point point = mOuterPoints.get(i);
                mPath.moveTo(centerPoint.x, centerPoint.y);
                mPath.lineTo(point.x, point.y);
                mCanvas.drawPath(mPath , mPaint);
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
                mCanvas.drawText(str, textRect.centerX(), baseline, mPaint);
            }
        } catch (Exception e) {

        } finally {
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }

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
    public void surfaceCreated(SurfaceHolder holder) {
        mHolder = holder;
        mHolder.setFormat(PixelFormat.TRANSPARENT);
        drawCanvas(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e("czl","surfaceview  w="+width+";h="+height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

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
