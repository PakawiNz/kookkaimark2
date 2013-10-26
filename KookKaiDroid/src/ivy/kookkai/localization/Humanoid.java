package ivy.kookkai.localization;

import android.util.Log;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.debugview.FieldView;
import ivy.kookkai.debugview.HomographyPointsView;

public class Humanoid {
	private static final int DEFAULT_P_AMOUNT = 1000;
	private static final int LINE_WEIGHT = 20;
	public static float[][] WEIGHT_MAP = new float[FieldView.TOTALWIDTH][FieldView.TOTALLENGTH];
	// [y co-ord][x-coord]
	public Particle[] mParticles;
	private Particle mBestParticle = null;
	private double weightTotalNorm = 0;
	private double weightVar = 0;
	private double[] weightRoulette;
	private double weightTotal;
	private static double[] motionUpdate = { 0.0, 0.0, 0.0 };

	public Humanoid() {
		long timestamp = System.currentTimeMillis();
		initialized_weight_map();
		Log.d("hmnd_timestamp", "" + (System.currentTimeMillis() - timestamp));
		mParticles = new Particle[DEFAULT_P_AMOUNT];
		for (int i = 0; i < mParticles.length; i++) {
			mParticles[i] = new Particle();
		}
		weightRoulette = new double[mParticles.length];
	}

	public static void incrementMotion(double x_inc, double y_inc, double z_inc) {
		motionUpdate[0] += x_inc;
		motionUpdate[1] += y_inc;
		motionUpdate[2] += z_inc;
	}

	public int update_particles_weight() {
		double maxWeight = 0;
		int ret = 0;
		weightTotalNorm = 0;
		weightVar = 0;
		weightTotal = 0;
		weightRoulette = new double[mParticles.length];
		for (int i = 0; i < mParticles.length; i++) {
			// WARNING + cal_weight to put some weight on particle w/ no white
			// lines seen
			double newWeight = 5 + calculate_weight(mParticles[i].getmX(),
					mParticles[i].getmY(), mParticles[i].getmZ());
			mParticles[i].setmWeight(newWeight);
			weightTotal += newWeight;
			if (i == 0)
				weightRoulette[i] = newWeight;
			else
				weightRoulette[i] = newWeight + weightRoulette[i - 1];
			if (newWeight > maxWeight) {
				maxWeight = newWeight;
				ret = (int) maxWeight;
				mBestParticle = mParticles[i];
			}
		}
		Log.d("weight", "max weight:" + maxWeight);
		for (int i = 0; i < mParticles.length; i++) {
			double newWeightNorm = mParticles[i].getmWeight() / maxWeight;
			weightTotalNorm += newWeightNorm;
			weightVar += newWeightNorm * newWeightNorm;
		}
		weightVar /= mParticles.length;
		Log.d("hmnd",
				"best par x,y:" + (int) mBestParticle.getmX() + ","
						+ (int) mBestParticle.getmY() + ","
						+ (int) mBestParticle.getmWeight());
		return ret;
	}

	private double calculate_weight(double _x, double _y, double _z) {
		double weight = 0;
		for (int i = 0; i < HomographyPointsView.mWhiteArea_bybin.getBinCount(); i++) {
			double[] a = HomographyPointsView.mWhiteArea_bybin.getBin(i);
			double z = a[0];
			double mag = a[1];
			if (mag != -1) {
				int cal_x, cal_y;
				cal_x = (int) (_x + mag * Math.cos(z + _z));
				cal_y = (int) (_y + mag * Math.sin(z + _z));
				if (inBound(cal_x, cal_y)) {
					weight += WEIGHT_MAP[cal_y][cal_x];
				}
			}
		}
		return weight;
	}

	// TODO don't forget gaussian
	public void update_motion_random_uniform() {
		double mag_min = 13;
		double z_max = Math.PI / 7;
		double mag_error = 5;
		// z = z +- random()*PI/14
		// x = x+((15+random(0-5))*cos(z)
		// y = y+((15+random(0-5))*cos(z)

		for (int i = 0; i < mParticles.length; i++) {
			double z = mParticles[i].getmZ() + Math.random() * z_max - z_max
					/ 2;
			mParticles[i].setmZ(z);
			double mag = mag_min + mag_error * Math.random();
			double x = mParticles[i].getmX() + Math.cos(mParticles[i].getmZ())
					* mag;
			double y = mParticles[i].getmY() + Math.sin(mParticles[i].getmZ())
					* mag;
			mParticles[i].setmX(x);
			mParticles[i].setmY(y);

			if (!inBound((int) mParticles[i].getmX(),
					(int) mParticles[i].getmY())) {
				mParticles[i] = new Particle();
			}
		}
	}

