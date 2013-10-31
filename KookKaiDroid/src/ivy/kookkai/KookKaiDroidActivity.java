package ivy.kookkai;

import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.debugview.CameraInterface;
import ivy.kookkai.debugview.DebugImgView;
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

	private final static int MOVING_AVG_N = 5;
	CameraInterface cameraInterface;
	MainlLoop main;
	DebugImgView debugImgview;
	TextView debugText,headingText;
	Context mContext;	
	
	//Sensor Part
	SensorManager sensorManager, compassManager;
	float mValues[];
	float[] acc = new float[3];
	
	public static final String PATH = Environment
			.getExternalStorageDirectory().getAbsolutePath();

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

		LinearLayout leftVerticalLayout = new LinearLayout(this);
		leftVerticalLayout.setLayoutParams(new LayoutParams(
				cameraInterface.frameHeight / 2,
				cameraInterface.frameWidth *2/ 3 ));
		leftVerticalLayout.setOrientation(LinearLayout.VERTICAL);
		leftVerticalLayout.addView(notSetGoalDirection);
		leftVerticalLayout.addView(setGoalDirection);
		leftVerticalLayout.addView(exitButton);
		leftVerticalLayout.addView(cb);

		// Right Vertical Layout zone

		LinearLayout rightVerticalLayout = new LinearLayout(this);
		rightVerticalLayout.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, 350));
		rightVerticalLayout.setOrientation(LinearLayout.VERTICAL);
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
		main = new MainlLoop(cameraInterface, debugImgview, debugText, cb);
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
			float oldHeading = GlobalVar.heading;
			
			SensorManager.getRotationMatrix(R, null, acc, event.values);
			SensorManager.getOrientation(R, mValues);
			headingText.setText("Default:" + GlobalVar.GOAL_DIRECTION + "\n" + 
			"Heading:"+String.format("%.2f", mValues[0]));
			
			float newHeading = mValues[0];
			float diff = newHeading-oldHeading;
			if(diff>Math.PI) diff = diff - 2*(float)Math.PI;
			if(diff<-Math.PI) diff = diff + 2*(float)Math.PI;
			float avgHeading = oldHeading+diff/(float)MOVING_AVG_N;
			GlobalVar.setHeading(avgHeading);
			
		}
	}
}