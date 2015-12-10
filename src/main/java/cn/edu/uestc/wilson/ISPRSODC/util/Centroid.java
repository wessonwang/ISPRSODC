/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.util;

import java.util.List;

import cn.edu.uestc.wilson.ISPRSODC.gps.Location;

/**
 * @author weibornhigh
 *
 */
public class Centroid {
    
    /**
     * 
     * @param ODCluster
     * @return [longitude,latitude]
     */
    public static double[] compute(List<Location> locationList){
	int quality = locationList.size();
	double qualityLongitude = 0;
	double qualityLatitude = 0;
	for(Location location:locationList){
	    qualityLongitude = location.getLongitude() + qualityLongitude;
	    qualityLatitude = location.getLatitude() + qualityLatitude;
	}
	
	double centroidLongitude = qualityLongitude/quality;
	double centroidLatitude = qualityLatitude/quality;
	double[] coordination = {centroidLongitude,centroidLatitude};
	return coordination;
    }
}
