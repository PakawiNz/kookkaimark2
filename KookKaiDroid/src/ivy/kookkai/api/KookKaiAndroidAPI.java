package ivy.kookkai.api;

import ivy.kookkai.MainlLoop;
import ivy.kookkai.ai.FetchBall;
import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.localization.Humanoid;

import java.io.IOException;

import org.microbridge.server.Server;

import android.util.Log;

public class KookKaiAndroidAPI {

	public static final int EEPROM_BLOCK_SIZE = 60;
	public static final int NUMBER_OF_JOINTS = 16;
	public static final int NUMBER_OF_FRAME = 7;
	public static final int NUMBER_OF_GAIT = 9;
	public static final int SERVO_CENTER = 1500;

	// private CommPortIdentifier portid = null ;
	// private SerialPort serialport = null;
	// private OutputStream outSerial;
	// private InputStream inSerial;
	private byte[] readBuffer = new byte[200];
	private byte[] writeBuffer = new byte[200];
	private final byte[] header = { '$', '$' };
	private final byte[] ending = { '*', '*' };

	private Server server;

	public KookKaiAndroidAPI(int port) {
		openPort(port);
	}

	public void disconnect() {
		server.stop();
	}
	
	public String getServerStatus() {

		return "port:" + server.getPort() + " running:" + server.isRunning() + "\n";
	}

