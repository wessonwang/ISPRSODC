/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sf.javaml.core.kdtree.KDTree;
import cn.edu.uestc.wilson.ISPRSODC.gps.BusTrajectory;
import cn.edu.uestc.wilson.ISPRSODC.gps.Direction;
import cn.edu.uestc.wilson.ISPRSODC.gps.GPSReport;
import cn.edu.uestc.wilson.ISPRSODC.gps.Location;
import cn.edu.uestc.wilson.ISPRSODC.gps.MovePattern;
import cn.edu.uestc.wilson.ISPRSODC.gps.Station;
import cn.edu.uestc.wilson.ISPRSODC.gps.TerminalIndex;
import cn.edu.uestc.wilson.ISPRSODC.util.Gauge;
import cn.edu.uestc.wilson.ISPRSODC.util.SpatialSearcher;

/**
 * @author weibornhigh
 *
 */
public class DirectionSpliter {
    private double ODAreaRadius;
    
    public DirectionSpliter(double ODAreaRadius){
	this.ODAreaRadius = ODAreaRadius;
    }
    
    public Map<Direction,HashSet<Location>> split(Station S1, Station S2, Object[] BTObjects, KDTree GPSKDTree){
	//Terminal area marking
	this.markTerminalArea(S1, TerminalIndex.ONE, GPSKDTree);
	this.markTerminalArea(S2, TerminalIndex.TWO, GPSKDTree);
	
	HashSet<Location> UPList = new HashSet<Location>(150000);
	HashSet<Location> DOWNList = new HashSet<Location>(150000);
	for(Object BTObject:BTObjects){
	    Map<Direction,List<GPSReport>> directedMap = this.oneSpliting(BTObject);
	    UPList.addAll(directedMap.get(Direction.UP));
	    DOWNList.addAll(directedMap.get(Direction.DOWN));
	}
	
	Map<Direction,HashSet<Location>> directedCluster = new HashMap<Direction,HashSet<Location>>();
	directedCluster.put(Direction.UP, UPList);
	directedCluster.put(Direction.DOWN, DOWNList);
	return directedCluster;
    }
    
    private Map<Direction,List<GPSReport>> oneSpliting(Object BTObject){
	BusTrajectory trajectory = (BusTrajectory) BTObject;
	ArrayList<GPSReport> reportList = trajectory.getReportList(false);
	ArrayList<GPSReport> directedSplit = new ArrayList<GPSReport>();//���ھ���
	ArrayList<GPSReport> directedMarkingList = new ArrayList<GPSReport>();//��Ƿ���
	ArrayList<GPSReport> UPList = new ArrayList<GPSReport>(500);
	ArrayList<GPSReport> DOWNList = new ArrayList<GPSReport>(500);
	Map<Direction,List<GPSReport>> directedMap = new HashMap<Direction,List<GPSReport>>();
	directedMap.put(Direction.UP, UPList);
	directedMap.put(Direction.DOWN, DOWNList);
	
	GPSReport back = reportList.get(0);
	
	TerminalIndex lastIndex = back.terminalIndex;
	TerminalIndex nowIndex = TerminalIndex.NULL;
	Direction lastDirection = Direction.TERMINAL;
	for(GPSReport front:reportList){
	    nowIndex = front.terminalIndex;
	    if(nowIndex == lastIndex){
		directedMarkingList.add(front);
		if(front.movePattern == MovePattern.STATIONAREA)
		    directedSplit.add(front);
	    }
	    else{
		Direction direction = this.statusChanged(lastIndex, nowIndex);
		if(direction != Direction.TERMINAL && directedSplit.size()>20){
		    lastDirection = (direction==Direction.UP ? Direction.DOWN : Direction.UP);
		    List<GPSReport> directedList = directedMap.get(direction);
		    directedList.addAll(directedSplit);
		}
		this.directionSetting(directedMarkingList, direction);
		
		directedSplit.clear();
		directedMarkingList.clear();
		if(front.movePattern == MovePattern.STATIONAREA)
		    directedSplit.add(front);
		directedMarkingList.add(front);
	    }
	    
	    back = front;
	    lastIndex = back.terminalIndex;
	}
	this.directionSetting(directedMarkingList, lastDirection);
	return directedMap;
    }
    
    private void markTerminalArea(Station S,TerminalIndex tIndex,KDTree GPSKDTree){
	Object[] terminalAreaCandidates = SpatialSearcher.getAdjacentOnSameRoute(S, GPSKDTree, ODAreaRadius);
	for(Object candidate:terminalAreaCandidates){
	    GPSReport candidateReport = (GPSReport) candidate;
	    double distance = Gauge.distance(S, candidateReport);
	    if(distance<=ODAreaRadius){
		candidateReport.terminalIndex = tIndex;
	    }
	}
    }
    
    private Direction statusChanged(TerminalIndex lastIndex,TerminalIndex nowIndex){
	switch(lastIndex){
	case ZERO: switch(nowIndex){
		case ONE: return Direction.DOWN;
		case TWO: return Direction.UP;
		default :return Direction.TERMINAL;
		}
	case ONE: return Direction.TERMINAL;
	case TWO: return Direction.TERMINAL;
	default :return Direction.TERMINAL;
	}
    }
    
    private void directionSetting(ArrayList<GPSReport> directedMarkingList,Direction direction){
	for(GPSReport report:directedMarkingList){
	    report.direction = direction;
	}
    }
}
