package ivy.kookkai.debugview;

import ivy.kookkai.Network;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraInterface extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder mHolder;
	private Camera mCamera;

	public int counter = 0;
	public int frameWidth = 0;
	public int frameHeight = 0;

	private byte[] loadBuffer;
	private byte[] readyBuffer;
	private int colorStartIndex = 0;
	public byte[] cbcrBuffer;
	public byte[] yBuffer;
	public static int previewformat;

	// private CapImage capImage;

	public CameraInterface(Context context) {
		super(context);

		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		mCamera = Camera.open();
		Camera.Parameters para = mCamera.getParameters();
		CameraInterface.previewformat = para.getPreviewFormat();
		
		
//		para.setAutoExposureLock(true);
//		para.setAutoWhiteBalanceLock(true);		
		para.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);

		Size s = para.getPreviewSize();
		
		mCamera.setParameters(para);

		Log.d("ivy_debug", "h" + s.height + "w" + s.width);

		frameHeight = s.height;//height = 480
		frameWidth = s.width;// width = 640
		
		loadBuffer = new byte[frameWidth * frameHeight * 3 / 2];  
		readyBuffer = new byte[frameWidth * frameHeight * 3 / 2]; 
		cbcrBuffer = new byte[frameWidth * frameHeight / 2];
		yBuffer = new byte[frameWidth * frameHeight];
		colorStartIndex = frameWidth * frameHeight;

	}

	public void surfaceCreated(SurfaceHolder holder) {

		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.setDisplayOrientation(90);
			mCamera.addCallbackBuffer(loadBuffer);
			mCamera.setPreviewCallbackWithBuffer(new onCapture());
			mCamera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		try {
			mCamera.stopPreview();
			mCamera.release();
		} catch (Exception e) {

		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

	}
	
	public byte[] getHSV() {
		System.arraycopy(readyBuffer, colorStartIndex, cbcrBuffer, 0, cbcrBuffer.length);
		System.arraycopy(readyBuffer, 0, Network.imgYUV, 0, readyBuffer.length);
		
		return cbcrBuffer;
	}

	public byte[] getCbCr() {
		System.arraycopy(readyBuffer, colorStartIndex, cbcrBuffer, 0, cbcrBuffer.length);
		System.arraycopy(readyBuffer, 0, Network.imgYUV, 0, readyBuffer.length);
		// Network.imgYUV = readyBuffer;
		// Network.imgUV = cbcrBuffer;
		// Network.createJpeg();
		return cbcrBuffer;
	}
	
	public byte[] getYPrime(){
		System.arraycopy(readyBuffer, 0, yBuffer, 0, yBuffer.length);
		///System.arraycopy(readyBuffer, 0, Network.imgYUV, 0, readyBuffer.length);// because already done once
		return yBuffer;
	}

//	/**
//	 * Converts YUV420 NV21 to RGB8888
//	 * 
//	 * @param data byte array on YUV420 NV21 format.
//	 * @param width pixels width
//	 * @param height pixels height
//	 * @return a RGB8888 pixels int array. Where each int is a pixels ARGB. 
//	 */
//	
//	public static int[] convertYUV420_NV21toRGB8888(byte [] data, int width, int height) {
//	    int size = width*height;
//	    int offset = size;
//	    int[] pixels = new int[size];
//	    int u, v, y1, y2, y3, y4;
//
//	    // i percorre os Y and the final pixels
//	    // k percorre os pixles U e V
//	    for(int i=0, k=0; i < size; i+=2, k+=2) {
//	        y1 = data[i  ]&0xff;
//	        y2 = data[i+1]&0xff;
//	        y3 = data[width+i  ]&0xff;
//	        y4 = data[width+i+1]&0xff;
//
//	        u = data[offset+k  ]&0xff;
//	        v = data[offset+k+1]&0xff;
//	        u = u-128;
//	        v = v-128;
//
//	        pixels[i  ] = convertYUVtoRGB(y1, u, v);
//	        pixels[i+1] = convertYUVtoRGB(y2, u, v);
//	        pixels[width+i  ] = convertYUVtoRGB(y3, u, v);
//	        pixels[width+i+1] = convertYUVtoRGB(y4, u, v);
//
//	        if (i!=0 && (i+2)%width==0)
//	            i+=width;
//	    }
//
//	    return pixels;
//	}
//
//	private static int convertYUVtoRGB(int y, int u, int v) {
//	    int r,g,b;
//
//	    r = y + (int)1.402f*v;
//	    g = y - (int)(0.344f*u +0.714f*v);
//	    b = y + (int)1.772f*u;
//	    r = r>255? 255 : r<0 ? 0 : r;
//	    g = g>255? 255 : g<0 ? 0 : g;
//	    b = b>255? 255 : b<0 ? 0 : b;
//	    return 0xff000000 | (b<<16) | (g<<8) | r;
//	}
	
	private class onCapture implements PreviewCallback {
		public void onPreviewFrame(byte[] data, Camera camera) {

			// swap load and ready buffer
			// the input byte[] data is loadBuffer which we add to the queue
			// before
			mCamera.addCallbackBuffer(readyBuffer);
			readyBuffer = data;

		}
	}
}
