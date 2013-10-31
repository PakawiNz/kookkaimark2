package ivy.kookkai.debugview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ivy.kookkai.localization.Humanoid;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class FieldView extends View {
	//assume field 6*4
	
	public static final int FIELD_LENGTH = 600;
	public static final int FIELD_WIDTH = 400;
	public static final int GOAL_DEPTH = 50;
	public static final int GOAL_WIDTH = 150;
	public static final int GOAL_AREA_LENGTH = 60;
	public static final int GOAL_AREA_WIDTH = 220;
	public static final int PENALTY_MARK_DISTANCE = 180;
	public static final int CENTER_CIRCLE_DIAMETER = 120;
	public static final int BORDER_STRIP_WIDTH = 70;
//	public static final int LINE_WEIGHT = 5;
	
	public static final int TOTALLENGTH=2*BORDER_STRIP_WIDTH+FIELD_LENGTH;
	public static final int TOTALWIDTH=2*BORDER_STRIP_WIDTH+FIELD_WIDTH;
	
	Bitmap img;
	Canvas imgCanvas;
	Paint pStroke;

	public FieldView(Context context) {
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
		reset();
	}
	
	public void drawFieldLine(){
		// Not so scaled field
	//field lines
		drawRect(new Rect(getXPix(BORDER_STRIP_WIDTH), getYPix(BORDER_STRIP_WIDTH),
				getXPix(BORDER_STRIP_WIDTH+FIELD_LENGTH), getYPix(BORDER_STRIP_WIDTH+FIELD_WIDTH)));
	//center line
		drawLine(getXPix(TOTALLENGTH/2), getYPix(BORDER_STRIP_WIDTH)
				, getXPix(TOTALLENGTH/2), getYPix(TOTALWIDTH-BORDER_STRIP_WIDTH));
	//backend goal line
		//left backend goal line
		drawRect(new Rect(getXPix(BORDER_STRIP_WIDTH-GOAL_DEPTH),getYPix(TOTALWIDTH/2-GOAL_WIDTH/2),
				getXPix(BORDER_STRIP_WIDTH),getYPix(TOTALWIDTH/2+GOAL_WIDTH/2)));
		//right backend goal line
		drawRect(new Rect(getXPix(TOTALLENGTH-BORDER_STRIP_WIDTH),getYPix(TOTALWIDTH/2-GOAL_WIDTH/2),
				getXPix(TOTALLENGTH-BORDER_STRIP_WIDTH+GOAL_DEPTH),getYPix(TOTALWIDTH/2+GOAL_WIDTH/2)));
	//penalty goal area
		//left penalty goal area
		drawRect(new Rect(getXPix(BORDER_STRIP_WIDTH),getYPix(TOTALWIDTH/2-GOAL_AREA_WIDTH/2),
				getXPix(BORDER_STRIP_WIDTH+GOAL_AREA_LENGTH),getYPix(TOTALWIDTH/2+GOAL_AREA_WIDTH/2)));
		//right penalty goal area
		drawRect(new Rect(getXPix(TOTALLENGTH-BORDER_STRIP_WIDTH-GOAL_AREA_LENGTH),getYPix(TOTALWIDTH/2-GOAL_AREA_WIDTH/2),
				getXPix(TOTALLENGTH-BORDER_STRIP_WIDTH),getYPix(TOTALWIDTH/2+GOAL_AREA_WIDTH/2)));
	//center circle
		drawCircle(getXPix(TOTALLENGTH/2),getYPix(TOTALWIDTH/2),getXPix(CENTER_CIRCLE_DIAMETER/2),Color.WHITE);
		
	//penalty marks
		//left horizontal
		drawLine(getXPix(BORDER_STRIP_WIDTH+PENALTY_MARK_DISTANCE-10),getYPix(TOTALWIDTH/2),
				getXPix(BORDER_STRIP_WIDTH+PENALTY_MARK_DISTANCE+10),getYPix(TOTALWIDTH/2));
		//left vertical
		drawLine(getXPix(BORDER_STRIP_WIDTH+PENALTY_MARK_DISTANCE),getYPix(TOTALWIDTH/2-10),
				getXPix(BORDER_STRIP_WIDTH+PENALTY_MARK_DISTANCE),getYPix(TOTALWIDTH/2+10));
		//right horizontal
		drawLine(getXPix(TOTALLENGTH-PENALTY_MARK_DISTANCE-BORDER_STRIP_WIDTH-10),getYPix(TOTALWIDTH/2),
				getXPix(TOTALLENGTH-PENALTY_MARK_DISTANCE-BORDER_STRIP_WIDTH+10),getYPix(TOTALWIDTH/2));
		//right vertical
		drawLine(getXPix(TOTALLENGTH-PENALTY_MARK_DISTANCE-BORDER_STRIP_WIDTH),getYPix(TOTALWIDTH/2-10),
				getXPix(TOTALLENGTH-PENALTY_MARK_DISTANCE-BORDER_STRIP_WIDTH),getYPix(TOTALWIDTH/2+10));
	//center mark
		//horizontal
		drawLine(getXPix(TOTALLENGTH/2-10),getYPix(TOTALWIDTH/2),
				getXPix(TOTALLENGTH/2+10),getYPix(TOTALWIDTH/2));
		//vertical is unneccessary
	}
	public void print_weight_map(){
		for(int i=0;i<Humanoid.WEIGHT_MAP.length;i++){
			for(int j=0;j<Humanoid.WEIGHT_MAP[0].length;j++){
				int a = Color.argb((int)(Humanoid.WEIGHT_MAP[i][j]*255), 255, 255, 255);
				if(Humanoid.WEIGHT_MAP[i][j]!=0)
					drawPixel(getXPix(j), getYPix(i), a);
			}
		}
	}
	private int getXPix(int x){
		return x*getWidth()/TOTALLENGTH;
	}
	private int getYPix(int y){
		return y*getHeight()/TOTALWIDTH;
	}
	public void fill(int color) {
		imgCanvas.drawColor(color);
	}

	public void reset() {
		img.eraseColor(0x8f00ff00);//Color.GREEN);
//		drawFieldLine();
		print_weight_map();
	}

	public void drawRect(Rect r) {
		pStroke.setColor(Color.WHITE);
		imgCanvas.drawRect(r, pStroke);
	}
	public void drawLine(int startX,int startY,int stopX,int stopY){
		pStroke.setColor(Color.WHITE);
		imgCanvas.drawLine(startX, startY, stopX, stopY, pStroke);
	}
	public void drawCircle(int cx,int cy,int radius, int color) {
		pStroke.setColor(color);
		imgCanvas.drawCircle(cx, cy, radius, pStroke);
	}

	public void drawPixel(int x, int y, int color) {
		img.setPixel(x, y, color);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (img != null)
			canvas.drawBitmap(img, 0, 0, null);

	}
}
