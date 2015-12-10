package cn.edu.uestc.wilson.ISPRSODC.gps;

public abstract class Location {
    public double[] coordinate = new double[2];//[lng,lat]
    
    public Location(double lng, double lat){
	this.coordinate[0] = lng;
	this.coordinate[1] = lat;
    }
    
    public double getLongitude(){
	return this.coordinate[0];
    }
    
    public double getLatitude(){
	return this.coordinate[1];
    }
}
