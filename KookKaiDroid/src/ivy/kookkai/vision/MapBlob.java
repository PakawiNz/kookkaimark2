package ivy.kookkai.vision;

import android.graphics.Rect;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.vision.BlobObject;

public class MapBlob {
	public static final int KEEP_BALL_POSTION_PERIOD = 10;// unit: loop(s)
	private int mLastLoopSeen = 0;
	private double[] mLastBallStat = new double[4];
	private double[] mLastGoalStat = new double[4];
	private Rect lastRectSeen = new Rect(0, 0, 0, 0);
	private int UPDATE_FACTOR = 100;
	private int SINGLE_POLE_THRESHOLD = 2000;
	private boolean seenGoalLastTime = false;

	int lastGoalDirection = 0;

	public MapBlob() {

	}

	public String execute() {
		String out = "";

		GlobalVar.ballPos[2] = -1;
		GlobalVar.goalPos[2] = -1;
		GlobalVar.enemyPos[2] = -1;
		boolean seenABall = false;
		BlobObject biggestGoalBlob = null;

		for (int i = 0; i < GlobalVar.mergeResult.size(); i++) {
			BlobObject b = GlobalVar.mergeResult.get(i);

			switch (b.tag) {

			case GlobalVar.BALL:
				GlobalVar.ballPos[0] = mLastBallStat[0] = b.posRect.centerX()
						- GlobalVar.frameWidth / 2;
				GlobalVar.ballPos[1] = mLastBallStat[1] = GlobalVar.frameHeight
						- b.posRect.centerY();
				GlobalVar.ballPos[2] = mLastBallStat[2] = 1;
				GlobalVar.ballPos[3] = mLastBallStat[3] = b.getSize();
				seenABall = true;
				lastRectSeen = b.posRect;
				mLastLoopSeen = 0;
				break;

			case GlobalVar.GOAL:
				// check for goal direction
				if (GlobalVar.isGoalDirection()) {
					// GlobalVar.goalPos[0] = // (int)(0.35*b.posRect.left +
					// // 0.65*b.posRect.right)
					// // - (GlobalVar.frameWidth/2);
					// b.posRect.centerX() - GlobalVar.frameWidth / 2;
					// GlobalVar.goalPos[1] = GlobalVar.frameHeight -
					// b.posRect.bottom;// .centerY();
					// GlobalVar.goalPos[2] = 1;
					biggestGoalBlob = b;
				}
				break;
			case GlobalVar.CYAN_BLOB:
				if (GlobalVar.oppTeamColor == GlobalVar.CYAN_BLOB) {
					GlobalVar.enemyPos[0] = b.posRect.centerX()
							- GlobalVar.frameWidth / 2;
					GlobalVar.enemyPos[1] = GlobalVar.frameHeight
							- b.posRect.centerY();
					GlobalVar.enemyPos[2] = 1;
					GlobalVar.enemyPos[3] = b.getSize();
				}
				break;
			case GlobalVar.MAGENTA_BLOB:
				if (GlobalVar.oppTeamColor == GlobalVar.MAGENTA_BLOB) {
					GlobalVar.enemyPos[0] = b.posRect.centerX()
							- GlobalVar.frameWidth / 2;
					GlobalVar.enemyPos[1] = GlobalVar.frameHeight
							- b.posRect.centerY();
					GlobalVar.enemyPos[2] = 1;
					GlobalVar.enemyPos[3] = b.getSize();
					out += "Enemy Seen!\n";
				}
				break;
			}
		}

		// See a goal
		if (biggestGoalBlob != null) {

			if (biggestGoalBlob.pixelCount > SINGLE_POLE_THRESHOLD
					&& biggestGoalBlob.posRect.width() < biggestGoalBlob.posRect
							.height() * 7 / 16) {
				// out += "SinglePole:" + lastGoalDirection + ","
				// + biggestGoalBlob.pixelCount + "\n";
			} else if (biggestGoalBlob.posRect.width() < biggestGoalBlob.posRect
					.height()) {
				// is not single pole && see a little upper pole beam
				if (biggestGoalBlob.centroidX < biggestGoalBlob.posRect
						.centerX())
					lastGoalDirection = 1;
				else
					lastGoalDirection = -1;

				// out += "compensate last goal direction:" + lastGoalDirection
				// + "\n";
			} else {
				lastGoalDirection = 0;

			}

			if (seenGoalLastTime) {
				// double delta = ((b.posRect.centerX() -
				// GlobalVar.frameWidth / 2) - mLastGoalStat[0]);

				double delta = ((biggestGoalBlob.posRect.centerX()
						- GlobalVar.frameWidth / 2 + lastGoalDirection
						* GlobalVar.frameWidth / 4) - mLastGoalStat[0]);
				if (delta > GlobalVar.frameWidth / UPDATE_FACTOR)
					delta = GlobalVar.frameWidth / UPDATE_FACTOR;
				else if (delta < -GlobalVar.frameWidth / UPDATE_FACTOR)
					delta = -GlobalVar.frameWidth / UPDATE_FACTOR;
				GlobalVar.goalPos[0] = mLastGoalStat[0] + delta;
				out += "Delta:" + delta;
			} else {
				// GlobalVar.goalPos[0] = b.posRect.centerX()
				// - GlobalVar.frameWidth / 2;
				out += "RENEW goal pos ------------------------";
				GlobalVar.goalPos[0] = biggestGoalBlob.posRect.centerX()
						- GlobalVar.frameWidth / 2;
			}

			mLastGoalStat[0] = GlobalVar.goalPos[0];
			GlobalVar.goalPos[1] = GlobalVar.frameHeight
					- biggestGoalBlob.posRect.bottom;
			GlobalVar.goalPos[2] = 1;
			seenGoalLastTime = true;
		}// end see a goal
		else {// not seen a goal
			seenGoalLastTime = false;
		}
		out += "\n";

		// not seen a ball, use old value
		if (!seenABall && mLastLoopSeen < KEEP_BALL_POSTION_PERIOD) {
			GlobalVar.ballPos[0] = mLastBallStat[0];
			GlobalVar.ballPos[1] = mLastBallStat[1];
			GlobalVar.ballPos[2] = mLastBallStat[2];
			GlobalVar.ballPos[3] = mLastBallStat[3];
			mLastLoopSeen++;
			Rect r = new Rect(lastRectSeen.left-15,lastRectSeen.top-15,
					lastRectSeen.right+15,lastRectSeen.bottom+15);

			// add here only for drawing
			GlobalVar.mergeResult.add(new BlobObject((byte) GlobalVar.BALL, r,
					0, 0));

		}
		// map to world coordinate not implement yet!!!

		return out;
	}

}
