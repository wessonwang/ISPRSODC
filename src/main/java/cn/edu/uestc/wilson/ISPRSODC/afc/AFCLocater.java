/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.afc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

import cn.edu.uestc.wilson.ISPRSODC.gps.BusRouter;
import cn.edu.uestc.wilson.ISPRSODC.gps.Direction;
import cn.edu.uestc.wilson.ISPRSODC.gps.GPSReport;
import cn.edu.uestc.wilson.ISPRSODC.gps.TransitCenter;
import cn.edu.uestc.wilson.ISPRSODC.util.TimeSorter;
import net.sf.javaml.core.kdtree.KDTree;

/**
 * @author weibornhigh
 *
 */
public class AFCLocater {
    
    private int backTime;//second unit
    private int frontTime; //It seems GPS time system is not synchronous with the AFC system.
    
    public AFCLocater(int frontTime,int backTime){
	this.frontTime = frontTime;
	this.backTime = backTime;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ArrayList locate(CSVRecord AFCRecord,Map<Integer,Integer> BusRouteMap, TransitCenter transitCenter){
	ArrayList newAFC = new ArrayList();
	try{
	    int guid = Integer.parseInt(AFCRecord.get("guid"));
	    String card_id = AFCRecord.get("card_id");
	    int day = Integer.parseInt(AFCRecord.get("day"));
	    int time = Integer.parseInt(AFCRecord.get("time"));
	    int bus_id = Integer.parseInt(AFCRecord.get("bus_id"));
	    int route_id = BusRouteMap.get(bus_id);
	    BusRouter busRouter = transitCenter.getBusRouter(route_id);
	    KDTree GPSKDTree = busRouter.getGPSKDTree();
		
	    GPSReport Neighbor = this.getAFCNeighbor(bus_id, day, time, GPSKDTree);
	    int directMark = AFCLocater.directionMarker(Neighbor);
	    
	    if(directMark == -1){
		directMark = this.directOnStation(bus_id, day, time, GPSKDTree);
	    }
	    
	    newAFC.add(guid);
	    newAFC.add(card_id);
	    newAFC.add(route_id);
	    newAFC.add(bus_id);
	    newAFC.add(day);
	    newAFC.add(time);
	    newAFC.add(directMark);
	    newAFC.add((new BigDecimal(Neighbor.getLongitude())).setScale(7, BigDecimal.ROUND_HALF_UP).doubleValue());
	    newAFC.add((new BigDecimal(Neighbor.getLatitude())).setScale(7, BigDecimal.ROUND_HALF_UP).doubleValue());
	}
	catch(NumberFormatException e){
	    return null;
	}
	
	return newAFC;
    }
    
    private static int directionMarker(GPSReport report){
	
	Direction dic = report.direction;
	switch(dic){
	case UP : return 0;	//UP
	case DOWN : return 1;	//DOWN
	case TERMINAL: return -1;//STATION AREA
	default :return 2;	//CAN NOT BE LOCATED
	}
    }
    
    private int directOnStation(int bus_id, int day, int time, KDTree GPSKDTree){
	double[] uppk = {bus_id,day,time+1020,Double.MAX_VALUE,Double.MAX_VALUE};
	double[] lowk = {bus_id,day,time,0,0};
	Object[] candidateObjects = GPSKDTree.range(lowk, uppk);
	
	if(candidateObjects.length == 0){
	    return -1;
	}
	
	ArrayList<GPSReport> candidateList = new ArrayList<GPSReport>();
	for(Object candidateObject:candidateObjects){
	    GPSReport candidate = (GPSReport) candidateObject;
	    candidateList.add(candidate);
	}
	Collections.sort(candidateList, new TimeSorter());
	return directionMarker(candidateList.get(candidateList.size()-1));
    }
    
    private GPSReport getAFCNeighbor(int bus_id, int day, int time, KDTree GPSKDTree){
	ArrayList<GPSReport> candidateList = new ArrayList<GPSReport>();
	GPSReport neighborRP = null;
	double[] uppk = {bus_id,day,time+frontTime,Double.MAX_VALUE,Double.MAX_VALUE};
	double[] lowk = {bus_id,day,time-backTime,0,0};
	
	Object[] candidateObjects = GPSKDTree.range(lowk, uppk);
	for(Object candidateObject:candidateObjects){
	    GPSReport candidate = (GPSReport) candidateObject;
	    candidateList.add(candidate);
	}
	
	if(candidateList.size() == 0){
	    return new GPSReport(time);
	}
	
	Collections.sort(candidateList, new TimeSorter());
	if(candidateList.get(0).getTime() >= time){
	    neighborRP = candidateList.get(0);
	}
	else{
	    int maxIndex = 0;
	    int size = candidateList.size();
	    for(int i=0; i<size; i++){
		if(candidateList.get(i).getTime() <= time){
		    maxIndex = i;
		}
		else{
		    break;
		}
	    }
	    neighborRP = candidateList.get(maxIndex);
	}
	
	return neighborRP;
    }
}
