package ivy.kookkai.data;

import ivy.kookkai.ai.FetchBall;
import ivy.kookkai.refbox.Constants;
import ivy.kookkai.refbox.GameData;
import ivy.kookkai.refbox.KookKaiTeamInfo;
import ivy.kookkai.vision.BlobObject;

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
	public static float GOAL_DIRECTION = (float) (-1.2);// range from -PI
	
	public static ArrayList<ColorPlate> colorList = new ArrayList<ColorPlate>();
	public static ArrayList<BlobObject> blobResult;
	public static ArrayList<BlobObject> mergeResult;

	public final static int ORANGE = 1;
	public final static int YELLOW = 2;
	public final static int CYAN = 3;
	public final static int MAGENTA = 4;
	public final static int GREEN = 5;
	public final static int WHITE = 6;
	public final static int BLACK = 7;
	// object tag
	public final static int BALL = 11;
	public final static int GOAL = 12;
	public final static int CYAN_BLOB = 13;
	public final static int MAGENTA_BLOB = 14;
	public static int oppTeamColor = MAGENTA_BLOB;
	
	public static int myTeamNumber = 0;
	public static String comIP = "192.168.1.107";
	public static String twinIP = "192.168.1.123";

	private static final String colorfile = "/KorKai/color.txt";

	public final static int FRAME_HEIGHT = 640;
	public final static int FRAME_WIDTH = 480;
	public final static int REMAP_FACTOR = 8;// TODO find out why data of homo_8
												// is incorrect

	public static double cameraFocalLength = 20;
	public static byte[][] crcbHashMap = new byte[256][256];
	public static int[] rColor;

	// robot reference object position (x,y,available,size of blob)
	// vision data, not considering of which side the goal is
	// ball size, enemy size are used, goal size not used
	public static double ballPos[] = new double[4];
	public static double goalPos[] = new double[4];
	public static double enemyPos[] = new double[4];

	public static float heading = 0;// range from -PI to PI
																// to PI!!
	public static final float HEADING_ERROR_RANGE = (float) Math.PI * 7 / 16;
	// Accelerometer alignment at Homography calculation
	// TODO need precise recalibration with measurement
	// {(float)0.1,(float)8.2,(float)5.3};
	public static final double Y_ALIGNMENT = 8.2;
	public static final double THETA = Math.asin(Y_ALIGNMENT / 9.8);
	public static final double CAMERA_HEIGHT = 41.5 / Math.sin(THETA);

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
		blobResult = new ArrayList<BlobObject>();

		// /////////////////

		try {
			readColorList();
			Log.d("GlobalVar_init", "read color list succeeded");
		} catch (Exception e) {
			// e.printStackTrace();
			System.err.println(e.getMessage());
			Log.d("GlobalVar_init", "fail to read color list");
			// default value
			colorList.clear();
			colorList.add(new ColorPlate(ORANGE, Color.RED, 160, 255, 0, 90));
			colorList
					.add(new ColorPlate(YELLOW, Color.YELLOW, 114, 145, 0, 82));
			colorList.add(new ColorPlate(CYAN, Color.CYAN, 0, 128, 160, 200));
			colorList.add(new ColorPlate(MAGENTA, Color.MAGENTA, 135, 255, 135,
					255));
			colorList.add(new ColorPlate(GREEN, Color.GREEN, 0, 120, 0, 108));
			colorList.add(new ColorPlate(WHITE, Color.WHITE, 0, 42, 0, 16));
			colorList.add(new ColorPlate(BLACK, Color.BLACK, 10, 20, 0, 10));

		}

		createColorHashMap();

	}
	
	public static boolean committeeAllowMeToPlay(FetchBall fetcher) {
		if (GlobalVar.gameData.teams[0].teamNumber == 1) {
			//teaminfo = GlobalVar.gameData.teams[0];
			KookKaiTeamInfo.getInstance().setTeamInfo(0);
		} else {
		//teaminfo = GlobalVar.gameData.teams[1];
		KookKaiTeamInfo.getInstance().setTeamInfo(1);
		}
		
		// calculate deference between accelerometer value that we expect and
		// actual one.
	
	
		//Checking KickOff
		//Team[0] We are Cyan
		if((KookKaiTeamInfo.getInstance().getTeamInfo()==GlobalVar.gameData.teams[0])){
				//Check KickOff by Cyan //We're starting
		if(GlobalVar.gameData.kickOffTeam==0){
		 // if game state is not playing so robot must stand still.
			if(GlobalVar.gameData.state == Constants.STATE_INITIAL ||GlobalVar.gameData.state == Constants.STATE_SET ||GlobalVar.gameData.state == Constants.STATE_FINISHED  ){
				 
				Log.d("WTF","tate Stop");
				return false;
			}
			//READY:Change by following KickOff 
			else if(GlobalVar.gameData.state == Constants.STATE_READY){
				if(GlobalVar.myTeamNumber==0){ Log.d("WTF","State Ready"); return false; }
				if(GlobalVar.myTeamNumber==1){fetcher.walkToSetupPosition();}
				if(GlobalVar.myTeamNumber==2){ fetcher.walkToSetupPosition();}
				}
			else if(GlobalVar.gameData.state == Constants.STATE_PLAYING ) {
				if (KookKaiTeamInfo.getInstance().getTeamInfo().player[GlobalVar.myTeamNumber].penalty != Constants.PENALTY_NONE) {
					Log.d("WTF","State Stop");
					return false;
				
				}
			 	else{
			 	
			 		Log.d("WTF","State Playing");
			 		return true;
			 	}
			}
		}
		//Check KickOff by Magenta  //We're waiting
		else if(GlobalVar.gameData.kickOffTeam==1){
			 // if game state is not playing so robot must stand still.
				if(GlobalVar.gameData.state == Constants.STATE_INITIAL ||GlobalVar.gameData.state == Constants.STATE_SET ||GlobalVar.gameData.state == Constants.STATE_FINISHED  ){
					 
					Log.d("WTF","State Stop");
					return false;
				}
				//READY:Change by following KickOff 
				else if(GlobalVar.gameData.state == Constants.STATE_READY){
					if(GlobalVar.myTeamNumber==0){ Log.d("WTF","State Ready"); return false;}
					if(GlobalVar.myTeamNumber==1){fetcher.walkToSetupPosition();}
					if(GlobalVar.myTeamNumber==2){ fetcher.walkToSetupPosition();}
					}
				else if(GlobalVar.gameData.state == Constants.STATE_PLAYING ) {
					if (KookKaiTeamInfo.getInstance().getTeamInfo().player[GlobalVar.myTeamNumber].penalty != Constants.PENALTY_NONE) {
						Log.d("WTF","State Stop");
						return false;
					
					}
				 	else{
				 	
				 		Log.d("WTF","State Playing");
				 		return true;
				 	}
				}
			}
		else 	if(GlobalVar.gameData.kickOffTeam==2){
			 // if game state is not playing so robot must stand still.
				if(GlobalVar.gameData.state == Constants.STATE_INITIAL ||GlobalVar.gameData.state == Constants.STATE_SET ||GlobalVar.gameData.state == Constants.STATE_FINISHED  ){
					 
					Log.d("WTF","tate Stop");
					return false;
				}
				//READY:Change by following KickOff 
				else if(GlobalVar.gameData.state == Constants.STATE_READY){
					if(GlobalVar.myTeamNumber==0){ Log.d("WTF","State Ready"); return false; }
					if(GlobalVar.myTeamNumber==1){fetcher.walkToSetupPosition();}
					if(GlobalVar.myTeamNumber==2){ fetcher.walkToSetupPosition();}
					}
				else if(GlobalVar.gameData.state == Constants.STATE_PLAYING ) {
					if (KookKaiTeamInfo.getInstance().getTeamInfo().player[GlobalVar.myTeamNumber].penalty != Constants.PENALTY_NONE) {
						Log.d("WTF","State Stop");
						return false;
					
					}
				 	else{
				 	
				 		Log.d("WTF","State Playing");
				 		return true;
				 	}
				}
			}
		}
		//Team[1] //We're Magenta
		else if((KookKaiTeamInfo.getInstance().getTeamInfo()==GlobalVar.gameData.teams[1])){
			//Check KickOff by Cyan//We're waiting
			if(GlobalVar.gameData.kickOffTeam==0){
				 // if game state is not playing so robot must stand still.
				if(GlobalVar.gameData.state == Constants.STATE_INITIAL ||GlobalVar.gameData.state == Constants.STATE_SET ||GlobalVar.gameData.state == Constants.STATE_FINISHED  ){
					 
					Log.d("WTF","tate Stop");
					return false;
				}
				//READY:Change by following KickOff 
				else if(GlobalVar.gameData.state == Constants.STATE_READY){
					if(GlobalVar.myTeamNumber==0){ Log.d("WTF","State Ready"); return false;}
					if(GlobalVar.myTeamNumber==1){fetcher.walkToSetupPosition();}
					if(GlobalVar.myTeamNumber==2){ fetcher.walkToSetupPosition();}
					}
				else if(GlobalVar.gameData.state == Constants.STATE_PLAYING ) {
					if (KookKaiTeamInfo.getInstance().getTeamInfo().player[GlobalVar.myTeamNumber].penalty != Constants.PENALTY_NONE) {
						Log.d("WTF","State Stop");
						return  false;
					
					}
				 	else{
				 	
				 		Log.d("WTF","State Playing");
				 		return true;
				 	}
				}			}
				//Check KickOff by Magenta //We're starting
				else if(GlobalVar.gameData.kickOffTeam==1){
					 // if game state is not playing so robot must stand still.
					if(GlobalVar.gameData.state == Constants.STATE_INITIAL ||GlobalVar.gameData.state == Constants.STATE_SET ||GlobalVar.gameData.state == Constants.STATE_FINISHED  ){
						 
						Log.d("WTF","State Stop");
						return false;
					}
					//READY:Change by following KickOff 
					else if(GlobalVar.gameData.state == Constants.STATE_READY){
						if(GlobalVar.myTeamNumber==0){ Log.d("WTF","State Ready");return false; }
						if(GlobalVar.myTeamNumber==1){fetcher.walkToSetupPosition();}
						if(GlobalVar.myTeamNumber==2){ fetcher.walkToSetupPosition();}
						}
					else if(GlobalVar.gameData.state == Constants.STATE_PLAYING ) {
						if (KookKaiTeamInfo.getInstance().getTeamInfo().player[GlobalVar.myTeamNumber].penalty != Constants.PENALTY_NONE) {
							Log.d("WTF","State Stop");
							return false;
						
						}
					 	else{
					 	
					 		Log.d("WTF","State Playing");
					 		return true;
					 	}
					}				}			
			}
		return true;
	}

	public static void readColorList() throws Exception {
		String path = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		String filename = path + colorfile;
		File state = new File(filename);
		if (!state.exists()) {
			throw new Exception("'" + filename + "' doesn't exist");
		} else {
			// state.createNewFile();
			// FileWriter w = new FileWriter(state);
			// w.write("1\n");
			// w.close();

			BufferedReader fr = new BufferedReader(new FileReader(filename));
			int cPrams[] = new int[28];
			int x = 0;
			String line;
			while ((line = fr.readLine()) != null) {
				String tokens[] = line.trim().split(" ");
				for (int i = 0; i < tokens.length; i++) {
					try {
						cPrams[x++] = Integer.parseInt(tokens[i]);
					} catch (Exception e) {

					}
				}
			}
			fr.close();

			if (x != cPrams.length)// 28)
				throw new Exception("'" + filename + "' invalid format");
			// red then blue
			colorList.clear();
			colorList.add(new ColorPlate(ORANGE, Color.RED, cPrams[0],
					cPrams[1], cPrams[2], cPrams[3]));
			colorList.add(new ColorPlate(YELLOW, Color.YELLOW, cPrams[4],
					cPrams[5], cPrams[6], cPrams[7]));
			/*
			 * colorList.add(new ColorPlate(BLUE, Color.BLUE, cPrams[8],
			 * cPrams[9], cPrams[10], cPrams[11])); colorList.add(new
			 * ColorPlate(CYAN, Color.CYAN, cPrams[12], cPrams[13], cPrams[14],
			 * cPrams[15])); colorList.add(new ColorPlate(MAGENTA,
			 * Color.MAGENTA, cPrams[16], cPrams[17], cPrams[18], cPrams[19]));
			 * colorList.add(new ColorPlate(GREEN, Color.GREEN, cPrams[20],
			 * cPrams[21], cPrams[22], cPrams[23]));
			 */
			colorList.add(new ColorPlate(CYAN, Color.CYAN, cPrams[8],
					cPrams[9], cPrams[10], cPrams[11]));
			colorList.add(new ColorPlate(MAGENTA, Color.MAGENTA, cPrams[12],
					cPrams[13], cPrams[14], cPrams[15]));
			colorList.add(new ColorPlate(GREEN, Color.GREEN, cPrams[16],
					cPrams[17], cPrams[18], cPrams[19]));
			colorList.add(new ColorPlate(WHITE, Color.WHITE, cPrams[20],
					cPrams[21], cPrams[22], cPrams[23]));
			colorList.add(new ColorPlate(BLACK, Color.BLACK, cPrams[24],
					cPrams[25], cPrams[26], cPrams[27]));

		}
	}

	public static void writeColorList() throws IOException {
		String path = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		String filename = path + colorfile;

		File folder = new File(path + "/KorKai");
		if (!folder.exists()) {
			System.out.println("create folder " + path + "/KorKai");
			folder.mkdirs();
			System.out.println("folder created");
		}

		File state = new File(filename);
		if (!state.exists()) {
			System.out.println("create new file " + filename);
			state.createNewFile();
			System.out.println("file created");
		}
		FileWriter w = new FileWriter(state);
		for (int i = 0; i < colorList.size(); i++) {
			ColorPlate cp = colorList.get(i);
			w.write(cp.minCr + " " + cp.maxCr + " " + cp.minCb + " " + cp.maxCb
					+ "\n");
		}
		w.close();

		initVar();
	}

	public static int getTagColor(int tag) {
		return rColor[tag % 10];

	}

	public static void createColorHashMap() {

		rColor = new int[colorList.size() + 1];
		rColor[0] = 0;
		ColorPlate[] cPlate = new ColorPlate[GlobalVar.colorList.size()];
		for (int i = 0; i < GlobalVar.colorList.size(); i++) {
			cPlate[i] = GlobalVar.colorList.get(i);
			rColor[i + 1] = cPlate[i].color;
		}

		for (int i = 0; i < crcbHashMap.length; i++) {
			for (int j = 0; j < crcbHashMap[0].length; j++) {
				crcbHashMap[i][j] = 0;
				for (int k = 0; k < cPlate.length; k++) {
					if (cPlate[k].isThisColor(i, j)) {
						crcbHashMap[i][j] = (byte) (k + 1);// cPlate[k].tag;
						break;
					}
				}
			}
		}

	}

}
