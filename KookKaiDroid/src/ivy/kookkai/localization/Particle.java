package ivy.kookkai.localization;

import ivy.kookkai.debugview.FieldView;
import android.graphics.Point;

public class Particle {

	private double mWeight;
	private double mZ;// mZ==0 = particle point to east
	private double mX,mY; // in the unit of cm
	// unknown starting point yet but x,y = 0,0 should be start bottom right
	// up is y+, right is x+
	
	public Particle() {
		mWeight=0.5;	
		mX=FieldView.TOTALLENGTH*Math.random();
		mY=FieldView.TOTALWIDTH*Math.random();
		mZ=2*Math.PI*Math.random();
	}

	public void setmZ(double mZ) {
		this.mZ = mZ;
	}

	public void setmX(double mX) {
		this.mX = mX;
	}

	public void setmY(double mY) {
		this.mY = mY;
	}

	public Particle(double x,double y,double z,double w){
		this.mX=x;
		this.mY=y;
		this.mZ=z;
		this.mWeight=w;	
	}
	public void update_weight(double _weight){
		
	}

	public double getmWeight() {
		return mWeight;
	}

	public void setmWeight(double mWeight) {
		this.mWeight = mWeight;
	}

	public double getmZ() {
		return mZ;
	}

	public double getmX() {
		return mX;
	}

	public double getmY() {
		return mY;
	}
	
}
