package ivy.kookkai.debugview;

public class PointRangeBin {
	public static double Z_LOWER_BOUND = -Math.PI/4, Z_UPPER_BOUND = Math.PI/4;
	public static double MAG_LOWER_BOUND = 0, MAG_UPPER_BOUND = 250;
	public static int BIN_COUNT = 60;
	public static double interval = (Z_UPPER_BOUND - Z_LOWER_BOUND)/BIN_COUNT;
	public double[] bin_z = new double[BIN_COUNT];
	public double[] bin_mag = new double[BIN_COUNT];
	PointRangeBin(){
		for(int i=0;i<bin_mag.length;i++){
			bin_mag[i]=MAG_UPPER_BOUND;
		}
	}
	public void reset(){
		for(int i=0;i<BIN_COUNT;i++){
			bin_mag[i]=-1;
			bin_z[i]=-1;
		}
	}
	public void setBin(double z,double mag){
		int bin_num = (int)((z-Z_LOWER_BOUND)/interval);
		if((mag < bin_mag[bin_num] ||  bin_mag[bin_num] < 0 )
				&& mag < MAG_UPPER_BOUND && mag > MAG_LOWER_BOUND
				){
			bin_mag[bin_num] = mag;
			bin_z[bin_num] = z;
		}
	}
	public double[] getBin(int index){
		return new double[]{bin_z[index],bin_mag[index]};
	}
	public int getBinCount(){
		return BIN_COUNT;
	}
}
