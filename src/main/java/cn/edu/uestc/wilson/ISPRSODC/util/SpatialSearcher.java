/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.util;

import java.util.LinkedList;

import net.sf.javaml.core.kdtree.KDTree;
import ch.hsr.geohash.WGS84Point;
import ch.hsr.geohash.util.VincentyGeodesy;
import cn.edu.uestc.wilson.ISPRSODC.gps.Location;

/**
 * @author weibornhigh
 *
 */
public class SpatialSearcher {
    
    public static Object[] getAdjacentOnSameRoute(Location center,KDTree GPSKDTree,double radius){
	LinkedList<double[]> range = SpatialSearcher.rangeOnSameRoute(center, radius);
	Object[] adjacentObjects = GPSKDTree.range(range.get(0), range.get(1));
	return adjacentObjects;
    }
    
    /**
     * To get the range.
     * @param center
     * @param radius
     * @return LnikedList<> {lowk,uppk}
     */
    private static LinkedList<double[]> rangeOnSameRoute(Location center,double radius){
	WGS84Point WGS84center = new WGS84Point(center.getLatitude(),center.getLongitude());
	WGS84Point WGS84NorthEast = VincentyGeodesy.moveInDirection(VincentyGeodesy.moveInDirection(WGS84center, 0, radius), 90,radius);
	WGS84Point WGS84SouthWest = VincentyGeodesy.moveInDirection(VincentyGeodesy.moveInDirection(WGS84center, 180, radius),270, radius);
	
	double[] uppk = {Integer.MAX_VALUE,
			Integer.MAX_VALUE,
			Integer.MAX_VALUE,
			WGS84NorthEast.getLongitude(),
			WGS84NorthEast.getLatitude()};
	
	double[] lowk = {0,
			0,
			0,
			WGS84SouthWest.getLongitude(),
			WGS84SouthWest.getLatitude()};
	
	LinkedList<double[]> range = new LinkedList<double[]>();
	range.add(lowk);
	range.add(uppk);
	return range;
    }
    
    public static Object[] getAdjacentStops(int route_id, int direction, double[] coordination, KDTree STOPKDTree,double radius){
	WGS84Point WGS84center = new WGS84Point(coordination[1],coordination[0]);
	WGS84Point WGS84NorthEast = VincentyGeodesy.moveInDirection(VincentyGeodesy.moveInDirection(WGS84center, 0, radius), 90,radius);
	WGS84Point WGS84SouthWest = VincentyGeodesy.moveInDirection(VincentyGeodesy.moveInDirection(WGS84center, 180, radius),270, radius);
	
	if(direction == -1){
	    double[] uppk = {route_id,
		    	     1,
		    	     WGS84NorthEast.getLongitude(),
		    	     WGS84NorthEast.getLatitude()};
	    double[] lowk = {route_id,
		    	     0,
		    	     WGS84SouthWest.getLongitude(),
		    	     WGS84SouthWest.getLatitude()};
	    
	    return STOPKDTree.range(lowk, uppk);
	}
	else{
	    double[] uppk = {route_id,
		    	     direction,
		    	     WGS84NorthEast.getLongitude(),
		    	     WGS84NorthEast.getLatitude()};
	    double[] lowk = {route_id,
		    	     direction,
		    	     WGS84SouthWest.getLongitude(),
		    	     WGS84SouthWest.getLatitude()};
	    
	    return STOPKDTree.range(lowk, uppk);
	}
	
    }
}
