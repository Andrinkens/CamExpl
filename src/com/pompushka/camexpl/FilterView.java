package com.pompushka.camexpl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class FilterView extends SurfaceView implements SurfaceHolder.Callback{
	SurfaceHolder sh;
	Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	Bitmap previewBmp;
	Matrix matrix;
	
    private String TAG = "CameraEx";
	
	public FilterView(Context context) {
		super(context);
		sh = getHolder();
		getHolder().addCallback(this);
	}
	
	public void setPreviewBmp(Bitmap bmp){
		if (bmp == null) return;
		//previewBmp = bmp;
		Canvas canvas = sh.lockCanvas();
        canvas.drawBitmap(bmp, matrix, null);
        sh.unlockCanvasAndPost(canvas);

		Log.d(TAG, "Surface bmp obtain");
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event) {/*
		Canvas canvas = surfaceHolder.lockCanvas();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        canvas.drawRect(30, 30, 80, 80, paint);
        paint.setStrokeWidth(0);
        paint.setColor(Color.CYAN);
        canvas.drawRect(33, 60, 77, 77, paint );
        paint.setColor(Color.YELLOW);
        canvas.drawRect(33, 33, 77, 60, paint );
        surfaceHolder.unlockCanvasAndPost(canvas);*/
        return true;
    }



	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Log.d(TAG, "Surface changed");
		matrix = new Matrix();
		matrix.postScale(-1.0f, 1.2f);
		matrix.postTranslate(650.0f, 0.0f);
		/*
		Canvas canvas = arg0.lockCanvas();
        if (previewBmp != null) canvas.drawBitmap(previewBmp, matrix, null);
        arg0.unlockCanvasAndPost(canvas);
		Log.d(TAG, "Surface changed");*/
	}



	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		/*
		Canvas canvas = arg0.lockCanvas();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        canvas.drawRect(30, 30, 80, 80, paint);
        paint.setStrokeWidth(0);
        paint.setColor(Color.CYAN);
        canvas.drawRect(33, 60, 77, 77, paint );
        paint.setColor(Color.YELLOW);
        canvas.drawRect(33, 33, 77, 60, paint );
        arg0.unlockCanvasAndPost(canvas);*/
		
		Log.d(TAG, "Surface created");
	}



	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
