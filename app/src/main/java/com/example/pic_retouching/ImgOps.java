package com.example.pic_retouching;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.core.widget.ImageViewCompat;

import com.yalantis.ucrop.view.CropImageView;

public class ImgOps extends ImageView implements ScaleGestureDetector.OnScaleGestureListener,
        View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener {

    private boolean isLayout = true;
    private Matrix matrix;
    private float[] matrixValues = new float[9];
    // used to preserve the 9 values of a matrix

    private ScaleGestureDetector scaleGestureDetector = null;
    private GestureDetector mGestureDetector;
    private float initScale = 1.0f;
    public static final float SCALE_MAX = 4.0f;
    private static final float SCALE_MID = 2.0f;

    private boolean isDrag = true;
    private int lastPointCount;
    private float lastX;
    private float lastY;
    private int mode = 0;
    private Bitmap bitmap;

    private int WIDTH = 200;
    private int HEIGHT = 200;
    private int COUNT = (WIDTH + 1) * (HEIGHT + 1);


    private float[] verts = new float[COUNT * 2];

    private float[] orig = new float[COUNT * 2];

    private int screenWidth, screenHeight;//屏幕的宽高

    private int mWidth, mHeight;//View 的宽高

    //作用范围半径
    private int r = 100;


    public ImgOps(Context context, AttributeSet arrts) {
        super(context, arrts);
        super.setScaleType(ScaleType.MATRIX);
        // make the image controlled by matrix

        matrix = new Matrix();
        scaleGestureDetector = new ScaleGestureDetector(context, this);
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener());
        // init matrix and scaleGestureDetector
        this.setOnTouchListener(this);
    }

    public ImgOps(Context context){
        super(context);
        super.setScaleType(ScaleType.MATRIX);
        // make the image controlled by matrix

        matrix = new Matrix();
        scaleGestureDetector = new ScaleGestureDetector(context, this);
        // init matrix and scaleGestureDetector
        this.setOnTouchListener(this);
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        warp(100, 100, 1900, 1900);
//        canvas.drawBitmapMesh(bitmap, WIDTH, HEIGHT, verts, 0, null, 0 , null);
//    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        bitmap = bm;
        //init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
        // add GlobalLayout listener
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        // remove GlobalLayout listener
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
//        detector.onTouchEvent()
        Log.e("ACTION", "Scale" );
        float currentScale = this.getScale();
        float scaleFactor = scaleGestureDetector.getScaleFactor();

        if (getDrawable() == null)
            return true;

        if(isDrag == false) {
            if ((currentScale < SCALE_MAX && scaleFactor > 1.0f) || (currentScale > initScale && scaleFactor < 1.0f)) {
                if (scaleFactor * currentScale < initScale)
                    scaleFactor = initScale / currentScale;
                // when smaller

                if (scaleFactor * currentScale > SCALE_MAX)
                    scaleFactor = SCALE_MAX / currentScale;
                // when bigger

                matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                setImageMatrix(matrix);
                // set the imageView

                Log.e("Scale", String.valueOf(matrixValues[Matrix.MSCALE_X]));
            } // set scale
        }
        return true;
    }


    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    public void setMode(int mode) {
        this.mode = mode;
        invalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        if (mGestureDetector.onTouchEvent(event)) {
//            return true;
//        }
        invalidate();
        //scaleGestureDetector.onTouchEvent(event);
        // has a problem when one of the point performs ACTION_UP (i.e. ACTION_POINTER_UP)
        float x = 0, y = 0;
        // init the touching point
        final int pointerCount = event.getPointerCount();
        // get the number of touching point

        lastPointCount = pointerCount;
        Log.e("countpoints", "COUNT " + lastPointCount);
        Log.e("Drag beginning", "can be drag? "+isDrag);
        x = event.getX();
        y = event.getY();

        if(lastPointCount == 1) {
            if(isDrag == true) {
                // only when isDrag is true, the image can be dragged
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // when a point down
                        lastX = event.getX();
                        lastY = event.getY();
                        Log.e("down", "ACTION_MOVE x ");
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // when the point change
                        float dx = x - lastX;
                        float dy = y - lastY;

                        Log.e("move", "ACTION_MOVE x " + dx);
//                        Log.e("touch", "ACTION_MOVE y " + dy);
                        matrix.postTranslate(dx, dy);
                        setImageMatrix(matrix);

                        lastX = x;
                        lastY = y;
                        // initialize lastX and lastY
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        lastPointCount = 0;
                        Log.e("up", "ACTION_MOVE x ");
//                        Log.e("up", "ACTION_MOVE y ");
                        // when up or cancel lastPointCount becomes 0
                        break;
                }
            }
        }else{
            isDrag = false;
            // when lastPointCount > 1 (mostly 2) the image can be scaled
            switch (event.getActionMasked()){
                case MotionEvent.ACTION_MOVE:
                    scaleGestureDetector.onTouchEvent(event);
                    Log.e("scaleGesture", "onTouch: "+scaleGestureDetector.getCurrentSpan() );
                    lastPointCount = 0;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    Log.e("TAG", "Do this step ");
                    isDrag = false;
                    break;
            }
            // ACTION_POINTER_UP: have not found the solution yet
            isDrag = true;

        }

        return true;
    }

