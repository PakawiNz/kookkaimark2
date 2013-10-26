package ivy.kookkai.vision;

import android.graphics.Rect;

public class BlobObject {

	public byte tag;
	public Rect posRect;
	public int pixelCount;
	public int centroidX;
	
	public BlobObject(byte tag, Rect pos,int pixelCount,int centroidX) {
		this.tag = tag;
		this.posRect = pos;
		this.pixelCount = pixelCount;
		this.centroidX = centroidX;
	}

	public int getSize() {
		return posRect.width() * posRect.height();

	}

}