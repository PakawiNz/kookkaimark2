package ivy.kookkai.debugview;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import ivy.kookkai.MainlLoop;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.localization.Ball;
import ivy.kookkai.localization.Humanoid;
import ivy.kookkai.localization.Localization;
import ivy.kookkai.localization.Particle;
import ivy.kookkai.vision.Blob;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class UndistortView extends View{
	public static final int VIEWHEIGHT = 160;
	public static final int VIEWWIDTH = 120;
	
	private static final String remap_x_file  = "/KorKai/remap_x_div"+GlobalVar.REMAP_FACTOR+".txt";
    private static final String remap_y_file  = "/KorKai/remap_y_div"+GlobalVar.REMAP_FACTOR+".txt";
    
	Bitmap img;
	Canvas imgCanvas;
	Paint pStroke;
	
	public int[][] remapped_x,remapped_y;
	
	private CameraInterface cameraInterface;
	boolean initialized = false;
	
	public UndistortView(Context context) {
		super(context);
		pStroke = new Paint();
		pStroke.setColor(Color.argb(255, 255, 255, 0));// yellow with 100% opaque
		pStroke.setStyle(Paint.Style.STROKE);
		pStroke.setStrokeWidth(1);
		//initialize();
	}
	public void initialize(CameraInterface _cameraInterface){
		cameraInterface = _cameraInterface;
		remapped_x = new int[GlobalVar.FRAME_HEIGHT/GlobalVar.REMAP_FACTOR]
				[GlobalVar.FRAME_WIDTH/GlobalVar.REMAP_FACTOR];
		remapped_y = new int[GlobalVar.FRAME_HEIGHT/GlobalVar.REMAP_FACTOR]
				[GlobalVar.FRAME_WIDTH/GlobalVar.REMAP_FACTOR];
		Log.d("write","remapped x row:"+remapped_x[0].length);
		Log.d("write","remapped x col:"+remapped_y.length);
		try {
			readRemapArray();
		} catch (Exception e) {
			Log.d("write","write incomplete");
		}
		initialized = true;
	}
	public void readRemapArray() throws Exception {
		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		String remap_x_name = path + remap_x_file;
		String remap_y_name = path + remap_y_file;
		File state_remapx = new File(remap_x_name);
		
		if (!state_remapx.exists()) {
			Log.d("write","file doesn't exist");
			throw new Exception("'" + state_remapx + "' doesn't exist");		
		} else {
			BufferedReader fr1,fr2;
			
			fr1 = new BufferedReader(new FileReader(remap_x_name));
			fr2 = new BufferedReader(new FileReader(remap_y_name));
			
			String line1,line2;
			int i=0;
			while ((line1 = fr1.readLine()) != null && (line2 = fr2.readLine()) != null) {
				String tokens1[] = line1.trim().split(" ");	
				Log.d("write","tokens1 length:"+tokens1.length);
				String tokens2[] = line2.trim().split(" ");
				Log.d("write","tokens2 length:"+tokens2.length);
				try {				
					for(int j=0;j<tokens1.length;j++){
						remapped_x[j][i] = Integer.parseInt(tokens1[j]);					
					}
				} catch (Exception e) {
					Log.d("write","unable to load remapped_x");
				}
				try {				
					for(int j=0;j<tokens2.length;j++){
						remapped_y[j][i] = Integer.parseInt(tokens2[j]);
					}
				} catch (Exception e) {
					Log.d("write","unable to load remapped_y");
				}
				i++;
			}
			Log.d("write","close remapped_x");
			fr1.close();	
			fr2.close();	
			Log.d("write","closed");
		}
	}
	
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if(initialized){
			int width = Math.abs(left - right);
			int height = Math.abs(top - bottom);
			img = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			imgCanvas = new Canvas(img);
			reset();
		}
	}
	private void drawObjects(){
		if(initialized && MainlLoop.DEBUGMODE){
			byte[] yPrime = cameraInterface.getYPrime();
			Log.d("write","drawing objects");
			long timestamp =System.currentTimeMillis();
			for(int i=0;i<cameraInterface.frameWidth/GlobalVar.REMAP_FACTOR;i++){
				for(int j=0;j<cameraInterface.frameHeight/GlobalVar.REMAP_FACTOR;j++){
					int coord1=getCoordinate(remapped_x[i][j], 
							remapped_y[i][j]);
//					int coord2=getCoordinate(i*GlobalVar.REMAP_FACTOR, j*GlobalVar.REMAP_FACTOR);
					int temp = (int)yPrime[coord1]&0xff;
					//TODO don't forget to prioritize other color before detect white
					if(temp>Blob.WHITE_THRESHOLD){
						pStroke.setColor(Color.argb(temp, 255, 255, 255));
						int x=getXPix(j*GlobalVar.REMAP_FACTOR);
						int y=getYPix(i*GlobalVar.REMAP_FACTOR);
						imgCanvas.drawPoint(x,y, pStroke);
					}
					
					
				}
			}
			Log.d("time",""+(System.currentTimeMillis()-timestamp));
		}
	}
	
	private int getCoordinate(int x,int y){
		return (479-y)*640+x;
	}
	private int getXPix(int x){
		return x*getWidth()/cameraInterface.frameHeight;//takenote: the frame is swap!
	}
	private int getYPix(int y){
		return y*getHeight()/cameraInterface.frameWidth;
	}
	public void reset() {
		img.eraseColor(Color.BLACK);
		drawObjects();
		drawRect(new Rect(1,1,getWidth()-1,getHeight()-1),Color.CYAN);
		
		///TODO TEMP writing output
		boolean saveimage=false;
		if(saveimage){
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			img.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
	
			//you can create a new file name "test.jpg" in sdcard folder.
			File f = new File(Environment.getExternalStorageDirectory()
			                        + File.separator + "test.jpg");
			try {
				f.createNewFile();
				//write the bytes in file
				FileOutputStream fo = new FileOutputStream(f);
				fo.write(bytes.toByteArray());
	
				// remember close de FileOutput
				fo.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		///ENDTEMP
	}
	public void drawRect(Rect r, int color) {
		pStroke.setColor(color);
		imgCanvas.drawRect(r, pStroke);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		if (img != null)
			canvas.drawBitmap(img, 0, 0, null);

	}
	
}
