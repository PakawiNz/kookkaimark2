package ivy.kookkai.debugview;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
import android.graphics.drawable.shapes.ArcShape;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class HomographyPointsView extends View {
	public static final int VIEWHEIGHT = 160;
	public static final int VIEWWIDTH = 120;
	// Display in the scale of /10 mm = /100 cm. = /10000 meter
	public static final int X_MIN = -15000;
	public static final int X_MAX = 15000;
	public static final int Y_MIN = 0;
	public static final int Y_MAX = 40000;
	public static final int X_RANGE = X_MAX - X_MIN;
	public static final int Y_RANGE = Y_MAX - Y_MIN;

	// Parameters for read mode localization
	private static boolean WRITELOCALIZEDATA = false;// WARNING have only write
														// mode for bybin mode
	private static boolean READLOCALIZEDATA = false;// WARNING read and write
													// can't be both true
	private static final String SENSOR_DATA_RANGE_FILE = "/SensorData/sensordata_range.txt";
	private static final String SENSOR_DATA_HEADER_FILE = "/SensorData/sensordata_header.txt";
	private long timestamp = 0;

	public static final String PATH = Environment.getExternalStorageDirectory()
			.getAbsolutePath();
	private static final String sensor_range_name = PATH
			+ SENSOR_DATA_RANGE_FILE;
	private static final String sensor_header_name = PATH
			+ SENSOR_DATA_HEADER_FILE;
	private File state_range, state_header;
	private BufferedReader header_buffer, range_buffer;
	private double zLowerBound = 0;
	private double zUpperBound = 0;
	private int binCount = 0;

	private static final String HOMO_X_FILE = "/KorKai/homo_x_div"
			+ GlobalVar.REMAP_FACTOR + ".txt";
	private static final String HOMO_Y_FILE = "/KorKai/homo_y_div"
			+ GlobalVar.REMAP_FACTOR + ".txt";

	Bitmap img;
	Canvas imgCanvas;
	Paint pStroke;

	private int[][] remapped_x, remapped_y;
	private double[][] homo_z, homo_mag; // in the unit of radian and cm
	private int[][] homo_x, homo_y;
	public static PointRangeBin mWhiteArea_bybin;

	private CameraInterface cameraInterface;
	private UndistortView undistortView;
	boolean initialized = false;

	public HomographyPointsView(Context context) {
		super(context);
		pStroke = new Paint();
		pStroke.setColor(Color.argb(255, 255, 255, 0));// yellow with 100%
														// opaque
		pStroke.setStyle(Paint.Style.STROKE);
		pStroke.setStrokeWidth(1);
		// initialize();
	}

	public void initialize(CameraInterface _cameraInterface,
			UndistortView _undistortView) {
		cameraInterface = _cameraInterface;
		undistortView = _undistortView;
		remapped_x = undistortView.remapped_x;
		remapped_y = undistortView.remapped_y;
		mWhiteArea_bybin = new PointRangeBin();

		homo_x = new int[GlobalVar.FRAME_HEIGHT / GlobalVar.REMAP_FACTOR][GlobalVar.FRAME_WIDTH
				/ GlobalVar.REMAP_FACTOR];
		homo_y = new int[GlobalVar.FRAME_HEIGHT / GlobalVar.REMAP_FACTOR][GlobalVar.FRAME_WIDTH
				/ GlobalVar.REMAP_FACTOR];
		homo_z = new double[GlobalVar.FRAME_HEIGHT / GlobalVar.REMAP_FACTOR][GlobalVar.FRAME_WIDTH
				/ GlobalVar.REMAP_FACTOR];
		homo_mag = new double[GlobalVar.FRAME_HEIGHT / GlobalVar.REMAP_FACTOR][GlobalVar.FRAME_WIDTH
				/ GlobalVar.REMAP_FACTOR];

		try {
			readRemapArray();
		} catch (Exception e) {
			Log.d("homo", "write homo incomplete");
		}
		if (WRITELOCALIZEDATA) {
			try {
				initialize_write_data_files();
			} catch (Exception e) {
				Log.d("save_file", "failed to initialize_write_data_files");
			}
		} else if (READLOCALIZEDATA) {
			try {
				initialize_read_data_files();
			} catch (Exception e) {
				Log.d("save_file", "failed to initialize_read_data_files");
			}
		}
		initialized = true;
	}

	public void readRemapArray() throws Exception {
		String path = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		String homo_x_name = path + HOMO_X_FILE;
		String homo_y_name = path + HOMO_Y_FILE;
		File state_homo = new File(homo_x_name);
		int max_x = -100000, max_y = -100000;
		int min_x = 100000, min_y = 100000;
		double max_z = -100, min_z = 100;
		double max_mag = -2000, min_mag = 2000;

		if (!state_homo.exists()) {
			Log.d("homo", "file doesn't exist");
			throw new Exception("'" + state_homo + "' doesn't exist");
		} else {
			BufferedReader fr1, fr2;

			fr1 = new BufferedReader(new FileReader(homo_x_name));
			fr2 = new BufferedReader(new FileReader(homo_y_name));

			String line1, line2;
			int i = 0;
			while ((line1 = fr1.readLine()) != null
					&& (line2 = fr2.readLine()) != null) {
				String tokens1[] = line1.trim().split(" ");
				String tokens2[] = line2.trim().split(" ");
				try {
					for (int j = 0; j < tokens2.length; j++) {
						int datax = Integer.parseInt(tokens1[j]);
						int datay = Integer.parseInt(tokens2[j]);
						homo_x[j][i] = -datax;
						if (datax > max_x)
							max_x = datax;
						if (datax < min_x)
							min_x = datax;

						homo_y[j][i] = datay;
						if (datay > max_y)
							max_y = datay;
						if (datay < min_y)
							min_y = datay;

						homo_z[j][i] = Math.atan2(datax, datay);
						homo_mag[j][i] = Math.sqrt(datay * datay / 10000
								+ datax * datax / 10000);
						if (homo_z[j][i] > max_z)
							max_z = homo_z[j][i];
						if (homo_z[j][i] < min_z)
							min_z = homo_z[j][i];
						if (homo_mag[j][i] > max_mag)
							max_mag = homo_mag[j][i];
						if (homo_mag[j][i] < min_mag)
							min_mag = homo_mag[j][i];
					}
				} catch (Exception e) {
					Log.d("homo", "unable to load homo y");
				}
				i++;
			}
			fr1.close();
			fr2.close();
			Log.d("homo", "closed");
		}
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
		if (initialized) {
			double min_z = 1000, max_z = -1000, min_mag = 1000, max_mag = -1000;
			byte[] yPrime = cameraInterface.getYPrime();
			mWhiteArea_bybin.reset();
			double cam_orient = (GlobalVar.ay > 9.8) ? Math.PI : Math
					.asin(GlobalVar.ay / 9.8);
			double cam_diff = cam_orient - GlobalVar.THETA;
			if (READLOCALIZEDATA) {
				try {
					read_range_data_files();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				for (int i = 0; i < cameraInterface.frameWidth
						/ GlobalVar.REMAP_FACTOR; i++) {
					for (int j = 0; j < cameraInterface.frameHeight
							/ GlobalVar.REMAP_FACTOR; j++) {
						int coord1 = getCoordinate(remapped_x[i][j],
								remapped_y[i][j]);
						int temp = (int) yPrime[coord1] & 0xff;
						// TODO don't forget to prioritize other color before
						// detect
						// white
						if (temp > Blob.WHITE_THRESHOLD) {
							pStroke.setColor(Color.argb(255, 255, 255, 255));

							if (homo_mag[i][j] > PointRangeBin.MAG_LOWER_BOUND
									&& homo_mag[i][j] < PointRangeBin.MAG_UPPER_BOUND
									&& homo_z[i][j] > PointRangeBin.Z_LOWER_BOUND
									&& homo_z[i][j] < PointRangeBin.Z_UPPER_BOUND) {

								// TODO
								boolean acc_correct = false;
								if (acc_correct) {
									double distance_y = homo_y[i][j];
									double distance_x = homo_x[i][j];
									double theta = Math.atan(distance_y
											/ GlobalVar.CAMERA_HEIGHT);
									// double beta = theta+cam_diff;
									double beta = cam_diff;
									double dy;
									double dx;
									double actual_height = GlobalVar.CAMERA_HEIGHT
											* (GlobalVar.ay / 9.8);
									double c = Math.sqrt(distance_y
											* distance_y + actual_height
											* actual_height);
									int x, y;
									if (cam_diff > 0) {
										beta = beta * 1.2;
										theta = theta * 1.4 + Math.PI / 40;
										dy = Math.sin(beta)
												* c
												/ Math.sin(3 * Math.PI / 2
														- theta - beta);
										dx = distance_x / distance_y * dy;
										x = getXPix(homo_x[i][j] + (int) dx);
										y = getYPix(homo_y[i][j] + (int) dy);
										imgCanvas.drawPoint(x, y, pStroke);
									} else {
										beta = -beta * 1.2;
										theta = theta * 1.4 - Math.PI / 40;
										dy = Math.sin(beta)
												* c
												/ Math.sin(3 * Math.PI / 2
														+ theta - beta);
										dx = distance_x / distance_y * dy;
										x = getXPix(homo_x[i][j] + (int) dx);
										y = getYPix(homo_y[i][j] - (int) dy);
										imgCanvas.drawPoint(x, y, pStroke);
									}
									mWhiteArea_bybin.setBin(homo_z[i][j],
											homo_mag[i][j]);

								} else {
									mWhiteArea_bybin.setBin(homo_z[i][j],
											homo_mag[i][j]);
									int x = getXPix(homo_x[i][j]);
									int y = getYPix(homo_y[i][j]);
									imgCanvas.drawPoint(x, y, pStroke);
								}

								if (homo_z[i][j] < min_z)
									min_z = homo_z[i][j];
								if (homo_z[i][j] > max_z)
									max_z = homo_z[i][j];
								if (homo_mag[i][j] < min_mag)
									min_mag = homo_mag[i][j];
								if (homo_mag[i][j] > max_mag)
									max_mag = homo_mag[i][j];

							}
						}
					}
				}
				if (WRITELOCALIZEDATA) {
					try {
						append_range_data_files();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
	}

	private int getCoordinate(int x, int y) {
		// get mapping between Y value from YCbCr buffer array
		return (479 - y) * 640 + x;
	}

	private int getXPix(int x) {
		return (X_RANGE / 2 + x) * getWidth() / X_RANGE;
	}

	private int getYPix(int y) {
		return (Y_RANGE - y) * getHeight() / Y_RANGE;
	}

	int image_num = 0;

	public void reset() {
		img.eraseColor(Color.BLACK);
		drawObjects();
		pStroke.setColor(Color.YELLOW);
		imgCanvas.drawCircle(getXPix(0), getYPix(0), 5, pStroke);
		imgCanvas.drawCircle(getXPix(0), getYPix(Y_RANGE / 2), 5, pStroke);
		for (int i = 10000; i < Y_RANGE; i += 10000) {
			imgCanvas.drawLine(getXPix(-X_RANGE / 2), getYPix(i),
					getXPix(X_RANGE / 2), getYPix(i), pStroke);
		}
		for (int i = -X_RANGE / 2; i < X_RANGE / 2; i += 10000) {
			imgCanvas.drawLine(getXPix(i), getYPix(0), getXPix(i),
					getYPix(Y_RANGE), pStroke);
		}
		drawRect(new Rect(1, 1, getWidth() - 1, getHeight() - 1), Color.CYAN);
		// TEMP writing output
		boolean saveimage = false;
		if (saveimage) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			img.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
			// you can create a new file name "test.jpg" in sdcard folder.
			File f = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "pointrangepict" + image_num++ + ".jpg");
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

	public void initialize_write_data_files() throws Exception {

		// delete both header and range file, then rewrite new
		// header file

		Log.d("save_file", "initial saving process");
		File folder = new File(PATH + "/SensorData");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		Log.d("save_file", "folder created");

		state_range = new File(sensor_range_name);
		state_header = new File(sensor_header_name);
		state_range.delete();
		state_header.delete();
		if (!state_header.exists()) {
			state_header.createNewFile();
		}
		Log.d("save_file", "sensor header files created");

		FileWriter w;
		w = new FileWriter(state_header);
		w.write("" + PointRangeBin.Z_LOWER_BOUND + " "
				+ PointRangeBin.Z_UPPER_BOUND);
		w.write(" " + PointRangeBin.BIN_COUNT);
		Log.d("save_file", "wrote header data file");
		w.close();
	}

	public void initialize_read_data_files() throws Exception {
		state_header = new File(sensor_header_name);
		state_range = new File(sensor_range_name);

		if (!state_header.exists()) {
			Log.d("save_file", "file doesn't exist");
			throw new Exception("'" + state_header + "' doesn't exist");
		} else {

			String line1;
			header_buffer = new BufferedReader(new FileReader(
					sensor_header_name));
			line1 = header_buffer.readLine();
			String tokens[] = line1.trim().split(" ");
			zLowerBound = Double.parseDouble(tokens[0]);
			zUpperBound = Double.parseDouble(tokens[1]);
			binCount = Integer.parseInt(tokens[2]);
			Log.d("homo_testtoken", "," + tokens[0] + "," + tokens[1] + ","
					+ tokens[2] + ",");
			header_buffer.close();

			range_buffer = new BufferedReader(new FileReader(sensor_range_name));
		}
	}

	public void append_range_data_files() throws Exception {
		// delete both header and range file, then rewrite new
		// header file

		Log.d("save_file", "initial saving process");
		File folder = new File(PATH + "/SensorData");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		Log.d("save_file", "folder created");

		if (!state_range.exists()) {
			state_range.createNewFile();
		}
		Log.d("save_file", "sensor data files created");

		FileWriter w;
		w = new FileWriter(state_range, true);
		timestamp = System.currentTimeMillis();
		w.write("" + timestamp + ",");
		for (int i = 0; i < mWhiteArea_bybin.BIN_COUNT; i++) {
			w.write("" + mWhiteArea_bybin.bin_mag[i] + " ");
		}
		w.write("\n");
		Log.d("save_file", "wrote sensor data file");
		w.close();

	}

	public void read_range_data_files() throws Exception {
		if (!state_header.exists()) {
			Log.d("save_file", "file doesn't exist");
			throw new Exception("'" + state_header + "' doesn't exist");
		} else {
			String line1 = range_buffer.readLine();
			String tokens[] = line1.trim().split(",");
			String tokens_rangedata[] = tokens[1].trim().split(" ");
			double interval = (zUpperBound - zLowerBound) / binCount;
			for (int i = 0; i < tokens_rangedata.length; i++) {
				double z = zLowerBound + interval * i;
				double mag = Double.parseDouble(tokens_rangedata[i]);
				mWhiteArea_bybin.setBin(z,
						Double.parseDouble(tokens_rangedata[i]));
				int y = (int) (Math.cos(z) * mag * 100);
				int x = (int) (-Math.sin(z) * mag * 100);

				imgCanvas.drawPoint(getXPix(x), getYPix(y), pStroke);
			}

		}
	}
}
