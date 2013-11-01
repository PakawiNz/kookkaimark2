package ivy.kookkai;

import ivy.kookkai.ai.AITemplate;
import ivy.kookkai.ai.PakawiNz_AI;
import ivy.kookkai.api.KookKaiAndroidAPI;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.debugview.CameraInterface;
import ivy.kookkai.debugview.DebugImgView;
import ivy.kookkai.refbox.Constants;
import ivy.kookkai.refbox.GameControllerClient;
import ivy.kookkai.refbox.GameData;
import ivy.kookkai.refbox.KookKaiTeamInfo;
import ivy.kookkai.vision.Blob;
import ivy.kookkai.vision.BlobAnalyser;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.udpfootballer.KookKaiTwin;
import com.example.udpfootballer.MessageBundle;
import com.example.udpfootballer.ReceiverListener;
import com.example.udpfootballer.KookKaiStateAndAction;
import com.example.udpfootballer.UDPClient;
import com.example.udpfootballer.UDPServer;

public class MainlLoop implements Runnable {
	
	private final Handler handler = new Handler();
	private CameraInterface camInterface;
	private DebugImgView debugImg;
	private boolean running;
	
	private TextView debugText;
	private String outString;
	private Blob visionBlob;
	private KookKaiAndroidAPI robotAPI;
	private BlobAnalyser mapBlob;
	
	private AITemplate ai;
	private GameControllerClient gameClient;
	private UDPClient client;
	private UDPServer server;
	private MessageBundle outMessage;
	private MessageBundle inboxMessage;
	private KookKaiStateAndAction myState;
	private KookKaiTwin twin;

	private CheckBox drawColorCheck;

	public MainlLoop(CameraInterface cam, DebugImgView dimg,
			TextView dtext, CheckBox drawColor
			) {
		camInterface = cam;
		debugImg = dimg;
		debugText = dtext;
		drawColorCheck = drawColor;
		outMessage = new MessageBundle();
		inboxMessage = new MessageBundle();
		robotAPI = new KookKaiAndroidAPI(4567);
		
		ai = new PakawiNz_AI(robotAPI);
		
		visionBlob = new Blob(camInterface.frameWidth / 2,  camInterface.frameHeight / 2, debugImg);
		mapBlob = new BlobAnalyser();
		
		gameClient = new GameControllerClient(Constants.NETWORK_DATA_PORT);
		client = new UDPClient();
		client.add(GlobalVar.twinIP);
		client.add(GlobalVar.comIP);
		server = new UDPServer();
		server.setOnReceiveListener(new ReceiverListener() {
			@Override
			public void onReceive(byte[] a) {
				inboxMessage.setBytes(a);
				inboxMessage.markTimeStamp();
				twin.setStateAndAction(inboxMessage.getStateAndAction());
				twin.setBallPixels(inboxMessage.getBallPixels());
				twin.markTimeStamp();
			}
		});
		server.start();
		myState = KookKaiStateAndAction.getInstance();
		twin = KookKaiTwin.getInstance();
		GlobalVar.initVar();
		
		// frame is resized and rotated
		GlobalVar.frameHeight = camInterface.frameWidth / 2;
		GlobalVar.frameWidth = camInterface.frameHeight / 2;
	}

	public void start() {
		handler.removeCallbacks(this);
		handler.postDelayed(this, 100);
		running = true;
		gameClient.startClient();
		Network.createThread();
	}

	public void stop() {
		handler.removeCallbacks(this);
		running = false;
		robotAPI.stand_still();
		robotAPI.disconnect();
		gameClient.stopClient();
		Network.destroyThread();
	}

