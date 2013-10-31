package ivy.kookkai.vision;

import java.util.ArrayList;

import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.debugview.DebugImgView;
import android.graphics.Rect;
import android.util.Log;

public class Blob {
	
	private int width, height;
	private final int qx[] = new int[86400];
	private byte[] colorData;
	private byte[] yImg;
	private byte[] img;
	private DebugImgView debugImg;
	private boolean drawcolor;
	
	private int[] convexL;
	private int[] convexR;

	public int dataWidth;
	public int dataHeight;
	String out = "";

	public Blob() {
		
	}

	public String execute(byte[] brightness, byte[] cbcrImg, int w, int h,
			DebugImgView dView, boolean drawColor) {
		out = "";
		if (colorData == null || colorData.length != cbcrImg.length / 2) {
			colorData = new byte[cbcrImg.length / 2];
			convexL = new int[w];
			convexR = new int[w];
		}
		yImg = brightness;
		img = cbcrImg;
		debugImg = dView;
		width = w;
		height = h;
		drawcolor = drawColor;

		dataHeight = w;
		dataWidth = h;

		threshold();
		findBlob();
		ArrayList<BlobObject> finalresult = connectBlob();
		//filterNoise(finalresult);
		GlobalVar.mergeResult = finalresult;

		return out;
	}

	private void threshold() {

		int outIndex = 0;
		int y, cr, cb;
		int i, j, k;
		
		boolean foundGreen;		
		boolean foundDarkGreen;	
		int firstDarkgreen = 0;
		int lastDarkgreen = 0;
		
		int w2 = width * 2;
		int w4 = width * 4;
		for (i = 0; i < width; i++) {
			foundGreen = false;
			foundDarkGreen = false;
			convexL[i] = width;
			convexR[i] = 0;
			for (j = i * 2 + w2 * (height - 1), k = i * 2 + w4 * (height - 1); j >= 0; j -= w2, k -= w4, outIndex++) {
				cr = (int) img[j] & 0xff;
				cb = (int) img[j + 1] & 0xff;
				y = (int) yImg[k] & 0xff;

				if (y > ColorManager.WHITE_THRESHOLD)
					colorData[outIndex] = ColorManager.WHITE;
				else if (y < ColorManager.BLACK_THRESHOLD)
					colorData[outIndex] = ColorManager.BLACK;
				else 
					colorData[outIndex] = ColorManager.crcbHashMap[cr][cb];
				
				/** CONVEX HULL MAKING */
				
				if(colorData[outIndex] == ColorManager.DARKGREEN){	// this is darkgreen
					if(!foundDarkGreen) {								// this is the first darkgreen
						firstDarkgreen = outIndex;
						foundDarkGreen = true;
					}
					
					if(foundGreen){		// this adjacent to green
						if(convexR[i] == outIndex - 1){
							convexR[i] = outIndex;
						}
						colorData[outIndex] = ColorManager.GREEN;
					}
				} else {
					if(colorData[outIndex] == ColorManager.GREEN){	// this is green
						if(!foundGreen) {								// this is the firstgreen
							if(foundDarkGreen) {							// have some darkgreen adjacent to this
								convexL[i] = firstDarkgreen;		
								for(int a = firstDarkgreen; a < outIndex ; a++){
									colorData[a] = ColorManager.GREEN;
								}
							} else {
								convexL[i] = outIndex;
							}
							foundGreen = true;
						}
						convexR[i] = outIndex;
					}else {
						foundDarkGreen = false;
					}
				}
			}
		}	
		
		// can't move to debugImgView coz vision blob will execute painting
		if (drawcolor) {
			outIndex = 0;
			for (i = 0; i < width; i++) {
				for (j = 0; j < height; j++, outIndex++) {
					debugImg.drawPixel(j, i, ColorManager.rColor[colorData[outIndex]]);
				}
			}
		}
	}

	public byte[] getColorData() {
		return colorData;
	}
	
