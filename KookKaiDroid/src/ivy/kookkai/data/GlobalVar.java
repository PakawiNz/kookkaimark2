package ivy.kookkai.data;

import ivy.kookkai.ai.FetchBall;
import ivy.kookkai.refbox.Constants;
import ivy.kookkai.refbox.GameData;
import ivy.kookkai.refbox.KookKaiTeamInfo;
import ivy.kookkai.vision.BlobObject;
import ivy.kookkai.vision.ColorManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

public final class GlobalVar {

	public static final int KOOKKAI_MARK = 3;
	
	// object tag
	public static final int BALL = 11;
	public static final int GOAL = 12;
	public static final int CYAN_BLOB = 13;
	public static final int MAGENTA_BLOB = 14;

	public static final int FRAME_HEIGHT = 640;
	public static final int FRAME_WIDTH = 480;
	public static final int REMAP_FACTOR = 8;// TODO find out why data of homo_8 // is incorrect
	
	public static float GOAL_DIRECTION = (float) (-1.2);// range from -PI
	
	public static final float HEADING_ERROR_RANGE = (float) Math.PI * 7 / 16;
	// Accelerometer alignment at Homography calculation
	// TODO need precise recalibration with measurement
	// {(float)0.1,(float)8.2,(float)5.3};
	
	public static final double cameraFocalLength = 20;
	public static final double Y_ALIGNMENT = 8.2;
	public static final double THETA = Math.asin(Y_ALIGNMENT / 9.8);
	public static final double CAMERA_HEIGHT = 41.5 / Math.sin(THETA);

	public static final int oppTeamColor = MAGENTA_BLOB;
	public static final int myTeamNumber = 0;
	
	public static final String comIP = "192.168.1.107";
	public static final String twinIP = "192.168.1.123";

	public static ArrayList<BlobObject> blobResult;
	public static ArrayList<BlobObject> mergeResult;
	
	// robot reference object position (x,y,available,size of blob)
	// vision data, not considering of which side the goal is
	// ball size, enemy size are used, goal size not used
	public static double ballPos[] = new double[4];
	public static double goalPos[] = new double[4];
	
	public static double goalPosL[] = new double[4];
	public static double goalPosR[] = new double[4];
	
	public static double enemyPos[] = new double[4];

	public static float heading = 0;// range from -PI to PI
									// to PI!!
	
	public static void setHeading(float heading) {
		GlobalVar.heading = heading;
	}

	public static boolean isGoalDirection() {
		// NOTE Manual GOAL_DIRECTION adjust at second half
		float error = heading - GOAL_DIRECTION;
		if (error >= 0) {
			if (error <= HEADING_ERROR_RANGE) {
				Log.d("global_isgoal", "yes");
				return true;
			}
			if (2 * Math.PI - error <= HEADING_ERROR_RANGE) {
				Log.d("global_isgoal", "yes");
				return true;
			}
		} else {
			if (-HEADING_ERROR_RANGE < error) {
				Log.d("global_isgoal", "yes");
				return true;
			}
			if (2 * Math.PI + error <= HEADING_ERROR_RANGE) {
				Log.d("global_isgoal", "yes");
				return true;
			}
		}
		Log.d("global_isgoal", "no");
		return false;

	}

	public static JoystickData joyData = new JoystickData();

	public static int frameWidth;
	public static int frameHeight;

	public static GameData gameData;

	public static double ax, ay, az;

	public static void initVar() {
		GlobalVar.blobResult = new ArrayList<BlobObject>();
		ColorManager.initVar();
	}

