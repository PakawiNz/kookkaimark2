package ivy.kookkai.data;

import java.io.Serializable;

public class ColorPlate implements Serializable {
	public int tag;
	public int color;
	public int minCb, maxCb;
	public int minCr, maxCr;

	public ColorPlate(int tag, int color, int minCr, int maxCr, int minCb, int maxCb) {
		this.tag = tag;
		this.color = color;
		this.minCb = minCb;
		this.maxCb = maxCb;
		this.minCr = minCr;
		this.maxCr = maxCr;

	}

	public boolean isThisColor(int cr, int cb) {
		if (cb >= minCb && cb <= maxCb && cr >= minCr && cr <= maxCr)
			return true;
		return false;
	}

}