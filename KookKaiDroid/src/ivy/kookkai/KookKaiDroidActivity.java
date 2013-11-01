package ivy.kookkai;

import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.debugview.CameraInterface;
import ivy.kookkai.debugview.DebugImgView;
import ivy.kookkai.vision.SensorInterface;
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

public class KookKaiDroidActivity extends Activity {
	/** Called when the activity is first created. */

	CameraInterface cameraInterface;
	SensorInterface sensorInterface;
	
	MainlLoop main;
	DebugImgView debugImgview;
	TextView debugText, headingText;
	Context mContext;
	CheckBox drawColor;

	public static final String PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Log.d("ivy_debug", "start!!!");

		mContext = this.getApplicationContext();
		
		cameraInterface = new CameraInterface(this);
		sensorInterface = new SensorInterface(this);

		createGUI();

		headingText = (TextView) findViewById(R.id.headingText);
		debugText = (TextView) findViewById(R.id.debugText);

		main = new MainlLoop(cameraInterface, sensorInterface, debugImgview, debugText, drawColor);
	}

	private void createGUI() {
		LinearLayout rootHorizontalLayout = (LinearLayout) findViewById(R.id.upper_view);

		// Left Vertical Layout zone
		cameraInterface.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		debugImgview = new DebugImgView(this);
		debugImgview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		FrameLayout cameraFrame = new FrameLayout(this);
		cameraFrame.setLayoutParams(new LayoutParams(cameraInterface.frameHeight / 2, cameraInterface.frameWidth / 2));
		cameraFrame.addView(cameraInterface);
		cameraFrame.addView(debugImgview);

		final Button exitButton = new Button(this);
		final Button setGoalDirection = new Button(this);
		final Button notSetGoalDirection = new Button(this);

		setGoalDirection.setFocusable(false);
		setGoalDirection.setText("Set Magnetic");
		setGoalDirection.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sensorInterface.setCurrentToGoalDirection();
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

		drawColor = new CheckBox(this);
		drawColor.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		drawColor.setChecked(false);
		drawColor.setText("draw color");
		drawColor.setFocusable(false);

		LinearLayout leftVerticalLayout = new LinearLayout(this);
		leftVerticalLayout.setLayoutParams(new LayoutParams(cameraInterface.frameHeight / 2,
				cameraInterface.frameWidth * 2 / 3));
		leftVerticalLayout.setOrientation(LinearLayout.VERTICAL);
		leftVerticalLayout.addView(notSetGoalDirection);
		leftVerticalLayout.addView(setGoalDirection);
		leftVerticalLayout.addView(exitButton);
		leftVerticalLayout.addView(drawColor);

		// Right Vertical Layout zone

		LinearLayout rightVerticalLayout = new LinearLayout(this);
		rightVerticalLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 350));
		rightVerticalLayout.setOrientation(LinearLayout.VERTICAL);
		rightVerticalLayout.addView(cameraFrame);

		// TODO: SWAP HERE TO USE WITH ACER
		rootHorizontalLayout.addView(leftVerticalLayout);
		rootHorizontalLayout.addView(rightVerticalLayout);
	}

	private boolean keyHandle(int keyCode, KeyEvent event) {
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		return keyHandle(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		super.onKeyUp(keyCode, event);
		return keyHandle(keyCode, event);
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		main.stop();
	}

}