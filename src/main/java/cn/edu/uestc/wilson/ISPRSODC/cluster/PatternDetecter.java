/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.cluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import cn.edu.uestc.wilson.ISPRSODC.gps.BusTrajectory;
import cn.edu.uestc.wilson.ISPRSODC.gps.GPSReport;
import cn.edu.uestc.wilson.ISPRSODC.gps.Location;
import cn.edu.uestc.wilson.ISPRSODC.gps.MovePattern;
import cn.edu.uestc.wilson.ISPRSODC.util.Gauge;

/**
 * @author weibornhigh
 * As large scale of data set, those pattern about stopping at terminal, slowing down and pick up passengers should be picked out.
 * And then input these GPS report to join the cluster.
 */
public class PatternDetecter {
    private int minSTAT;		// Minimum Static Time At Terminal, in second unit.
    private double maxDis4Terminal;	// Maximum distance during stopping at terminal.
    private double maxRadius;		// Maximum moving radius during stopping at terminal.
    private double minDis4Normal;	// Minimum distance when bus cruising.
    
    /**
     * 
     * @param sampleInterval
     * @param walkerSpeed
     * @param busCruisingSpeed
     * @param minSTAT
     * @param maxRadius
     */
    public PatternDetecter(int sampleInterval,double walkerSpeed,double busCruisingSpeed,int minSTAT,double maxRadius){
	this.minSTAT = minSTAT;
	this.maxDis4Terminal = (walkerSpeed/3.6D)*sampleInterval;
	this.maxRadius = maxRadius;
	this.minDis4Normal = (0.8*busCruisingSpeed/3.6D)*sampleInterval;
    }
    
    public HashSet<Location> getTerminalCandidates(Object[] BTObjects){
	HashSet<Location> terminalCandidates = new HashSet<Location>(4000);
	for(Object BTObject:BTObjects){
	    BusTrajectory BT = (BusTrajectory) BTObject;
	    ArrayList<GPSReport> currentCandidates = this.oneDetecting(BT);
	    terminalCandidates.addAll(currentCandidates);
	}
	
	this.allSetTerminalArea(terminalCandidates);
	return terminalCandidates;
    }
    
    private ArrayList<GPSReport> oneDetecting(BusTrajectory trajectory){
	ArrayList<GPSReport> terminalCandidates = new ArrayList<GPSReport>(100);
	LinkedList<GPSReport> split = new LinkedList<GPSReport>();
	
	ArrayList<GPSReport> reportList = trajectory.getReportList(true);
	GPSReport back = reportList.get(0);
	for(GPSReport front:reportList){
	    double displacement = Gauge.distance(back, front);
	    //NORMAL report marking
	    if(displacement>this.minDis4Normal){
		front.movePattern = MovePattern.NORMAL;
	    }
	    
	    //terminal area marking
	    if(displacement<this.maxDis4Terminal){
		split.add(front);
	    }
	    else{
		GPSReport start = split.getFirst();
		GPSReport end = split.getLast();
		int moveDuration = end.getTime()-start.getTime();
		double moveRadius =Gauge.distance(start, end);
		if(moveDuration>this.minSTAT && moveRadius<this.maxRadius){
		    terminalCandidates.addAll(split);
		}
		split.clear();
		split.add(front);
	    }
	    
	    back = front;
	}
	
	return terminalCandidates;
    }
    
    private void allSetTerminalArea(HashSet<Location> terminalCandidates){
	for(Location location:terminalCandidates){
	    GPSReport report = (GPSReport) location;
	    report.movePattern = MovePattern.TERMINALAREA;
	}
    }
}
