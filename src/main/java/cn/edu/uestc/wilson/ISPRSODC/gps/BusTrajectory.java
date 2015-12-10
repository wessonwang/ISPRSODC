/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.gps;

import java.util.ArrayList;
import java.util.Collections;

import cn.edu.uestc.wilson.ISPRSODC.util.TimeSorter;

/**
 * @author weibornhigh
 *
 */
public class BusTrajectory {
    private ArrayList<GPSReport> reportList;
    
    public BusTrajectory(){
	this.reportList = new ArrayList<GPSReport>();
    }
    
    public void addLocation(GPSReport location){
	this.reportList.add(location);
    }
    
    public ArrayList<GPSReport> getReportList(boolean isSorted){
	if(isSorted){
	    this.sort();
	    return this.reportList;
	}
	else{
	    return this.reportList;
	}
    }
    
//    public GPSReport searchBoardingLocation(int AFCTime){
//	double[] key = {AFCTime};
//	Object[] candidates = this.GPSKDTree.nearest(key, 10);
//	GPSReport candidate = (GPSReport) candidates[0];
//	double timeDiffer = Double.MAX_VALUE;
//	for(Object candidateObject:candidates){
//	    GPSReport current = (GPSReport) candidateObject;
//	    double tmpTimeDiffer = AFCTime-current.getTime();
//	    if(current.getTime()<AFCTime && tmpTimeDiffer<timeDiffer){
//		timeDiffer = tmpTimeDiffer;
//		candidate = current;
//	    }
//	}
//	
//	return candidate;
//    }
    
    /**
     * As no sequence exists when adding {@link GPSReport} to {@link ArrayList<GPSReport>},
     * sorting should be do before using trajectory.
     */
    private void sort(){
	Collections.sort(this.reportList, new TimeSorter());
    }
}