//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        // has a problem when one of the point performs ACTION_UP (i.e. ACTION_POINTER_UP)
//        float x = 0, y = 0;
//        // init the touching point
//        final int pointerCount = event.getPointerCount();
//        // get the number of touching point
//
//        lastPointCount = pointerCount;
//        Log.e("countpoints", "COUNT " + lastPointCount);
//        Log.e("Drag beginning", "can be drag? "+isDrag);
//        x = event.getX();
//        y = event.getY();
//        Point begin = new Point();
//        Point middle = new Point();
//
//        if(mode == 1){
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    mode = 1;
//                    lastX = event.getX();
//                    lastY = event.getY();
//                    break;
//
//                case MotionEvent.ACTION_POINTER_DOWN:
//                    mode = 2;
//                    Log.e("ACTION", "ACTION_POINTER_DOWN");
//                    break;
//
//                case MotionEvent.ACTION_MOVE:
//                    float dx = x - lastX;
//                    float dy = y - lastY;
////                    Log.e("ACTION", "ACTION_MOVE" + dx);
////                    Log.e("ACTION", "ACTION_MOVE" + dy);
//                    matrix.postTranslate(dx, dy);
//                    setImageMatrix(matrix);
//                    lastX = x;
//                    lastY = y;
//
//                    // initialize lastX and lastY
//                    break;
//
//                case MotionEvent.ACTION_UP:
//                case MotionEvent.ACTION_CANCEL:
//                    mode = 1;
//                    Log.e("ACTION", "ACTION_UP");
//                    // when up or cancel lastPointCount becomes 0
//                    break;
//
//            }
//        }else if(mode == 2){
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_MOVE:
//                    scaleGestureDetector.onTouchEvent(event);
//                    Log.e("ACTION", "ACTION_MOVE" + scaleGestureDetector.getPreviousSpan());
//                    Log.e("ACTION", "ACTION_MOVE" + scaleGestureDetector.getCurrentSpan());
//                    Log.e("ACTION", "finger" + event.getPointerCount());
//                    // initialize lastX and lastY
//                    break;
//                case MotionEvent.ACTION_POINTER_UP:
//                    mode = 1;
//                    Log.e("ACTION", "ACTION_POINTER_UP");
//                    break;
//            }
//        }else{
//            mode = 1;
//        }
//
//
//        return true;
//    }

    @Override
    public void onGlobalLayout() {
        // center the image and initially zoom image

        if(isLayout){
            Drawable drawable = getDrawable();
            if(drawable == null)
                return;

            int WindowWidth = getWidth();
            int WindowHeight = getHeight();
            // get the width and height of ImageView

            int drawableWidth = drawable.getIntrinsicWidth();
            int drawableHeight = drawable.getIntrinsicHeight();
            // get the width and height of drawable

            float scale = 1.0f;
            if(drawableWidth > WindowWidth && drawableHeight <= WindowHeight)
                scale = WindowWidth * 1.0f / drawableWidth;

            if(drawableHeight > WindowHeight && drawableWidth <= WindowHeight)
                scale = WindowHeight * 1.0f/ drawableHeight;

            if(drawableWidth > WindowWidth && drawableHeight > WindowHeight)
                scale = Math.min(WindowHeight * 1.0f / drawableHeight, WindowWidth * 1.0f / drawableWidth);

            initScale = scale;
            // set initial scale

            matrix.postTranslate((WindowWidth - drawableWidth)/2, (WindowHeight - drawableHeight)/2);
            matrix.postScale(scale, scale, getWidth()/2, getHeight()/2);
            // zoom the image

            setImageMatrix(matrix);
            // set the image

            isLayout = false;
        }

    }

    public float getScale(){
        matrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }// get the scale of matrix (MSCALE_Y and MSCALE_X are the same)

    public void isMoving(boolean moving){
        if(moving == true){

        }
    }