	private void findBlob() {
		BlobObject b;
		GlobalVar.blobResult.clear();
		int outIndex = 0;
		
		for (int i = 0; i < width; i++) {
			for(int j = 0; j < height ; j++){
				// GREEN should be a defined color, but should not count as a blob
				if (convexL[i] < outIndex && outIndex < convexR[i]) {
					if(	colorData[outIndex] != ColorManager.UNDEFINE && (
						//colorData[outIndex] == ColorManager.MAGENTA || 
						//colorData[outIndex] == ColorManager.CYAN || 
						colorData[outIndex] == ColorManager.YELLOW || 
						colorData[outIndex] == ColorManager.ORANGE )) {			
					
						b = fillBlob(outIndex);
						if (b != null) {
							GlobalVar.blobResult.add(b);
							// debugImg.drawRect(b.posRect,Color.WHITE);
						}
					}
				}
				outIndex++;
			}
		}
		
//		outIndex = 0;
//		for (int i = 0; i < width; i++) {
//			for(int j = 0; j < height ; j++){
//				if (convexL[i] < outIndex && outIndex < convexR[i]){
//					colorData[outIndex] = ColorManager.MAGENTA;
//				}
//				outIndex++;
//			}
//		}
		
		
	}

	private BlobObject fillBlob(int pos) {
		qx[0] = pos;
		byte baseColor = getPixel(pos);
		int minX = pos % dataWidth, maxX = pos % dataWidth;
		int minY = pos / dataWidth, maxY = pos / dataWidth;
		int pixelCount = 1;
		int curX, curY, curPos;
		int centroidX = 0;

		setPixel(pos, (byte) 0);

		for (int i = 0; i < pixelCount; i++) {
			curX = qx[i] % dataWidth;
			curY = qx[i] / dataWidth;
			curPos = qx[i];

			if (curX > maxX)
				maxX = curX;
			if (curX < minX)
				minX = curX;
			if (curY > maxY)
				maxY = curY;
			if (curY < minY)
				minY = curY;

			// LEFT BREATH
			if (curX > 0 && getPixel(curPos - 1) == baseColor ){
				qx[pixelCount] = curPos - 1;
				setPixel(curPos - 1, (byte) ColorManager.UNDEFINE);
				pixelCount++;
				centroidX += curX;
			}
			
			// RIGHT BREATH
			if (curX + 1 < dataWidth && getPixel(curPos + 1) == baseColor ){
				qx[pixelCount] = curPos + 1;
				setPixel(curPos + 1, (byte) ColorManager.UNDEFINE);
				pixelCount++;
				centroidX += curX;
			}

			// UP BREATH
			if (curY > 0 && getPixel(curPos - dataWidth) == baseColor ){
				qx[pixelCount] = curPos - dataWidth;
				setPixel(curPos - dataWidth, (byte) ColorManager.UNDEFINE);
				pixelCount++;
				centroidX += curX;
			}

			// DOWN BREATH
			if (curY + 1 < dataHeight && getPixel(curPos + dataWidth) == baseColor ){
				qx[pixelCount] = curPos + dataWidth;
				setPixel(curPos + dataWidth, (byte) ColorManager.UNDEFINE);
				pixelCount++;
				centroidX += curX;
			}
		}
		centroidX /= pixelCount;

		int minSize;
		switch (baseColor) {
		case ColorManager.ORANGE:
			minSize = ColorManager.MIN_COUNT_ORANGE;
			break;
		case ColorManager.YELLOW:
			minSize = ColorManager.MIN_COUNT_YELLOW;
			break;
		case ColorManager.CYAN:
			minSize = ColorManager.MIN_COUNT_CYAN;
			break;
		case ColorManager.MAGENTA:
			minSize = ColorManager.MIN_COUNT_MAGENTA;
			break;
		default:
			minSize = 120;
			break;
		}
		
		if (pixelCount < minSize)
			return null;

		return new BlobObject(baseColor, new Rect(minX, minY, maxX, maxY),pixelCount, centroidX);
	}