	public void update_motion_from_model() {
		Log.d("hmnd_motion", motionUpdate[0] + "," + motionUpdate[1] + ","
				+ motionUpdate[2]);
		double x_error = 4;
		double y_error = 6;
		double z_error = Math.PI / 7;
		for (int i = 0; i < mParticles.length; i++) {
			double dx = motionUpdate[0]
					+ (x_error * Math.random() - x_error / 2);
			double dy = motionUpdate[1]
					+ (y_error * Math.random() - y_error / 2);
			double dz = motionUpdate[2]
					+ (z_error * Math.random() - z_error / 2);
			double z = mParticles[i].getmZ();
			double x = mParticles[i].getmX() + Math.cos(z) * dy + Math.sin(z)
					* dx;
			double y = mParticles[i].getmY() + Math.sin(z) * dy - Math.cos(z)
					* dx;
			// Log.d("hmnd_diff",""+(int)(x-mParticles[i].getmX())+","+(int)(y-mParticles[i].getmY()));
			Log.d("hmnd_diff", "" + x + "," + y);
			mParticles[i].setmX(x);
			mParticles[i].setmY(y);
			mParticles[i].setmZ(mParticles[i].getmZ() + dz);

			if (!inBound((int) mParticles[i].getmX(),
					(int) mParticles[i].getmY())) {
				mParticles[i] = new Particle();
			}
		}
		for (int i = 0; i < 3; i++) {
			motionUpdate[i] = 0;
		}
	}

	public void resampling_bestfit() {
		double mean = weightTotalNorm / mParticles.length;
		double sd = Math.sqrt(weightVar);
		double threshold = mean + sd * 0;// TODO set threshold
		Particle BestPar = getmBestParticle();
		for (int i = 0; i < mParticles.length; i++) {
			if (mParticles[i].getmWeight() < threshold) {
				mParticles[i].setmX(BestPar.getmX());
				mParticles[i].setmY(BestPar.getmY());
				mParticles[i].setmZ(BestPar.getmZ());
			}
		}
	}

	public void resampling_prob() {
		double dice;
		Particle[] tempParticles = new Particle[mParticles.length];
		for (int i = 0; i < mParticles.length; i++) {
			tempParticles[i] = new Particle();
		}
		for (int i = 0; i < mParticles.length; i++) {
			dice = Math.random() * weightTotal;
			for (int j = 0; j < mParticles.length; j++) {
				if (weightRoulette[j] > dice) {
					tempParticles[i].setmX(mParticles[j].getmX());
					tempParticles[i].setmY(mParticles[j].getmY());
					tempParticles[i].setmZ(mParticles[j].getmZ());
					break;
				}
			}
		}
		for (int i = 0; i < mParticles.length; i++) {
			mParticles[i] = tempParticles[i];
		}
	}

	public Particle getmBestParticle() {
		return mBestParticle;
	}

	private boolean inBound(int x, int y) {
		if (x >= 0 && x < WEIGHT_MAP[0].length && y >= 0
				&& y < WEIGHT_MAP.length)
			return true;
		return false;
	}

	private void fillDilatedY(int y, int x) {
		for (int j = -LINE_WEIGHT / 2; j < 0; j++) {
			float weight = (4 / (float) LINE_WEIGHT) * j + 2;
			if (weight > 1)
				weight = 1;
			if (weight > WEIGHT_MAP[y + j][x])
				WEIGHT_MAP[y + j][x] = weight;
		}
		for (int j = 0; j < LINE_WEIGHT / 2; j++) {
			float weight = (-4 / (float) LINE_WEIGHT) * j + 2;
			if (weight > 1)
				weight = 1;
			if (weight > WEIGHT_MAP[y + j][x])
				WEIGHT_MAP[y + j][x] = weight;
		}
	}