//    private RectF getMatrixRectF(){
//        RectF rectF = new RectF();
//        Matrix matrix = this.matrix;
//        Drawable drawable = getDrawable();
//        if(drawable != null){
//            rectF.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
//            matrix.mapRect(rectF);
//        }
//        return rectF;
//    }// get the image Rect by using the matrix of this image

//    private void init(){
//        int index = 0;
//        float bmWidth = bitmap.getWidth();
//        float bmHeight = bitmap.getHeight();
//
//        for (int i = 0; i < HEIGHT + 1; i++) {
//            float fy = bmHeight * i / HEIGHT;
//            for (int j = 0; j < WIDTH + 1; j++) {
//                float fx = bmWidth * j / WIDTH;
//                //X轴坐标 放在偶数位
//                verts[index * 2] = fx;
//                orig[index * 2] = verts[index * 2];
//                //Y轴坐标 放在奇数位
//                verts[index * 2 + 1] = fy;
//                orig[index * 2 + 1] = verts[index * 2 + 1];
//                index += 1;
//            }
//        }
//    }

//    private void warp(float startX, float startY, float endX, float endY) {
//
//        //计算拖动距离
//        float ddPull = (endX - startX) * (endX - startX) + (endY - startY) * (endY - startY);
//        float dPull = (float) Math.sqrt(ddPull);
//        //文献中提到的算法，并不能很好的实现拖动距离 MC 越大变形效果越明显的功能，下面这行代码则是我对该算法的优化
//        dPull = screenWidth - dPull >= 0.0001f ? screenWidth - dPull : 0.0001f;
//
//        for (int i = 0; i < COUNT * 2; i += 2) {
//            //计算每个坐标点与触摸点之间的距离
//            float dx = verts[i] - startX;
//            float dy = verts[i + 1] - startY;
//            float dd = dx * dx + dy * dy;
//            float d = (float) Math.sqrt(dd);
//
//            //文献中提到的算法同样不能实现只有圆形选区内的图像才进行变形的功能，这里需要做一个距离的判断
//            if (d < r) {
//                //变形系数，扭曲度
//                double e = (r * r - dd) * (r * r - dd) / ((r * r - dd + dPull * dPull) * (r * r - dd + dPull * dPull));
//                double pullX = e * (endX - startX);
//                double pullY = e * (endY - startY);
//                verts[i] = (float) (verts[i] + pullX);
//                verts[i + 1] = (float) (verts[i + 1] + pullY);
//            }
//            Log.e("draw", "warp: " +verts[i] );
//        }
//
////        invalidate();
//    }

}