	public static boolean committeeAllowMeToPlay(FetchBall fetcher) {
		if (GlobalVar.gameData.teams[0].teamNumber == 1) {
			// teaminfo = GlobalVar.gameData.teams[0];
			KookKaiTeamInfo.getInstance().setTeamInfo(0);
		} else {
			// teaminfo = GlobalVar.gameData.teams[1];
			KookKaiTeamInfo.getInstance().setTeamInfo(1);
		}

		// calculate deference between accelerometer value that we expect and
		// actual one.

		// Checking KickOff
		// Team[0] We are Cyan
		if ((KookKaiTeamInfo.getInstance().getTeamInfo() == GlobalVar.gameData.teams[0])) {
			// Check KickOff by Cyan //We're starting
			if (GlobalVar.gameData.kickOffTeam == 0) {
				// if game state is not playing so robot must stand still.
				if (GlobalVar.gameData.state == Constants.STATE_INITIAL
						|| GlobalVar.gameData.state == Constants.STATE_SET
						|| GlobalVar.gameData.state == Constants.STATE_FINISHED) {

					Log.d("WTF", "tate Stop");
					return false;
				}
				// READY:Change by following KickOff
				else if (GlobalVar.gameData.state == Constants.STATE_READY) {
					if (GlobalVar.myTeamNumber == 0) {
						Log.d("WTF", "State Ready");
						return false;
					}
					if (GlobalVar.myTeamNumber == 1) {
						fetcher.walkToSetupPosition();
					}
					if (GlobalVar.myTeamNumber == 2) {
						fetcher.walkToSetupPosition();
					}
				} else if (GlobalVar.gameData.state == Constants.STATE_PLAYING) {
					if (KookKaiTeamInfo.getInstance().getTeamInfo().player[GlobalVar.myTeamNumber].penalty != Constants.PENALTY_NONE) {
						Log.d("WTF", "State Stop");
						return false;
					} else {
						Log.d("WTF", "State Playing");
						return true;
					}
				}
			}
			// Check KickOff by Magenta //We're waiting
			else if (GlobalVar.gameData.kickOffTeam == 1) {
				// if game state is not playing so robot must stand still.
				if (GlobalVar.gameData.state == Constants.STATE_INITIAL
						|| GlobalVar.gameData.state == Constants.STATE_SET
						|| GlobalVar.gameData.state == Constants.STATE_FINISHED) {

					Log.d("WTF", "State Stop");
					return false;
				}
				// READY:Change by following KickOff
				else if (GlobalVar.gameData.state == Constants.STATE_READY) {
					if (GlobalVar.myTeamNumber == 0) {
						Log.d("WTF", "State Ready");
						return false;
					}
					if (GlobalVar.myTeamNumber == 1) {
						fetcher.walkToSetupPosition();
					}
					if (GlobalVar.myTeamNumber == 2) {
						fetcher.walkToSetupPosition();
					}
				} else if (GlobalVar.gameData.state == Constants.STATE_PLAYING) {
					if (KookKaiTeamInfo.getInstance().getTeamInfo().player[GlobalVar.myTeamNumber].penalty != Constants.PENALTY_NONE) {
						Log.d("WTF", "State Stop");
						return false;

					} else {

						Log.d("WTF", "State Playing");
						return true;
					}
				}
			} else if (GlobalVar.gameData.kickOffTeam == 2) {
				// if game state is not playing so robot must stand still.
				if (GlobalVar.gameData.state == Constants.STATE_INITIAL
						|| GlobalVar.gameData.state == Constants.STATE_SET
						|| GlobalVar.gameData.state == Constants.STATE_FINISHED) {

					Log.d("WTF", "tate Stop");
					return false;
				}
				// READY:Change by following KickOff
				else if (GlobalVar.gameData.state == Constants.STATE_READY) {
					if (GlobalVar.myTeamNumber == 0) {
						Log.d("WTF", "State Ready");
						return false;
					}
					if (GlobalVar.myTeamNumber == 1) {
						fetcher.walkToSetupPosition();
					}
					if (GlobalVar.myTeamNumber == 2) {
						fetcher.walkToSetupPosition();
					}
				} else if (GlobalVar.gameData.state == Constants.STATE_PLAYING) {
					if (KookKaiTeamInfo.getInstance().getTeamInfo().player[GlobalVar.myTeamNumber].penalty != Constants.PENALTY_NONE) {
						Log.d("WTF", "State Stop");
						return false;

					} else {

						Log.d("WTF", "State Playing");
						return true;
					}
				}
			}
		}
		// Team[1] //We're Magenta
		else if ((KookKaiTeamInfo.getInstance().getTeamInfo() == GlobalVar.gameData.teams[1])) {
			// Check KickOff by Cyan//We're waiting
			if (GlobalVar.gameData.kickOffTeam == 0) {
				// if game state is not playing so robot must stand still.
				if (GlobalVar.gameData.state == Constants.STATE_INITIAL
						|| GlobalVar.gameData.state == Constants.STATE_SET
						|| GlobalVar.gameData.state == Constants.STATE_FINISHED) {

					Log.d("WTF", "tate Stop");
					return false;
				}
				// READY:Change by following KickOff
				else if (GlobalVar.gameData.state == Constants.STATE_READY) {
					if (GlobalVar.myTeamNumber == 0) {
						Log.d("WTF", "State Ready");
						return false;
					}
					if (GlobalVar.myTeamNumber == 1) {
						fetcher.walkToSetupPosition();
					}
					if (GlobalVar.myTeamNumber == 2) {
						fetcher.walkToSetupPosition();
					}
				} else if (GlobalVar.gameData.state == Constants.STATE_PLAYING) {
					if (KookKaiTeamInfo.getInstance().getTeamInfo().player[GlobalVar.myTeamNumber].penalty != Constants.PENALTY_NONE) {
						Log.d("WTF", "State Stop");
						return false;

					} else {

						Log.d("WTF", "State Playing");
						return true;
					}
				}
			}
			// Check KickOff by Magenta //We're starting
			else if (GlobalVar.gameData.kickOffTeam == 1) {
				// if game state is not playing so robot must stand still.
				if (GlobalVar.gameData.state == Constants.STATE_INITIAL
						|| GlobalVar.gameData.state == Constants.STATE_SET
						|| GlobalVar.gameData.state == Constants.STATE_FINISHED) {

					Log.d("WTF", "State Stop");
					return false;
				}
				// READY:Change by following KickOff
				else if (GlobalVar.gameData.state == Constants.STATE_READY) {
					if (GlobalVar.myTeamNumber == 0) {
						Log.d("WTF", "State Ready");
						return false;
					}
					if (GlobalVar.myTeamNumber == 1) {
						fetcher.walkToSetupPosition();
					}
					if (GlobalVar.myTeamNumber == 2) {
						fetcher.walkToSetupPosition();
					}
				} else if (GlobalVar.gameData.state == Constants.STATE_PLAYING) {
					if (KookKaiTeamInfo.getInstance().getTeamInfo().player[GlobalVar.myTeamNumber].penalty != Constants.PENALTY_NONE) {
						Log.d("WTF", "State Stop");
						return false;

					} else {

						Log.d("WTF", "State Playing");
						return true;
					}
				}
			}
		}
		return true;
	}

}
