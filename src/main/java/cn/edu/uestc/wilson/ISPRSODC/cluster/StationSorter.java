/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.sf.javaml.core.kdtree.KDTree;
import cn.edu.uestc.wilson.ISPRSODC.gps.BusRouter;
import cn.edu.uestc.wilson.ISPRSODC.gps.BusTrajectory;
import cn.edu.uestc.wilson.ISPRSODC.gps.Direction;
import cn.edu.uestc.wilson.ISPRSODC.gps.GPSReport;
import cn.edu.uestc.wilson.ISPRSODC.gps.Station;
import cn.edu.uestc.wilson.ISPRSODC.util.Gauge;

/**
 * @author weibornhigh
 *
 */
public class StationSorter {
    private BusRouter busRouter;
    private ArrayList<Station> singleDirectedStations;
    private Direction direction;
    private KDTree STKDTree;
    private static int runTimes = 10;
    private static double radius = 170;
    
    public StationSorter(ArrayList<Station> singleDirectedStations,Direction direction,BusRouter busRouter){
	this.busRouter = busRouter;
	this.singleDirectedStations = singleDirectedStations;
	this.direction = direction;
	this.STKDTree = StationSorter.getSTKDTree(singleDirectedStations);
    }
    
    public ArrayList<Station> sort(){
	Object[] BTObjects = this.busRouter.getAllBusTrajectory();
	int i = 0;
	for(Object BTObject:BTObjects){
	    
	    if(i >= StationSorter.runTimes)
		break;
	    
	    BusTrajectory BT = (BusTrajectory) BTObject;
	    ArrayList<GPSReport> reportList = BT.getReportList(false);
	    ArrayList<Station> markedStation = new ArrayList<Station>();
	    int sequence = 0;
	    for(GPSReport report:reportList){
		if(report.direction == this.direction || report.direction == Direction.TERMINAL){
		    double[] key = {report.getLongitude(),report.getLatitude()};
		    Station neighborST = (Station) STKDTree.nearest(key);
		    
		    if(neighborST != null && !markedStation.contains(neighborST) && Gauge.distance(report, neighborST) < StationSorter.radius){
			sequence++;
			markedStation.add(neighborST);
			neighborST.sequence = (neighborST.sequence + sequence)/2;
		    }
		}
		else{
		    markedStation.clear();
		    sequence = 0;
		}
	    }
	    
	    i++;
	}
	
	Collections.sort(this.singleDirectedStations, new Sorter());
	return this.singleDirectedStations;
    }
    
    private static KDTree getSTKDTree(ArrayList<Station> singleDirectedStations){
	KDTree STKDTree = new KDTree(2);//key=[lng,lat], value=Station;
	for(Station station:singleDirectedStations){
	    double[] key = {station.getLongitude(),station.getLatitude()};
	    STKDTree.insert(key, station);
	}
	return STKDTree;
    }
    
    class Sorter implements Comparator<Station>{
	public int compare(Station s1, Station s2) {
	    // TODO Auto-generated method stub
	    return (s1.sequence - s2.sequence) >= 0 ? 1:-1;
	}
    }
}
