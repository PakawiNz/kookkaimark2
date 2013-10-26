package ivy.kookkai.refbox;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import android.util.Log;


public class GameControllerClient extends Thread{

	private boolean running = true;
	private int port;
	private GameData gameData;
	
	public GameControllerClient(int revPort){
		this.port = revPort;
		gameData = new GameData();
		
	}
	
	
	public synchronized GameData getGameData(){
		return this.gameData;
		
	}
	
	public void startClient(){
		running = true;
		this.start();
		Log.d("socket", "client start");
	}
	
	public void stopClient(){
		running = false;
	}
	
	public void run(){
		
		try {
			DatagramSocket s = new DatagramSocket(null);
			s.setReuseAddress(true);
			s.bind(new InetSocketAddress(port));
			s.setSoTimeout(1000);

			ByteBuffer buffer = ByteBuffer.allocate(1000);
			buffer.order(Constants.NETWORK_BYTEORDER);
			byte[] data = buffer.array();
			DatagramPacket p = new DatagramPacket(data, data.length);

			while (running) {
				try {
					
					s.receive(p);
					buffer.rewind();
					getFromByteArray(buffer);
					
					
				} catch (SocketTimeoutException ste) {
					
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	


	public synchronized boolean getFromByteArray(ByteBuffer buffer) {
		byte[] header = new byte[4];
		buffer.get(header, 0, 4);
		/*String s = new String(header);
		if(s.compareTo(Constants.STRUCT_HEADER) !=0 ){
			return false;	
		}*/
		
		int version = buffer.getInt();
		if (version != Constants.STRUCT_VERSION) {
			return false;
		}

		
		gameData.playersPerTeam = buffer.get();       // The number of players on a team
		gameData.state = buffer.get() ;                // state of the game (STATE_READY, STATE_PLAYING, etc)
		gameData.firstHalf = buffer.get() ;            // 1 = game in first half, 0 otherwise
		gameData.kickOffTeam = buffer.get();          // the next team to kick off
		gameData.secondaryState = buffer.get();       // Extra state information - (STATE2_NORMAL, STATE2_PENALTYSHOOT, etc)
		gameData.dropInTeam = buffer.get();           // team that caused last drop in
		gameData.dropInTime = buffer.getShort();          // number of seconds passed since the last drop in.  -1 before first dropin
		gameData.secsRemaining = buffer.getInt();  
		
		
		for (byte team = 0; team < Constants.NUM_TEAMS; team++) {
			gameData.teams[team].teamNumber = buffer.get();
			gameData.teams[team].teamColour = buffer.get();
			gameData.teams[team].goalColour = buffer.get();
			gameData.teams[team].score = buffer.get();
			
			for(byte i=0;i< Constants.MAX_NUM_PLAYERS;i++){
				gameData.teams[team].player[i].penalty = buffer.getShort();
				gameData.teams[team].player[i].secsTillUnpenalised = buffer.getShort();
			}
			
		}
		return true;
	}
	
}


