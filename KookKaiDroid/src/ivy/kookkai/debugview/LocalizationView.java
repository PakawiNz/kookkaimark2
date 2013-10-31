package ivy.kookkai.debugview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ivy.kookkai.MainlLoop;
import ivy.kookkai.localization.Ball;
import ivy.kookkai.localization.Humanoid;
import ivy.kookkai.localization.Particle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class LocalizationView extends View {
	public static final int BALL_DIAMETER = 30;// (actually 6.7 cm only)

	public static int OUR_PLAYERS_NUM = 1;
	public static int OPP_PLAYERS_NUM = 0;
	public static final int[] OUR_COLOR = { Color.MAGENTA, Color.MAGENTA,
			Color.MAGENTA };
	public static final int[] OPP_COLOR = { Color.CYAN, Color.CYAN, Color.CYAN };

	Bitmap img;
	Canvas imgCanvas;
	Paint pStroke;

	private Humanoid ourHumanoid, oppHumanoid;
	private Ball ball;
	boolean initialized = false;

	public LocalizationView(Context context) {
		super(context);
		pStroke = new Paint();
		pStroke.setColor(Color.WHITE);
		pStroke.setStyle(Paint.Style.STROKE);
		pStroke.setStrokeWidth(1);

		// initialize();
	}

	public void initialize(Humanoid ourHumanoid, Humanoid oppHumanoid, Ball ball) {
		this.ourHumanoid = ourHumanoid;// new Humanoid[OUR_PLAYERS_NUM];
		this.oppHumanoid = oppHumanoid;// new Humanoid[OPP_PLAYERS_NUM];
		/*
		 * for(int i=0;i<OUR_PLAYERS_NUM;i++){ ourHumanoid[i] = new Humanoid();
		 * } for(int i=0;i<OPP_PLAYERS_NUM;i++){ oppHumanoid[i] = new
		 * Humanoid(); }
		 */
		this.ball = ball;// ball = new Ball();
		initialized = true;
	}

	public void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		if (initialized) {
			int width = Math.abs(left - right);
			int height = Math.abs(top - bottom);
			img = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			imgCanvas = new Canvas(img);
			reset();
		}
	}

	private void drawObjects() {
		if(MainlLoop.DEBUGMODE){
			drawHumanoidParticles(ourHumanoid);
			drawBestParticle(ourHumanoid);
			drawBall();
		}
	}

	private void drawBall() {
		pStroke.setColor(Color.RED);// 0xffa500
		pStroke.setStyle(Paint.Style.FILL_AND_STROKE);
		imgCanvas.drawCircle(getXPix(ball.x), getYPix(ball.y),
				getXPix(BALL_DIAMETER / 2), pStroke);

		pStroke.setStyle(Paint.Style.STROKE);

	}

	private void drawHumanoidParticles(Humanoid h) {
		pStroke.setColor(OUR_COLOR[0]);
		for (int i = 0; i < h.mParticles.length; i++) {
			// screen resolution is limited
			int color = Color.argb((int) (255 * h.mParticles[i].getmWeight()),
					255, 0, 255);
			pStroke.setColor(color);
			int x = (int) h.mParticles[i].getmX();
			int y = (int) h.mParticles[i].getmY();
			double z = h.mParticles[i].getmZ();
			imgCanvas.drawCircle(getXPix(x), getYPix(y), 2, pStroke);
			pStroke.setColor(0xffff0000);
//			imgCanvas.drawLine(getXPix(x), getYPix(y),
//					getXPix((int) (x + 10 * Math.cos(z))),
//					getYPix((int) (y - 10 * Math.sin(z))), pStroke);
		}
	}

	private Particle drawBestParticle(Humanoid h) {
		Particle bestPar = h.getmBestParticle();

		if (bestPar == null)
			return null;

		Log.d("local", "best par x,y:" + (int) bestPar.getmX() + ","
				+ (int) bestPar.getmY() + "," + (int) bestPar.getmWeight());

		int stopX = getXPix((int) (bestPar.getmX() + 200 * Math.cos(bestPar
				.getmZ())));
		int stopY = getYPix((int) (bestPar.getmY() + 200 * Math.sin(bestPar
				.getmZ())));
		pStroke.setColor(Color.BLACK);
		imgCanvas.drawLine(getXPix((int) bestPar.getmX()),
				getYPix((int) bestPar.getmY()), stopX, stopY, pStroke);
		imgCanvas.drawCircle(getXPix((int) bestPar.getmX()),
				getYPix((int) bestPar.getmY()), 5, pStroke);
		pStroke.setColor(Color.CYAN);
		for (int i = 0; i < HomographyPointsView.mWhiteArea_bybin
				.getBinCount(); i++) {
			double data_mag = HomographyPointsView.mWhiteArea_bybin.bin_mag[i];
			double data_z = HomographyPointsView.mWhiteArea_bybin.bin_z[i];
			if (data_mag > 0) {
				int stop_x = getXPix((int) (bestPar.getmX() + data_mag
						* Math.cos(data_z + bestPar.getmZ())));
				int stop_y = getYPix((int) (bestPar.getmY() + data_mag
						* Math.sin(data_z + bestPar.getmZ())));
				imgCanvas.drawCircle(stop_x, stop_y, 1, pStroke);
				// imgCanvas.drawLine(getXPix((int) bestPar.getmX()),
				// getYPix((int) bestPar.getmY()), stop_x, stop_y, pStroke);
			}
		}
		return bestPar;
	}

	private int getXPix(int x) {
		return x * getWidth() / FieldView.TOTALLENGTH;
	}

	private int getYPix(int y) {
		return (FieldView.TOTALWIDTH - y) * getHeight() / FieldView.TOTALWIDTH;
	}

	int image_num = 0;
	public void reset() {
		img.eraseColor(0x00000000);
		drawObjects();
		drawRect(new Rect(1, 1, getWidth() - 1, getHeight() - 1), Color.CYAN);
		// TODO TEMP writing output
		boolean saveimage = false;
		if (saveimage) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			img.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
			// you can create a new file name "test.jpg" in sdcard folder.
			File f = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "localizepict" + image_num++ + ".jpg");
			try {
				f.createNewFile();
				// write the bytes in file
				FileOutputStream fo = new FileOutputStream(f);
				fo.write(bytes.toByteArray());
				// remember close de FileOutput
				fo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// /ENDTEMP
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