	private ArrayList<BlobObject> connectBlob() {
		int i;

		BlobObject objA, objB;

		@SuppressWarnings("unchecked")
		ArrayList<BlobObject> tempList = (ArrayList<BlobObject>) GlobalVar.blobResult.clone();
		ArrayList<BlobObject> mergeList = new ArrayList<BlobObject>();

		while (tempList.size() > 0) {
			objA = tempList.get(0);
			tempList.remove(0);

			for (i = 0; i < tempList.size(); i++) {
				objB = tempList.get(i);

				if (objA.posRect.left - 4 < objB.posRect.right
						&& objA.posRect.right + 4 > objB.posRect.left
						&& objA.posRect.top - 10 < objB.posRect.bottom
						&& objA.posRect.bottom + 10 > objB.posRect.top
						&& Math.abs(objA.posRect.left - objA.posRect.right) < 70
						&& Math.abs(objB.posRect.left - objB.posRect.right) < 70) {

					if (objA.tag == objB.tag) {
						merge(objA, objB);
						tempList.remove(i);
						i--;
					}
				}
			}

			objA.tag += 10;

			int sizeA = objA.getSize();
			// sort from min to max size
			for (i = 0; i < mergeList.size(); i++)
				if (mergeList.get(i).getSize() > sizeA)
					break;
			mergeList.add(i, objA);
		}

		return mergeList;
	}

	private void merge(BlobObject a, BlobObject b) {
		a.posRect.left = Math.min(a.posRect.left, b.posRect.left);
		a.posRect.right = Math.max(a.posRect.right, b.posRect.right);
		a.posRect.top = Math.min(a.posRect.top, b.posRect.top);
		a.posRect.bottom = Math.max(a.posRect.bottom, b.posRect.bottom);
		if (a.centroidX + b.centroidX < 1)
			a.centroidX = 1;
		else
			a.centroidX = ((a.centroidX * a.pixelCount) + (b.centroidX * b.pixelCount))
					/ (a.centroidX + b.centroidX);
		a.pixelCount += b.pixelCount;

	}

	private void filterNoise(ArrayList<BlobObject> list) {
		int threshold = (int) (0.25 * GlobalVar.frameHeight);
		for (int i = 0; i < list.size(); i++) {
			BlobObject b = list.get(i);
			if (b.posRect.bottom < threshold) {
				list.remove(i);
				i--;
			}
		}
	}

	private byte getPixel(int pos) {
		return colorData[pos];
	}

	private void setPixel(int pos, byte val) {
		colorData[pos] = val;
	}

	/*public String execute(byte[] hsvImg, int w, int h, DebugImgView dView, boolean drawColor) {
		out = "";
		if (colorData == null || colorData.length != hsvImg.length / 3) {
			colorData = new byte[hsvImg.length / 3];
			convexL = new int[w];
			convexR = new int[w];
		}
		img = hsvImg;
		debugImg = dView;
		width = w;
		height = h;
		drawcolor = drawColor;

		dataHeight = w;
		dataWidth = h;

		threshold();
		findBlob();
		ArrayList<BlobObject> finalresult = connectBlob();
		filterNoise(finalresult);
		GlobalVar.mergeResult = finalresult;

		return out;
	}
	
	private void threshold() {

		int outIndex = 0;
		int v, h, s;
		int i, j, k;
		
		boolean foundGreen;
		int lastFoundGreen = 0;

		int w2 = width * 2;
		int w4 = width * 4;
		for (i = 0; i < width; i++) {
			foundGreen = false;
			convexL[i] = width;
			convexR[i] = 0;
			
			for (j = i * 2 + w2 * (height - 1), k = i * 2 + w4 * (height - 1); j >= 0; j -= w2, k -= w4, outIndex++) {
				h = (int) img[j] & 0xff;
				s = (int) img[j + 1] & 0xff;
				v = (int) yImg[k] & 0xff;

				colorData[outIndex] = ColorManager.crcbHashMap[h][s];
				
				if (colorData[outIndex] == ColorManager.UNDEFINE) {
					if (v > ColorManager.WHITE_THRESHOLD)
						colorData[outIndex] = ColorManager.WHITE;
					else if (v < ColorManager.BLACK_THRESHOLD)
						colorData[outIndex] = ColorManager.BLACK;
				}
				
				if(colorData[outIndex] == ColorManager.GREEN){
					if(!foundGreen) {
						convexL[i] = outIndex;
					}
					foundGreen = true;
					lastFoundGreen = outIndex;
				}
			}
			convexR[i] = lastFoundGreen;
		}	
	}*/

}