	private void fillDilatedX(int y, int x) {
		// for (int j = -LINE_WEIGHT / 2; j < LINE_WEIGHT / 2; j++) {
		// WEIGHT_MAP[y][x + j] = 1;
		// }
		for (int j = -LINE_WEIGHT / 2; j < 0; j++) {
			float weight = (4 / (float) LINE_WEIGHT) * j + 2;
			if (weight > 1)
				weight = 1;
			if (weight > WEIGHT_MAP[y][x + j])
				WEIGHT_MAP[y][x + j] = weight;
		}
		for (int j = 0; j < LINE_WEIGHT / 2; j++) {
			float weight = (-4 / (float) LINE_WEIGHT) * j + 2;
			if (weight > 1)
				weight = 1;
			if (weight > WEIGHT_MAP[y][x + j])
				WEIGHT_MAP[y][x + j] = weight;
		}
	}

	private void initialized_weight_map() {
		// WARNING line weight = 1, need parameterization
		// [y][x]

		// field line
		// horizontal touch line
		for (int i = FieldView.BORDER_STRIP_WIDTH; i < FieldView.BORDER_STRIP_WIDTH
				+ FieldView.FIELD_LENGTH; i++) {
			fillDilatedY(FieldView.BORDER_STRIP_WIDTH, i);
			fillDilatedY(FieldView.BORDER_STRIP_WIDTH + FieldView.FIELD_WIDTH,
					i);
		}
		// vertical goal lines and center line
		for (int i = FieldView.BORDER_STRIP_WIDTH; i < FieldView.BORDER_STRIP_WIDTH
				+ FieldView.FIELD_WIDTH; i++) {
			fillDilatedX(i, FieldView.BORDER_STRIP_WIDTH);
			fillDilatedX(i, FieldView.BORDER_STRIP_WIDTH
					+ FieldView.FIELD_LENGTH);
			fillDilatedX(i, FieldView.TOTALLENGTH / 2);
		}
		// backend goal line
		// left backend horizontal goal line
		for (int i = FieldView.BORDER_STRIP_WIDTH - FieldView.GOAL_DEPTH; i < FieldView.BORDER_STRIP_WIDTH; i++) {
			fillDilatedY(FieldView.TOTALWIDTH / 2 - FieldView.GOAL_WIDTH / 2, i);
			fillDilatedY(FieldView.TOTALWIDTH / 2 + FieldView.GOAL_WIDTH / 2, i);
		}
		// left backend vertical goal line
		for (int i = FieldView.TOTALWIDTH / 2 - FieldView.GOAL_WIDTH / 2; i < FieldView.TOTALWIDTH
				/ 2 + FieldView.GOAL_WIDTH / 2; i++) {
			fillDilatedX(i, FieldView.BORDER_STRIP_WIDTH - FieldView.GOAL_DEPTH);
		}
		// right backend horizontal goal line
		for (int i = FieldView.TOTALLENGTH - FieldView.BORDER_STRIP_WIDTH; i < FieldView.TOTALLENGTH
				- FieldView.BORDER_STRIP_WIDTH + FieldView.GOAL_DEPTH; i++) {
			fillDilatedY(FieldView.TOTALWIDTH / 2 - FieldView.GOAL_WIDTH / 2, i);
			fillDilatedY(FieldView.TOTALWIDTH / 2 + FieldView.GOAL_WIDTH / 2, i);
		}
		// right backend vertical goal line
		for (int i = FieldView.TOTALWIDTH / 2 - FieldView.GOAL_WIDTH / 2; i < FieldView.TOTALWIDTH
				/ 2 + FieldView.GOAL_WIDTH / 2; i++) {
			fillDilatedX(i, FieldView.TOTALLENGTH
					- FieldView.BORDER_STRIP_WIDTH + FieldView.GOAL_DEPTH);
		}
		// penalty goal area
		// left horizontal penalty goal area
		for (int i = FieldView.BORDER_STRIP_WIDTH; i < FieldView.BORDER_STRIP_WIDTH
				+ FieldView.GOAL_AREA_LENGTH; i++) {
			fillDilatedY(FieldView.TOTALWIDTH / 2 - FieldView.GOAL_AREA_WIDTH
					/ 2, i);
			fillDilatedY(FieldView.TOTALWIDTH / 2 + FieldView.GOAL_AREA_WIDTH
					/ 2, i);
		}
		// left vertical penalty goal area
		for (int i = FieldView.TOTALWIDTH / 2 - FieldView.GOAL_AREA_WIDTH / 2; i < FieldView.TOTALWIDTH
				/ 2 + FieldView.GOAL_AREA_WIDTH / 2; i++) {
			fillDilatedX(i, FieldView.BORDER_STRIP_WIDTH
					+ FieldView.GOAL_AREA_LENGTH);
		}
		// right horizontal penalty goal area
		for (int i = FieldView.TOTALLENGTH - FieldView.BORDER_STRIP_WIDTH
				- FieldView.GOAL_AREA_LENGTH; i < FieldView.TOTALLENGTH
				- FieldView.BORDER_STRIP_WIDTH; i++) {
			fillDilatedY(FieldView.TOTALWIDTH / 2 - FieldView.GOAL_AREA_WIDTH
					/ 2, i);
			fillDilatedY(FieldView.TOTALWIDTH / 2 + FieldView.GOAL_AREA_WIDTH
					/ 2, i);
		}
		// right vertical penalty goal area
		for (int i = FieldView.TOTALWIDTH / 2 - FieldView.GOAL_AREA_WIDTH / 2; i < FieldView.TOTALWIDTH
				/ 2 + FieldView.GOAL_AREA_WIDTH / 2; i++) {
			fillDilatedX(i, FieldView.TOTALLENGTH
					- FieldView.BORDER_STRIP_WIDTH - FieldView.GOAL_AREA_LENGTH);
		}
		// center circle
		float resolution = 300;
		for (int i = 0; i < resolution; i++) {
			// NOTE cause can't create ramp so use Line_weight / 3
			for (int j = -LINE_WEIGHT / 3; j < LINE_WEIGHT / 3; j++) {
				int y = (int) ((FieldView.CENTER_CIRCLE_DIAMETER / 2 + j) * Math
						.sin((float) i / resolution * 2 * Math.PI));
				int x = (int) ((FieldView.CENTER_CIRCLE_DIAMETER / 2 + j) * Math
						.cos((float) i / resolution * 2 * Math.PI));
				WEIGHT_MAP[y + FieldView.TOTALWIDTH / 2][x
						+ FieldView.TOTALLENGTH / 2] = 1;
			}
		}
		// left horizontal penalty mark
		for (int i = FieldView.BORDER_STRIP_WIDTH
				+ FieldView.PENALTY_MARK_DISTANCE - 5; i < FieldView.BORDER_STRIP_WIDTH
				+ FieldView.PENALTY_MARK_DISTANCE + 5; i++) {
			fillDilatedY(FieldView.TOTALWIDTH / 2, i);
		}
		// left vertical penalty mark
		for (int i = FieldView.TOTALWIDTH / 2 - 5; i < FieldView.TOTALWIDTH / 2 + 5; i++) {
			fillDilatedX(i, FieldView.BORDER_STRIP_WIDTH
					+ FieldView.PENALTY_MARK_DISTANCE);
		}
		// right horizontal penalty mark
		for (int i = FieldView.TOTALLENGTH - FieldView.PENALTY_MARK_DISTANCE
				- FieldView.BORDER_STRIP_WIDTH - 5; i < FieldView.TOTALLENGTH
				- FieldView.PENALTY_MARK_DISTANCE
				- FieldView.BORDER_STRIP_WIDTH + 5; i++) {
			fillDilatedY(FieldView.TOTALWIDTH / 2, i);
		}
		// right vertical penalty mark
		for (int i = FieldView.TOTALWIDTH / 2 - 5; i < FieldView.TOTALWIDTH / 2 + 5; i++) {
			fillDilatedX(i, FieldView.TOTALLENGTH
					- FieldView.PENALTY_MARK_DISTANCE
					- FieldView.BORDER_STRIP_WIDTH);
		}
		// center horizontal penalty mark
		for (int i = FieldView.TOTALLENGTH / 2 - 5; i < FieldView.TOTALLENGTH / 2 + 5; i++) {
			fillDilatedY(FieldView.TOTALWIDTH / 2, i);
		}

	}
}
