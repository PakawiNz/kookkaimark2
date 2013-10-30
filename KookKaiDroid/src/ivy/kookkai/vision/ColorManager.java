package ivy.kookkai.vision;

import ivy.kookkai.data.ColorPlate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

public class ColorManager {
	public final static int UNDEFINE = 0;
	public final static int ORANGE = 1;
	public final static int YELLOW = 2;
	public final static int CYAN = 3;
	public final static int MAGENTA = 4;
	public final static int GREEN = 5;
	public final static int WHITE = 6;
	public final static int BLACK = 7;
	public final static int SHADOWGREEN = 8;
	
	public final static int OUTCONVEX = 0;
	
	public static final int WHITE_THRESHOLD = 130;
	public static final int BLACK_THRESHOLD = 40;

	public static final int MIN_COUNT_ORANGE = 4;
	public static final int MIN_COUNT_YELLOW = 120;
	public static final int MIN_COUNT_CYAN = 20;
	public static final int MIN_COUNT_MAGENTA = 20;
	
	private static final String colorfile = "/KorKai/color.txt";
	public static ArrayList<ColorPlate> colorList = new ArrayList<ColorPlate>();
	
	public static byte[][] crcbHashMap = new byte[256][256];
	public static int[] rColor;
	
	public static void initVar() {
		//TODO : REMOVED THIS CHECK IF ERROR
		//GlobalVar.blobResult = new ArrayList<BlobObject>();

		try {
			readColorList();
			Log.d("ColorManager_init", "read color list succeeded");
		} catch (Exception e) {
			// e.printStackTrace();
			System.err.println(e.getMessage());
			Log.d("ColorManager_init", "fail to read color list");
			
			// default value
			colorList.clear();
			colorList.add(new ColorPlate(ORANGE, Color.RED, 160, 255, 0, 90));
			colorList.add(new ColorPlate(YELLOW, Color.YELLOW, 114, 145, 0, 82));
			colorList.add(new ColorPlate(CYAN, Color.CYAN, 0, 128, 160, 200));
			colorList.add(new ColorPlate(MAGENTA, Color.MAGENTA, 135, 255, 135,	255));
			colorList.add(new ColorPlate(GREEN, Color.GREEN, 0, 120, 0, 108));
			colorList.add(new ColorPlate(WHITE, Color.WHITE, 0, 42, 0, 16));
			colorList.add(new ColorPlate(BLACK, Color.BLACK, 10, 20, 0, 10));

		}

		createColorHashMap();

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
		ColorPlate[] cPlate = new ColorPlate[colorList.size()];
		for (int i = 0; i < colorList.size(); i++) {
			cPlate[i] = colorList.get(i);
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
