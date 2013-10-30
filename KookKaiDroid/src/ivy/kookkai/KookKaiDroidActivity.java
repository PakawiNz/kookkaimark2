package ivy.kookkai;

import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.debugview.CameraInterface;
import ivy.kookkai.debugview.DebugImgView;
import ivy.kookkai.debugview.FieldView;
import ivy.kookkai.debugview.HomographyPointsView;
import ivy.kookkai.debugview.LocalizationView;
import ivy.kookkai.debugview.UndistortView;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
//import android.renderscript.Mesh.Primitive;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
public class KookKaiDroidActivity extends Activity implements
		SensorEventListener {
	/** Called when the activity is first created. */

	private final int FIELDVIEWHEIGHT = 300;
	public static final boolean LOCALIZE_MODE = false;
	CameraInterface cameraInterface;
	MainlLoop main;
	DebugImgView debugImgview;
	FieldView fieldView;
	UndistortView undistortView;
	HomographyPointsView homographyView;
	LocalizationView localizationView;
	TextView debugText,headingText;
	Context mContext;	
	
	//Sensor Part
	SensorManager sensorManager, compassManager;
	float mValues[] = new float[3];
	float[] acc = new float[3];
	
	public static final String PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath();
	private static final String MAGNETIC_MINMAX_FILE = "/SensorData/magnetic_minmax.txt";
	private static final String MAGNETIC_MINMAX_NAME = PATH
			+ MAGNETIC_MINMAX_FILE;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		LinearLayout rootHorizontalLayout = (LinearLayout) findViewById(R.id.upper_view);

		Log.d("ivy_debug", "start!!!");

		mContext = this.getApplicationContext();

		// Left Vertical Layout zone
		cameraInterface = new CameraInterface(this);
		cameraInterface.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		debugImgview = new DebugImgView(this);
		debugImgview.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		FrameLayout cameraFrame = new FrameLayout(this);
		cameraFrame.setLayoutParams(new LayoutParams(
						cameraInterface.frameHeight / 2,
						cameraInterface.frameWidth / 2));
		cameraFrame.addView(cameraInterface);
		cameraFrame.addView(debugImgview);

		Button exitButton = new Button(this);
		final Button setGoalDirection = new Button(this);
		final Button notSetGoalDirection = new Button(this);
		
		setGoalDirection.setFocusable(false);
		setGoalDirection.setText("Set Magnetic");
		setGoalDirection.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				GlobalVar.GOAL_DIRECTION = mValues[0];
				setGoalDirection.setVisibility(View.GONE);
				notSetGoalDirection.setVisibility(View.GONE);
			}
		});
		
		
		notSetGoalDirection.setFocusable(false);
		notSetGoalDirection.setText("Not Set Magnetic");
		notSetGoalDirection.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setGoalDirection.setVisibility(View.GONE);
				notSetGoalDirection.setVisibility(View.GONE);
			}
		});
		
		exitButton.setFocusable(false);
		exitButton.setText("exit!!");
		exitButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		CheckBox cb = new CheckBox(this);
		cb.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		cb.setChecked(false);
		cb.setText("draw color");
		cb.setFocusable(false);

		Log.d("frame", "button height:");// why can't get button height
		LinearLayout leftVerticalLayout = new LinearLayout(this);
		leftVerticalLayout.setLayoutParams(new LayoutParams(
				cameraInterface.frameHeight / 2,
				cameraInterface.frameWidth / 1 ));
		leftVerticalLayout.setOrientation(LinearLayout.VERTICAL);
		leftVerticalLayout.addView(notSetGoalDirection);
		//leftVerticalLayout.addView(cameraFrame);
		leftVerticalLayout.addView(setGoalDirection);
		leftVerticalLayout.addView(exitButton);
		leftVerticalLayout.addView(cb);

		// Right Vertical Layout zone
		FrameLayout fieldFrame = new FrameLayout(this);

		fieldView = new FieldView(this);
		fieldView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				FIELDVIEWHEIGHT));

		localizationView = new LocalizationView(this);
		localizationView.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, FIELDVIEWHEIGHT));

		//fieldFrame.addView(fieldView);
		fieldFrame.addView(localizationView);

		undistortView = new UndistortView(this);
		undistortView.setLayoutParams(new LayoutParams(undistortView.VIEWWIDTH,
				undistortView.VIEWHEIGHT));

		homographyView = new HomographyPointsView(this);
		homographyView.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		LinearLayout undistort_n_homo = new LinearLayout(this);
		undistort_n_homo.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		undistort_n_homo.setOrientation(LinearLayout.HORIZONTAL);
		undistort_n_homo.addView(undistortView);
		undistort_n_homo.addView(homographyView);

		LinearLayout rightVerticalLayout = new LinearLayout(this);
		rightVerticalLayout.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, FIELDVIEWHEIGHT
						+ UndistortView.VIEWHEIGHT));
		rightVerticalLayout.setOrientation(LinearLayout.VERTICAL);
		//rightVerticalLayout.addView(fieldFrame);
		// rightVerticalLayout.addView(undistortFrame);
		//rightVerticalLayout.addView(undistort_n_homo);
		rightVerticalLayout.addView(cameraFrame);

		debugText = (TextView) findViewById(R.id.debugText);

		//TODO: SWAP HERE TO USE WITH ACER
		rootHorizontalLayout.addView(leftVerticalLayout);
		rootHorizontalLayout.addView(rightVerticalLayout);
		
		headingText = (TextView) findViewById(R.id.headingText);
		
		//then initialize sensors
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		compassManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		compassManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_NORMAL);
		main = new MainlLoop(cameraInterface, debugImgview, 
//				localizationView,
//				undistortView, homographyView, 
				debugText, cb);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		return keyHandle(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		super.onKeyUp(keyCode, event);
		return keyHandle(keyCode, event);
	}

	public boolean keyHandle(int keyCode, KeyEvent event) {
		boolean state;
		if (event.getAction() == KeyEvent.ACTION_DOWN)
			state = true;
		else
			state = false;

		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
			GlobalVar.joyData.dUp = state;
			break;

		case KeyEvent.KEYCODE_DPAD_DOWN:
			GlobalVar.joyData.dDown = state;
			break;

		case KeyEvent.KEYCODE_DPAD_LEFT:
			GlobalVar.joyData.dLeft = state;
			break;

		case KeyEvent.KEYCODE_DPAD_RIGHT:
			GlobalVar.joyData.dRight = state;
			break;

		case KeyEvent.KEYCODE_1:
			GlobalVar.joyData.one = state;
			break;
		case KeyEvent.KEYCODE_2:
			GlobalVar.joyData.two = state;
			break;

		case KeyEvent.KEYCODE_A:
			GlobalVar.joyData.a = state;
			break;
		case KeyEvent.KEYCODE_B:
			GlobalVar.joyData.b = state;
			break;

		default:
			break;
		}

		return true;
	}

	@Override
	public void onStart() {
		super.onStart();
		main.start();
		// debugImgview.fill(color.white);
		// debugImgview.invalidate();

	}

	@Override
	public void onStop() {
		super.onStop();
		// main.stop();
	}

	public void onDestroy() {
		super.onDestroy();
		main.stop();

	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

			GlobalVar.ax = acc[0] = event.values[0];
			GlobalVar.ay = acc[1] = event.values[1];
			GlobalVar.az = acc[2] = event.values[2];

		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			float R[] = new float[9];
			mValues = new float[3];

			SensorManager.getRotationMatrix(R, null, acc, event.values);
			SensorManager.getOrientation(R, mValues);
			headingText.setText("Default:" + GlobalVar.GOAL_DIRECTION + "\n" + "Rotated:"+String.format("%.2f", mValues[0]));
			GlobalVar.setHeading(mValues[0]);
			
		}
	}
}