package ivy.kookkai.vision;

import ivy.kookkai.data.GlobalVar;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

public class SensorActivity implements SensorEventListener {
	
	TextView headingText;

	private SensorManager sensorManager;
	private final double falldownThreashold = 150.0;
	private final double _ax = 0.3, _ay = 9.5, _az = 4.8;
	
	private float[] acc = new float[3];
	private double dax;
	private double day;
	private double daz;
	private double difAccel;
	
	private static float GOAL_DIRECTION = (float) (-1.2);// range from -PI
	public  static final float HEADING_ERROR_RANGE = (float) Math.PI * 7 / 16;
	private static final int MOVING_AVG_N = 5;
	
	private SensorManager compassManager;
	private float mValues[];
	private float heading;
	
	public SensorActivity(Activity activity, TextView headingText){
		
		this.headingText = headingText;
		
		sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		compassManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		compassManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	public void acceleroMeter_Handler(SensorEvent event){
		acc[0] = event.values[0];
		acc[1] = event.values[1];
		acc[2] = event.values[2];
	}
	
	public void fallingIndication() {
		dax = acc[0] - _ax;
		day = acc[1] - _ay;
		daz = acc[2] - _az;
		difAccel = dax * dax + day * day + daz * daz;
		
		GlobalVar.isFalling = difAccel > falldownThreashold;
	}
	
	public void magneticField_Handler(SensorEvent event){
		float R[] = new float[9];
		mValues = new float[3];
		float oldHeading = GlobalVar.heading;
		
		SensorManager.getRotationMatrix(R, null, acc, event.values);
		SensorManager.getOrientation(R, mValues);
		headingText.setText("Default:" + GOAL_DIRECTION + "\n" + 
		"Heading:"+String.format("%.2f", mValues[0]));
		
		float newHeading = mValues[0];
		float diff = newHeading-oldHeading;
		if(diff>Math.PI) diff = diff - 2*(float)Math.PI;
		if(diff<-Math.PI) diff = diff + 2*(float)Math.PI;
		float avgHeading = oldHeading+diff/(float)MOVING_AVG_N;
		setHeading(avgHeading);
	}
	
	public void setHeading(float heading) {
		float h = heading;
		while(h>Math.PI) h-=2*(float)Math.PI;
		while(h<Math.PI) h+=2*(float)Math.PI;
		GlobalVar.heading = heading;
	}

	public void directionIndication() {
		// NOTE Manual GOAL_DIRECTION adjust at second half
		float error = heading - GOAL_DIRECTION;
		if (error >= 0) {
			if (error <= HEADING_ERROR_RANGE) {
				GlobalVar.isGoalDirection = true;
				return;
			}
			if (2 * Math.PI - error <= HEADING_ERROR_RANGE) {
				GlobalVar.isGoalDirection = true;
				return;
			}
		} else {
			if (-HEADING_ERROR_RANGE < error) {
				GlobalVar.isGoalDirection = true;
				return;
			}
			if (2 * Math.PI + error <= HEADING_ERROR_RANGE) {
				GlobalVar.isGoalDirection = true;
				return;
			}
		}
		GlobalVar.isGoalDirection = false;

	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			acceleroMeter_Handler(event);
		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			magneticField_Handler(event);
		}
	}

}
