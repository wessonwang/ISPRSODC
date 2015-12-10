/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.util;

import cn.edu.uestc.wilson.ISPRSODC.gps.Location;

/**
 * @author weibornhigh
 *
 */
public class Gauge {
    public static double distance(Location p1,Location p2) {
	double p1_lon = p1.getLongitude();
	double p1_lat = p1.getLatitude();
	double p2_lon = p2.getLongitude();
	double p2_lat = p2.getLatitude();
	double radLat1 = Rad(p1_lat);
	double radLat2 = Rad(p2_lat);
	double a = radLat1 - radLat2;
	double b = Rad(p1_lon) - Rad(p2_lon);
	double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b/2),2)));
	double dis = s*6378137.0D;
	return dis;
    }
    
    public static double distance(double p1_lon,double p1_lat,double p2_lon,double p2_lat) {
	double radLat1 = Rad(p1_lat);
	double radLat2 = Rad(p2_lat);
	double a = radLat1 - radLat2;
	double b = Rad(p1_lon) - Rad(p2_lon);
	double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b/2),2)));
	double dis = s*6378137.0D;
	return dis;
    }
	
    private static double Rad(double d){
	return d * Math.PI / 180.0;
    }
}
