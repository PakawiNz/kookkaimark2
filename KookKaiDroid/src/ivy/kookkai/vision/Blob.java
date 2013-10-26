package ivy.kookkai.vision;

import java.util.ArrayList;

import ivy.kookkai.data.GlobalVar;
import ivy.kookkai.debugview.DebugImgView;
import android.graphics.Rect;

public class Blob {
	public static final int WHITE_THRESHOLD = 130;
	public static final int BLACK_THRESHOLD = 40;

	public static final int MIN_COUNT_ORANGE = 4;
	public static final int MIN_COUNT_YELLOW = 120;
	public static final int MIN_COUNT_CYAN = 20;
	public static final int MIN_COUNT_MAGENTA = 20;
	private int width, height;
	private final int qx[] = new int[86400];
	private byte[] colorData;
	private byte[] yImg;
	private byte[] img;
	private DebugImgView debugImg;
	private boolean drawcolor;

	public int dataWidth;
	public int dataHeight;
	String out = "";

	public Blob() {

	}

	public int getBallPixels() {
		// TODO :
		return 0;
	}

	public String execute(byte[] brightness, byte[] cbcrImg, int w, int h,
			DebugImgView dView, boolean drawColor) {
		out = "";
		if (colorData == null || colorData.length != cbcrImg.length / 2) {
			colorData = new byte[cbcrImg.length / 2];
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
		filterNoise(finalresult);
		GlobalVar.mergeResult = finalresult;

		return out;
	}

	private void threshold() {

		int outIndex = 0;
		int y, cr, cb;
		int i, j, k;

		int w2 = width * 2;
		int w4 = width * 4;
		for (i = 0; i < width; i++) {
			for (j = i * 2 + w2 * (height - 1), k = i * 2 + w4 * (height - 1); j >= 0; j -= w2, k -= w4, outIndex++) {
				cr = (int) img[j] & 0xff;
				cb = (int) img[j + 1] & 0xff;
				y = (int) yImg[k] & 0xff;

				colorData[outIndex] = GlobalVar.crcbHashMap[cr][cb];
				if (colorData[outIndex] == 0) {
					if (y > WHITE_THRESHOLD)
						colorData[outIndex] = GlobalVar.WHITE;
					else if (y < BLACK_THRESHOLD)
						colorData[outIndex] = GlobalVar.BLACK;
				}
			}
		}
		// /Important delete glare, depend on cellphone used

		// Acer
		// Xperia
		if (GlobalVar.KOOKKAI_MARK == 2) {
			int startHeightL = 65;
			double mL = 1;
			for (int f = dataHeight - startHeightL; f < dataHeight; f++) {
				double error = (startHeightL - dataHeight + f) / 6.4;
				for (int g = 0; g < mL * error * error; g++) {
					colorData[f * dataWidth + g] = 0;// GlobalVar.CYAN;
				}
			}
			int startHeightR = 65;
			double mR = 1;
			for (int f = dataHeight - startHeightR; f < dataHeight; f++) {
				double error = (startHeightR - dataHeight + f) / 6.4;
				for (int g = 0; g < mR * error * error; g++) {
					colorData[(f + 1) * dataWidth - 1 - g] = 0;// GlobalVar.CYAN;
				}
			}
		}
		// Galaxy Nexus
		else if (GlobalVar.KOOKKAI_MARK == 3) {
			int startHeightL = 38;
			double mL = 1;
			for (int f = dataHeight - startHeightL; f < dataHeight; f++) {
				double error = (startHeightL - dataHeight + f) / 6.4;
				for (int g = 0; g < mL * error * error; g++) {
					colorData[f * dataWidth + g] = 0;// GlobalVar.CYAN;
				}
			}
			int startHeightR = 60;
			double mR = 1;
			for (int f = dataHeight - startHeightR; f < dataHeight; f++) {
				double error = (startHeightR - dataHeight + f) / 6.4;
				for (int g = 0; g < mR * error * error; g++) {
					colorData[(f + 1) * dataWidth - 1 - g] = 0;// GlobalVar.CYAN;
				}
			}
		}
		// can't move to debugImgView coz vision blob will execute painting
		if (drawcolor) {
			outIndex = 0;
			for (i = 0; i < width; i++) {
				for (j = 0; j < height; j++, outIndex++) {
					debugImg.drawPixel(j, i,
							GlobalVar.rColor[colorData[outIndex]]);
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
		for (int i = 0; i < colorData.length; i++) {
			// GREEN should be a defined color, but should not count as a blob
			if (colorData[i] != 0
					&& (colorData[i] == GlobalVar.ORANGE
							|| colorData[i] == GlobalVar.YELLOW
							|| colorData[i] == GlobalVar.CYAN || colorData[i] == GlobalVar.MAGENTA)) {
				b = fillBlob(i);
				if (b != null) {
					GlobalVar.blobResult.add(b);
					// debugImg.drawRect(b.posRect,Color.WHITE);
				}
			}
		}
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

			if (curX > 0 && getPixel(curPos - 1) == baseColor) {
				qx[pixelCount] = curPos - 1;
				setPixel(curPos - 1, (byte) 0);
				pixelCount++;
				centroidX += curX;
			}

			if (curY > 0 && getPixel(curPos - dataWidth) == baseColor) {
				qx[pixelCount] = curPos - dataWidth;
				setPixel(curPos - dataWidth, (byte) 0);
				pixelCount++;
				centroidX += curX;
			}

			if (curX + 1 < dataWidth && getPixel(curPos + 1) == baseColor) {
				qx[pixelCount] = curPos + 1;
				setPixel(curPos + 1, (byte) 0);
				pixelCount++;
				centroidX += curX;
			}

			if (curY + 1 < dataHeight
					&& getPixel(curPos + dataWidth) == baseColor) {
				qx[pixelCount] = curPos + dataWidth;
				setPixel(curPos + dataWidth, (byte) 0);
				pixelCount++;
				centroidX += curX;
			}
		}
		centroidX /= pixelCount;

		int minSize = 120;
		switch (baseColor) {
		case GlobalVar.ORANGE:
			minSize = MIN_COUNT_ORANGE;
			break;
		case GlobalVar.YELLOW:
			minSize = MIN_COUNT_YELLOW;
			break;
		case GlobalVar.CYAN:
			minSize = MIN_COUNT_CYAN;
			break;
		case GlobalVar.MAGENTA:
			minSize = MIN_COUNT_MAGENTA;
			break;
		default:
			minSize = 120;
			break;
		}
		// / check boundary of orange blob if it's surrounded by green
		double total_count = 0;
		double green_count = 0;
		if (pixelCount < minSize && baseColor == GlobalVar.ORANGE) {
			int GREEN_OFFSET = 10;
			// check only lower part of frame
			for (int i = minY + (maxY - minY) / 2; i < maxY; i++) {
				// left boundary
				if (minX - GREEN_OFFSET > 0) {
					total_count++;
					if (getPixel(minX - GREEN_OFFSET + (i * dataWidth)) == GlobalVar.GREEN) {
						green_count++;
					}
				}
				// right boundary
				if (maxX + GREEN_OFFSET < dataWidth) {
					total_count++;
					if (getPixel(maxX + GREEN_OFFSET + (i * dataWidth)) == GlobalVar.GREEN) {
						green_count++;
					}
				}
			}
			for (int i = minX; i < maxX; i++) {
				// lower boundary
				if (maxY + GREEN_OFFSET < dataHeight) {
					total_count++;
					if (getPixel(i + ((maxY + GREEN_OFFSET) * dataWidth)) == GlobalVar.GREEN) {
						green_count++;
					}
				}
			}

			double threshold = 0.2 + (4 - pixelCount) * 0.1;
			if (green_count / total_count > threshold
					&& total_count > 0.8 * GREEN_OFFSET) {
				return new BlobObject(baseColor, new Rect(minX, minY, maxX,
						maxY), pixelCount, centroidX);
			} else {
				return null;
			}
		} else if (pixelCount < minSize)
			return null;

		return new BlobObject(baseColor, new Rect(minX, minY, maxX, maxY),
				pixelCount, centroidX);
	}

	private ArrayList<BlobObject> connectBlob() {
		int i;

		BlobObject objA, objB;

		ArrayList<BlobObject> tempList = (ArrayList<BlobObject>) GlobalVar.blobResult
				.clone();
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

}