	public void run() {
		long timeStamp = System.currentTimeMillis();
		// execute vision
		debugImg.reset();
		
		//Message Sent
//		if(GlobalVar.ballPos[2] > 0) 
//			outMessage.setBallPixels((int) GlobalVar.ballPos[3]);
//		else
//			outMessage.setBallPixels(0);
//		Log.d("WTF", "asdf");
//		if(GlobalVar.enemyPos[2] > 0)
//			myState.setState(KookKaiStateAndAction.STATE_ENEMY_FOUND);
//		else
//			myState.unSetState(KookKaiStateAndAction.STATE_ENEMY_FOUND);
//		outMessage.setStateAndAction(myState.getStateAndAction());
//		
//		client.sendMessage(outMessage.getBytes());
		outString = "ROBOT [MK" + GlobalVar.KOOKKAI_MARK + "]" + "\n";
//		outString += robotAPI.getServerStatus();
//		outString += "\n-- Me Myself -- \n";
//		outString += "My State : " + myState.toString() + "\n";
//		outString += "My Pixel : " + GlobalVar.ballPos[3] + "\n";
//		outString += "\n-- Network -- \n";
//		outString += "Friends' Ball Pixels : " + inboxMessage.getBallPixels() + "\n";
//		outString += "Friends' State : " + inboxMessage.toString() + "\n";
//		outString += "\n";
		
		GameData gameData = gameClient.getGameData();
		GlobalVar.gameData = gameData;
		int timeleft = gameData.secsRemaining;
		
		outString += (timeleft/60) + ":" +(timeleft%60)+"\n"; 

		byte[] cbcr = camInterface.getCbCr();
		byte[] y = camInterface.getYPrime();
		
		//NOTE: execute will paint blob
		outString += visionBlob.execute(y,cbcr, drawColorCheck.isChecked());
		outString += mapBlob.execute();
		for (int i = 0; i < GlobalVar.blobResult.size(); i++) {
			if(GlobalVar.blobResult.get(i).tag==GlobalVar.BALL){
				debugImg.drawRect(GlobalVar.blobResult.get(i).posRect, Color.GREEN);
			}
			else{
				debugImg.drawRect(GlobalVar.blobResult.get(i).posRect, Color.WHITE);
			}
		}
		for (int i = 0; i < GlobalVar.mergeResult.size(); i++) {
			if(GlobalVar.mergeResult.get(i).tag==GlobalVar.BALL){
				debugImg.drawRect(GlobalVar.mergeResult.get(i).posRect, Color.RED);
			}
			else{
				debugImg.drawRect(GlobalVar.mergeResult.get(i).posRect, Color.BLACK);
			}
		}

		// execute AI
		outString += ai.execute();

		// execute Control
		debugText.setText(outString);
		debugImg.invalidate(); //Important: must be last because it invalidates the whole view

		long timeDiff = System.currentTimeMillis() - timeStamp;
//		debugText.append("\n" + timeDiff + "ms");
//		debugText.append("\n"+KookKaiTeamInfo.getInstance().getTeamInfo().teamNumber);
//		if(KookKaiTeamInfo.getInstance().getTeamInfo().teamColour==0)debugText.append("\n"+"Team:Blue");
//		else debugText.append("\n"+"Team:Red");
//		
//		if(KookKaiTeamInfo.getInstance().getTeamInfo().goalColour==0)debugText.append("\n"+"Goal:Blue");
//		else debugText.append("\n"+"Goal:Yellow");
//		
//		if(gameData.firstHalf==1)
//			debugText.append("\n"+"FirstHalf");
//			else debugText.append("\n"+"SecondHalf");
//			if(gameData.state==0) debugText.append("\n"+"State:Initial");
//			else if(gameData.state==1 ) debugText.append("\n"+"State:Ready");
//			else if(gameData.state==2) debugText.append("\n"+"State:Set");
//			else if(gameData.state==3) debugText.append("\n"+"State:Play");
//			else if(gameData.state==4) debugText.append("\n"+"State:Finish");			
//			
//		if(gameData.secondaryState==0) debugText.append("\n"+"Extra:Normal");
//		else if(gameData.secondaryState==1) debugText.append("\n"+"Extra:PenaltyShoot");
//		else if(gameData.secondaryState==2) debugText.append("\n"+"Extra:Overtime");
//		
//		for(byte i=0;i< Constants.MAX_NUM_PLAYERS-8;i++){
//			if(KookKaiTeamInfo.getInstance().getTeamInfo().player[i].penalty==0)  debugText.append("\n"+"Player"+i+"Penalty:None");
//			else if(KookKaiTeamInfo.getInstance().getTeamInfo().player[i].penalty==1)  debugText.append("\n"+"Player"+i+" Penalty:Ball Manipulation");
//			else if(KookKaiTeamInfo.getInstance().getTeamInfo().player[i].penalty==2)  debugText.append("\n"+"Player"+i+" Penalty:Ball Physical Contact");
//			else if(KookKaiTeamInfo.getInstance().getTeamInfo().player[i].penalty==3)  debugText.append("\n"+"Player"+i+" Penalty:Illegal Attack");
//			else if(KookKaiTeamInfo.getInstance().getTeamInfo().player[i].penalty==4)  debugText.append("\n"+"Player"+i+" Penalty:Illegal Defence");
//			else if(KookKaiTeamInfo.getInstance().getTeamInfo().player[i].penalty==5)  debugText.append("\n"+"Player"+i+" Penalty:Request for Pickup");
//			else if(KookKaiTeamInfo.getInstance().getTeamInfo().player[i].penalty==6)  debugText.append("\n"+"Player"+i+" Penalty:Request for Service");
//			else if(KookKaiTeamInfo.getInstance().getTeamInfo().player[i].penalty==7)  debugText.append("\n"+"Player"+i+" Penalty:Upgrade Pickup to Service");
//		}
		if (running) {
			if (timeDiff < 90)
				handler.postDelayed(this, 100 - timeDiff);
			else
				handler.postDelayed(this, 10);
		}
	}

}
