/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.uestc.wilson.ISPRSODC.gps.Location;
import cn.edu.uestc.wilson.ISPRSODC.gps.Station;
import cn.edu.uestc.wilson.ISPRSODC.util.Gauge;

/**
 * @author weibornhigh
 *
 */
public class LWDBSCANCluster {
    private double epsilon;
    private int minPoints;
    private enum Status{
	PART_OF_CLUSTER,NOISE
    }
    private Map<Station,List<Station>> reportNeighborsList;
    private Map<Station,Status> visitedList;
    
    public LWDBSCANCluster(double epsilon,int minPoints){
	this.epsilon = epsilon;
	this.minPoints = minPoints;
	this.reportNeighborsList = new HashMap<Station,List<Station>>();
	this.visitedList = new HashMap<Station,Status>();
    }
    
    /*
     * 
     */
    public List<List<Location>> run(List<Station> dataset){
	this.initialize(dataset);
	List<List<Location>> clusters = new ArrayList<List<Location>>();
	for(Station current:dataset){
	    
	    if(this.visitedList.containsKey(current)){
		continue;
	    }
	    
	    List<Station> neighbors = this.reportNeighborsList.get(current);
	    if(neighbors.size() >= minPoints){
		List<Location> cluster = new ArrayList<Location>();
		clusters.add(this.expandCluster(cluster,current,neighbors,dataset));
	    }
	    else{
		this.visitedList.put(current, Status.NOISE);
	    }
	}
	
	return clusters;
    }
    
    private List<Location> expandCluster(List<Location> cluster,
	    					Station report,
	    					List<Station> neighbors,
	    					List<Station> dataset){
	cluster.add(report);
	this.visitedList.put(report, Status.PART_OF_CLUSTER);
	
	List<Station> seeds = new ArrayList<Station>(neighbors);
        int index = 0;
        while (index < seeds.size()) {
            final Station current = seeds.get(index);
            Status pStatus = visitedList.get(current);
            // only check non-visited points
            if (pStatus == null) {
                final List<Station> currentNeighbors = this.reportNeighborsList.get(current);
                if (currentNeighbors.size() >= minPoints) {
                    seeds = merge(seeds, currentNeighbors);
                }
            }

            if (pStatus != Status.PART_OF_CLUSTER) {
                visitedList.put(current, Status.PART_OF_CLUSTER);
                cluster.add(current);
            }

            index++;
        }
        return cluster;
    }
    
    private List<Station> merge(final List<Station> one, final List<Station> two) {
        for (Station item : two) {
            if (!one.contains(item)) {
                one.add(item);
            }
        }
        return one;
    }
    
    /*
     * To get all neighbors of every Location.
     */
    private void initialize(List<Station> dataset){
	for(Station center:dataset){

	    List<Station> neighbors = new ArrayList<Station>(5);
	    for(Station candidate:dataset){
		
		if(candidate.equals(center)){
		    continue;
		}
		
		double distance = Gauge.distance(center, candidate);
		if(distance <= epsilon){
		    neighbors.add(candidate);
		}
	    }
	    this.reportNeighborsList.put(center, neighbors);
	}
    }
}
