package ivy.kookkai.vision;

import ivy.kookkai.data.GlobalVar;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorInterface implements SensorEventListener {

	private String out;

	public SensorInterface(Activity activity) {

		sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		compassManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		compassManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_NORMAL);
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
	
	public String execute(){
		out = 	//"FallingState:" + falling_state + "\n" +
				//"GOAL:" + goal_direction + "\n" + 
				"Heading:" + String.format("%.2f", heading) + "\n";
		
		fallingIndication();
		directionIndication();
		
		GlobalVar.heading = heading;
		GlobalVar.isGoalDirection = isGoalDirection;
		GlobalVar.falling_state = falling_state;
		
		return out;
	}

	/**-------------------------------------> ACCELEROMETER <-------------------------------------*/

	public static final int FALL_FACEDOWN = -1;
	public static final int FALL_FACEUP = 1;
	public static final int FALL_NONE = 0;

	private static final double FALL_Treshold = 150.0;
	private static final double _ax = 0.3, _ay = 9.5, _az = 4.8;

	private SensorManager sensorManager;
	private float[] acc = new float[3];
	private double dax;
	private double day;
	private double daz;
	private double difAccel;
	private int falling_state;

	private void acceleroMeter_Handler(SensorEvent event) {
		acc[0] = event.values[0];
		acc[1] = event.values[1];
		acc[2] = event.values[2];
	}

	private void fallingIndication() {
		dax = acc[0] - _ax;
		day = acc[1] - _ay;
		daz = acc[2] - _az;
		difAccel = dax * dax + day * day + daz * daz;

		if (difAccel > FALL_Treshold) {
			if (acc[1] < 0) {
				falling_state = FALL_FACEDOWN;
			} else {
				falling_state = FALL_FACEUP;
			}
		} else {
			falling_state = FALL_NONE;
		}

	}

	/** -------------------------------------> MAGNETIC FIELD <------------------------------------- */

	private static final float HEADING_ERROR_RANGE = (float) Math.PI * 7 / 16;
	private static final int MOVING_AVG_N = 5;

	private SensorManager compassManager;
	private float mValues[];
	private float goal_direction = (float) (-1.2);// range from -PI
	private boolean isGoalDirection;
	private float heading;

	private void magneticField_Handler(SensorEvent event) {
		float R[] = new float[9];
		mValues = new float[3];
		float oldHeading = heading;

		SensorManager.getRotationMatrix(R, null, acc, event.values);
		SensorManager.getOrientation(R, mValues);

		float newHeading = mValues[0];
		float diff = newHeading - oldHeading;
		if (diff > Math.PI)
			diff = diff - 2 * (float) Math.PI;
		if (diff < -Math.PI)
			diff = diff + 2 * (float) Math.PI;
		float avgHeading = oldHeading + diff / (float) MOVING_AVG_N;

		while (avgHeading > Math.PI)
			avgHeading -= 2 * (float) Math.PI;
		while (avgHeading < Math.PI)
			avgHeading += 2 * (float) Math.PI;

		heading = avgHeading;
	}

	private void directionIndication() {
		// NOTE Manual GOAL_DIRECTION adjust at second half
		float error = heading - goal_direction;
		if (error >= 0) {
			if (error <= HEADING_ERROR_RANGE) {
				isGoalDirection = true;
				return;
			}
			if (2 * Math.PI - error <= HEADING_ERROR_RANGE) {
				isGoalDirection = true;
				return;
			}
		} else {
			if (-HEADING_ERROR_RANGE < error) {
				isGoalDirection = true;
				return;
			}
			if (2 * Math.PI + error <= HEADING_ERROR_RANGE) {
				isGoalDirection = true;
				return;
			}
		}
		isGoalDirection = false;
	}

	public void setCurrentToGoalDirection() {
		goal_direction = heading;
	}

}
