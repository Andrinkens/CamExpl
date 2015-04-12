package com.pompushka.camexpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback{
    private SurfaceHolder mHolder;
    private Camera mCamera;
    
    private String TAG = "CameraEx";
    
  //This variable is responsible for getting and setting the camera settings  
    private Parameters parameters;  
    //this variable stores the camera preview size   
    private Size previewSize;  
    //this array stores the pixels as hexadecimal pairs   
    private byte[] pixelsB;
    private int[] pixelsI;
    
    Bitmap.Config conf;
    Bitmap previewBitmap;
    
    private FilterView fW;

    public CameraPreview(Context context, Camera camera, FilterView view) {
        super(context);
        mCamera = camera;
        
        fW = view;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            parameters = mCamera.getParameters();
            parameters.getSupportedPreviewFormats().toString();
            mCamera.setParameters(parameters);

            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
            
            parameters = mCamera.getParameters();  
            previewSize = parameters.getPreviewSize();  
            pixelsB = new byte[previewSize.width * previewSize.height * 4];
            pixelsI = new int[previewSize.width * previewSize.height];
            
    		conf = Bitmap.Config.ARGB_8888; // see other conf types
    		previewBitmap = Bitmap.createBitmap(640, 480, conf);
            
            Log.d(TAG, parameters.getSupportedPreviewFormats().toString());
            
            
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            mCamera.release();  
            mCamera = null; 
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    	mCamera.stopPreview();  
        mCamera.release();  
        mCamera = null; 
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        try {
        	parameters.setPreviewSize(w, h);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

	@Override
	public void onPreviewFrame(byte[] arg0, Camera arg1) {
		decodeYUV420SP(pixelsB, arg0, previewSize.width,  previewSize.height);
		//decodeYUV420SP(pixelsI, arg0, previewSize.width,  previewSize.height);
		/*
		YuvImage yuvImage = new YuvImage(arg0, ImageFormat.NV21, previewSize.width, previewSize.height, null);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
		yuvImage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, baos);
		byte [] imageData = baos.toByteArray();
		Bitmap previewBitmap1 = BitmapFactory.decodeByteArray(imageData , 0, imageData.length);
		*/
		//int pBlength = previewBitmap.getByteCount();
		//ByteBuffer bytes = ByteBuffer.allocate(pBlength);
		//previewBitmap.copyPixelsToBuffer(bytes);
		//byte[] rfData = RobertsFilter(bytes.array());
		//previewBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(rfData));
		/*
		Bitmap previewBitmap2;
		BitmapFactory.Options options = new BitmapFactory.Options();
		//options.inMutable = true;
		options.
		previewBitmap2 = BitmapFactory.decodeByteArray(pixels, 0, pixels.length, options);
		*/
		
		IntBuffer intBuf =	ByteBuffer.wrap(pixelsB).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
		intBuf.get(pixelsI);
		
		previewBitmap = Bitmap.createBitmap(pixelsI, previewSize.width, previewSize.height, conf);
		//previewBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(pixels));

		
		fW.setPreviewBmp(previewBitmap);
		
		//Log.d(TAG, "The top right pixel has the following RGB (hexadecimal) values:"  
        //         +Integer.toHexString(previewBitmap.getPixel(0, 0)));
	}
	
	public int[] convert(byte buf[]) {
		   int intArr[] = new int[buf.length / 4];
		   int offset = 0;
		   for(int i = 0; i < intArr.length; i++) {
		      intArr[i] = (buf[3 + offset] & 0xFF) | ((buf[2 + offset] & 0xFF) << 8) |
		                  ((buf[1 + offset] & 0xFF) << 16) | ((buf[0 + offset] & 0xFF) << 24);  
		   offset += 4;
		   }
		   return intArr;
		}
	
	private byte[] RobertsFilter(byte[] dataIn){
		byte[] dataOut = new byte[dataIn.length];
		byte temp1,temp2,temp3 = 0;
		byte sR = 1;//shift Red
		byte sG = 2;//
		byte sB = 3;//
		
		for (int i=0;i<previewSize.width*(previewSize.height-1)-1;i++){

			dataOut[i<<2]=(byte) 0xff;
			
			temp1 = (byte) (dataIn[sR + (i<<2)] - dataIn[sR + ((i+1)<<2) + (previewSize.width<<2)]);
			temp2 = (byte) (dataIn[sR + ((i+1)<<2)] - dataIn[sR + (i<<2) + (previewSize.width<<2)]);
			temp3 = (byte) Math.sqrt(temp1*temp1 + temp2*temp2);
			dataOut[sR+(i<<2)]=(byte) (temp3);
			
			temp1 = (byte) (dataIn[sG + (i<<2)] - dataIn[sG + ((i+1)<<2) + (previewSize.width<<2)]);
			temp2 = (byte) (dataIn[sG + ((i+1)<<2)] - dataIn[sG + (i<<2) + (previewSize.width<<2)]);
			temp3 = (byte) Math.sqrt(temp1*temp1 + temp2*temp2);
			dataOut[sG+(i<<2)]=(byte) (temp3);
			
			temp1 = (byte) (dataIn[sB + (i<<2)] - dataIn[sB + ((i+1)<<2) + (previewSize.width<<2)]);
			temp2 = (byte) (dataIn[sB + ((i+1)<<2)] - dataIn[sB + (i<<2) + (previewSize.width<<2)]);
			temp3 = (byte) Math.sqrt(temp1*temp1 + temp2*temp2);
			dataOut[sB+(i<<2)]=(byte) (temp3);
		}
		return dataOut;
	}
	
	private int[] RobertsFilter(int[] dataIn){//not right works
		int[] dataOut = new int[dataIn.length];
		byte[][] R = new byte[2][2];
		byte[][] G = new byte[2][2];
		byte[][] B = new byte[2][2];
		byte r,g,b;
		
		for (int i=0;i<previewSize.width*(previewSize.height-1)-1;i++){
			
				R[0][0] = (byte) ((dataIn[i] & 0x00ff0000)>>16);
				G[0][0] = (byte) ((dataIn[i] & 0x0000ff00)>>8);
				B[0][0] = (byte) (dataIn[i] & 0x000000ff);
				
				R[1][1] = (byte) ((dataIn[i+previewSize.width+1] & 0x00ff0000)>>16);
				G[1][1] = (byte) ((dataIn[i+previewSize.width+1] & 0x0000ff00)>>8);
				B[1][1] = (byte) (dataIn[i+previewSize.width+1] & 0x000000ff);
				
				R[0][1] = (byte) ((dataIn[i+previewSize.width] & 0x00ff0000)>>16);
				G[0][1] = (byte) ((dataIn[i+previewSize.width] & 0x0000ff00)>>8);
				B[0][1] = (byte) (dataIn[i+previewSize.width] & 0x000000ff);
				
				R[1][0] = (byte) ((dataIn[i+1] & 0x00ff0000)>>16);
				G[1][0] = (byte) ((dataIn[i+1] & 0x0000ff00)>>8);
				B[1][0] = (byte) (dataIn[i+1] & 0x000000ff);
				
				r = (byte) Math.sqrt((R[0][0] - R[1][1])*(R[0][0] - R[1][1]) + 
						(R[1][0] - R[0][1])*(R[1][0] - R[0][1]));
				
				g = (byte) Math.sqrt((G[0][0] - G[1][1])*(G[0][0] - G[1][1]) + 
						(G[1][0] - G[0][1])*(G[1][0] - G[0][1]));
				
				b = (byte) Math.sqrt((B[0][0] - B[1][1])*(B[0][0] - B[1][1]) + 
						(B[1][0] - B[0][1])*(B[1][0] - B[0][1]));
				
				dataOut[i] = 0xff000000 | (r<<16)&0xff0000 | (g<<8)&0xff00 | b & 0xff;
		}
		return dataOut;
	}
	
	void decodeYUV420SP(byte[] rgb, byte[] yuv420sp, int width, int height) {  
        
        final int frameSize = width * height;  

        for (int j = 0, yp = 0; j < height; j++) {       int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;  
          for (int i = 0; i < width; i++, yp++) {  
            int y = (0xff & ((int) yuv420sp[yp])) - 16;  
            if (y < 0)  
              y = 0;  
            if ((i & 1) == 0) {  
              v = (0xff & yuv420sp[uvp++]) - 128;  
              u = (0xff & yuv420sp[uvp++]) - 128;  
            }  

            int y1192 = 1192 * y;  
            int r = (y1192 + 1634 * v);  
            int g = (y1192 - 833 * v - 400 * u);  
            int b = (y1192 + 2066 * u);  

            if (r < 0)                  r = 0;               else if (r > 262143)  
               r = 262143;  
            if (g < 0)                  g = 0;               else if (g > 262143)  
               g = 262143;  
            if (b < 0)                  b = 0;               else if (b > 262143)  
               b = 262143;  

            //rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            rgb[4*yp] = (byte) 0xff;
            rgb[4*yp+1] = (byte) (r >> 10);
            rgb[4*yp+2] = (byte) (g >> 10);
            rgb[4*yp+3] = (byte) (b >> 10);
          }  
        }  
      }
	
	void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {  
        
        final int frameSize = width * height;  

        for (int j = 0, yp = 0; j < height; j++) {       int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;  
          for (int i = 0; i < width; i++, yp++) {  
            int y = (0xff & ((int) yuv420sp[yp])) - 16;  
            if (y < 0)  
              y = 0;  
            if ((i & 1) == 0) {  
              v = (0xff & yuv420sp[uvp++]) - 128;  
              u = (0xff & yuv420sp[uvp++]) - 128;  
            }  

            int y1192 = 1192 * y;  
            int r = (y1192 + 1634 * v);  
            int g = (y1192 - 833 * v - 400 * u);  
            int b = (y1192 + 2066 * u);  

            if (r < 0)                  r = 0;               else if (r > 262143)  
               r = 262143;  
            if (g < 0)                  g = 0;               else if (g > 262143)  
               g = 262143;  
            if (b < 0)                  b = 0;               else if (b > 262143)  
               b = 262143;  

            rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);  
          }  
        }  
      }  
}