	private boolean openPort(int port) {

		server = new Server(port);
		/*
		 * server.addListener(new AbstractServerListener() {
		 * 
		 * @Override public void onReceive(org.microbridge.server.Client client,
		 * byte[] data) { revData = data;
		 * 
		 * }; });
		 */

		try {
			server.start();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return true;

	}

	/*
	 * public void setHome(int[] joint) { try {
	 * 
	 * byte[] cmd = {'s','h','m'}; sendHeader(cmd); int k =0;
	 * 
	 * for(int i=0;i<NUMBER_OF_JOINTS;i++){ System.out.print(joint[i]+ " ");
	 * writeBuffer[i*2] = (byte) (0xff & joint[i]); writeBuffer[i*2+1] = (byte)
	 * (0xff & (joint[i]>>8 )); k+=2; } System.out.println();
	 * for(;k<EEPROM_BLOCK_SIZE;k++){ writeBuffer[k] = 0; }
	 * outSerial.write(writeBuffer, 0, EEPROM_BLOCK_SIZE); sendEnding(); } catch
	 * (Exception e) { e.printStackTrace(); } }
	 */

	/*
	 * public int[] getHome() { int[] joint = new int[NUMBER_OF_JOINTS]; byte[]
	 * cmd = {'g','h','m'}; int index =0;
	 * 
	 * try { clearSerialBuffer(); sendHeader(cmd); sendEnding();
	 * 
	 * while(index < EEPROM_BLOCK_SIZE){ int len = inSerial.read(readBuffer,
	 * index, readBuffer.length-index);
	 * 
	 * index +=len;
	 * 
	 * }
	 * 
	 * for(int i=0;i<NUMBER_OF_JOINTS;i++){ joint[i] = (0xff & readBuffer[i*2])
	 * | ((0xff &readBuffer[i*2+1])<<8); System.out.print(readBuffer[i*2]+ " "
	 * +readBuffer[i*2+1]+" "); System.out.println(joint[i]);
	 * 
	 * } System.out.println();
	 * 
	 * } catch (Exception e) { e.printStackTrace(); }
	 * 
	 * return joint; }
	 */

	public void playSaveMotion(int index) {
		try {

			// clearSerialBuffer();

			byte[] cmd = { 'm', 'o', 'v' };
			sendHeader(cmd);
			byte[] out = new byte[1];
			out[0] = (byte) index;
			server.send(out);

			// outSerial.write(index);
			sendEnding();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void walking(int x, int y, int z) {
		
		//original 15
		/* MK 2 = 12 */
		/* MK 3 = 12 */
		if (x > 12) {
			x = 12;
		}
		//original -10
		if (x < -15)
			x = -15;
		//original = 10
		if (y > 20)
			y = 20;
		//original -10
		if (y < -10)
			y = -10;
		//original 100
		if (z > 150)
			z = 200;
		//original -100
		if (z < -150)
			z = -150;

		if (z >= 150 || z <= -150) {
			y = -10;
			x = -10;
		}
		walkingNonLimit(x,y,z);
	}

	public void walkingNonLimit(int x , int y, int z){
		try {
			byte[] cmd = { 'w', 'a', 'k' };
			sendHeader(cmd);
			if(GlobalVar.KOOKKAI_MARK==3){
				z*=-1;//TODO WARNING
			}
			Log.d("api_wak",""+x+","+y+","+z);
			writeBuffer[0] = (byte) x;
			writeBuffer[1] = (byte) (x >> 8);
			writeBuffer[2] = (byte) y;
			writeBuffer[3] = (byte) (y >> 8);
			writeBuffer[4] = (byte) z;
			writeBuffer[5] = (byte) (z >> 8);
			byte[] out = new byte[6];
			System.arraycopy(writeBuffer, 0, out, 0, 6);
			server.send(out);
			// outSerial.write(writeBuffer, 0, 6);
			sendEnding();
			
			//TODO localization part for motion model
			Humanoid.incrementMotion(0, 1.4, 0);
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setJoints(int startIndex, int count, int[] joints, int speed) {
		try {

			byte[] cmd = { 's', 'e', 't' };
			sendHeader(cmd);

			writeBuffer[0] = (byte) speed;
			writeBuffer[1] = (byte) (speed >> 8);
			writeBuffer[2] = (byte) startIndex;
			writeBuffer[3] = (byte) count;

			for (int i = 0; i < count; i++) {
				writeBuffer[4 + i * 2] = (byte) (0xff & joints[i]);
				writeBuffer[4 + i * 2 + 1] = (byte) (0xff & (joints[i] >> 8));

			}
			// outSerial.write(writeBuffer, 0, 4+count*2);
			byte[] out = new byte[4 + count * 2];
			System.arraycopy(writeBuffer, 0, out, 0, out.length);
			server.send(out);

			sendEnding();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setJointsRaw(int startIndex, int count, int[] joints, int speed) {
		try {

			byte[] cmd = { 's', 'e', 'r' };
			sendHeader(cmd);

			writeBuffer[0] = (byte) speed;
			writeBuffer[1] = (byte) (speed >> 8);
			writeBuffer[2] = (byte) startIndex;
			writeBuffer[3] = (byte) count;

			for (int i = 0; i < count; i++) {
				writeBuffer[4 + i * 2] = (byte) (0xff & joints[i]);
				writeBuffer[4 + i * 2 + 1] = (byte) (0xff & (joints[i] >> 8));

			}
			// outSerial.write(writeBuffer, 0, 4+count*2);
			byte[] out = new byte[4 + count * 2];
			System.arraycopy(writeBuffer, 0, out, 0, out.length);
			server.send(out);
			sendEnding();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void freeMotor() {
		try {
			byte[] cmd = { 'f', 'r', 'e' };
			sendHeader(cmd);
			sendEnding();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * public int[] getJoints(int startIndex,int count) { int[] joints = new
	 * int[count]; byte[] buffer = new byte[count*2+2];
	 * 
	 * writeBuffer[0] = (byte)startIndex; writeBuffer[1] = (byte)count;
	 * 
	 * try { clearSerialBuffer(); byte[] cmd = {'g','e','t'}; sendHeader(cmd);
	 * outSerial.write(writeBuffer, 0, 2); sendEnding();
	 * 
	 * int index=0; while(index < count*2){ index += inSerial.read(readBuffer,
	 * index, 200); }
	 * 
	 * for (int i = 0; i < count; i++) { joints[i] = readBuffer[i * 2] |
	 * (readBuffer[i * 2 + 1] << 8); }
	 * 
	 * } catch (Exception e) { e.printStackTrace(); return null; }
	 * 
	 * return joints;
	 * 
	 * }
	 */
	public void stand_still() {
		try {
			byte[] cmd = { 'h', 'o', 'l' };
			sendHeader(cmd);
			sendEnding();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void ready() {
		try {
			byte[] cmd = { 'r', 'e', 'd' };
			sendHeader(cmd);
			sendEnding();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * public void writeEEPROM(int blockNum, byte[] data,int length) { try {
	 * clearSerialBuffer(); byte[] cmd = {'w','r','i'}; sendHeader(cmd);
	 * 
	 * writeBuffer[0] = (byte)(0xff & blockNum); writeBuffer[1] = (byte)(0xff &
	 * (blockNum >> 8));
	 * 
	 * int i=0; for(;i < length;i++){ writeBuffer[2+i] = data[i]; } for(;i<
	 * EEPROM_BLOCK_SIZE;i++){ writeBuffer[i+2] =0; }
	 * 
	 * outSerial.write(writeBuffer, 0, EEPROM_BLOCK_SIZE+2); sendEnding();
	 * 
	 * Thread.sleep(700);
	 * 
	 * } catch (Exception e) { e.printStackTrace(); } }
	 * 
	 * public byte[] readEEPROM(int blockNum){ byte[] out = new
	 * byte[EEPROM_BLOCK_SIZE]; try { clearSerialBuffer(); byte[] cmd =
	 * {'r','e','a'}; sendHeader(cmd); writeBuffer[0] = (byte)(0xff & blockNum);
	 * writeBuffer[1] = (byte)(0xff & (blockNum >> 8));
	 * outSerial.write(writeBuffer, 0,2); sendEnding();
	 * 
	 * int index=0;
	 * 
	 * while(index < EEPROM_BLOCK_SIZE){ int len = inSerial.read(out, index,
	 * out.length-index); index +=len;
	 * 
	 * }
	 * 
	 * } catch (Exception e) { e.printStackTrace(); } return out;
	 * 
	 * }
	 */
	private void sendHeader(byte[] cmd) {

		if (!server.isRunning())
			try {
				server.start();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		try {
			server.send(header);
			server.send(cmd);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void sendEnding() {
		try {
			server.send(ending);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*
	 * private void clearSerialBuffer(){ try{ int n = inSerial.available(); if(n
	 * > 0)inSerial.skip(n); }catch(Exception e){ e.printStackTrace(); } }
	 */

	/*
	 * public byte[] test(){ byte[] cmd = {'t','e','s'}; sendHeader(cmd);
	 * sendEnding();
	 * 
	 * try { int n = inSerial.read(readBuffer); readBuffer[n] = '\0';
	 * 
	 * } catch (IOException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 * 
	 * return readBuffer; }
	 */
	/*
	 * public void test2(){
	 * 
	 * byte[] cmd = {'t','e','s'}; sendHeader(cmd); sendEnding(); int n; try{
	 * System.out.println("wait"); Thread.sleep(1001);
	 * 
	 * 
	 * if(inSerial.available() != 0){ n=inSerial.read(readBuffer);
	 * 
	 * System.out.println(n+"yes");
	 * 
	 * System.out.println(new String(readBuffer, 0, n));
	 * 
	 * }else{ System.out.print("no"); } }catch(Exception e){
	 * e.printStackTrace(); } }
	 */

}
