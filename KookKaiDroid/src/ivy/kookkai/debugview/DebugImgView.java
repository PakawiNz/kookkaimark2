package ivy.kookkai.debugview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ivy.kookkai.data.GlobalVar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class DebugImgView extends View {

	Bitmap img;
	Canvas imgCanvas;
	Paint pStroke;
	private boolean drawcolor;

	public DebugImgView(Context context) {
		super(context);

		pStroke = new Paint();
		pStroke.setColor(Color.WHITE);
		pStroke.setStyle(Paint.Style.STROKE);
		pStroke.setStrokeWidth(1);
	}

	@Override
	public void onLayout(boolean changed, int left, int top, int right, int bottom) {

		int width = Math.abs(left - right);
		int height = Math.abs(top - bottom);
		
		img = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		imgCanvas = new Canvas(img);
		Log.d("ivy_debug", "wM " + width + " wH " + height);
		Log.d("debug_image","initialized by onLayout");
	}

	public void fill(int color) {
		imgCanvas.drawColor(color);
	}

	public void reset() {
		img.eraseColor(0);
		drawRect(new Rect(1,1,getWidth()-1,getHeight()-1),Color.CYAN);
		//TODO find out why can't move to mainlloop
//		for (int i = 0; i < GlobalVar.blobResult.size(); i++) {
//			drawRect(GlobalVar.blobResult.get(i).posRect, Color.GREEN);
//		}
//		for (int i = 0; i < GlobalVar.mergeResult.size(); i++) {
//			drawRect(GlobalVar.mergeResult.get(i).posRect, Color.BLUE);
//		}
		
	}

	public void drawRect(Rect r, int color) {
		pStroke.setColor(color);
		imgCanvas.drawRect(r, pStroke);
	}

	public void drawPixel(int x, int y, int color) {
		img.setPixel(x, y, color);
		// Log.d("ivy_debug", color+ " color ");
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (img != null)
			canvas.drawBitmap(img, 0, 0, null);

	}
	private int getXPix(int x){
		return x*getWidth()/FieldView.TOTALLENGTH;
	}
	private int getYPix(int y){
		return y*getHeight()/FieldView.TOTALWIDTH;
	}
}
