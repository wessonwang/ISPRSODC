/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.javaml.core.kdtree.KDTree;
import cn.edu.uestc.wilson.ISPRSODC.gps.Location;
import cn.edu.uestc.wilson.ISPRSODC.util.Gauge;
import cn.edu.uestc.wilson.ISPRSODC.util.SpatialSearcher;

/**
 * @author weibornhigh
 *
 */
public class DBSCANCluster {
    private double epsilon;
    private int minPoints;
    private enum Status{
	PART_OF_CLUSTER,NOISE
    }
    private Map<Location,List<Location>> reportNeighborsList;
    private Map<Location,Status> visitedList;
    
    public DBSCANCluster(double epsilon,int minPoints){
	this.epsilon = epsilon;
	this.minPoints = minPoints;
	this.reportNeighborsList = new HashMap<Location,List<Location>>();
	this.visitedList = new HashMap<Location,Status>();
    }
    
    /*
     * 
     */
    public List<List<Location>> run(HashSet<Location> dataset,KDTree kdTree){
	this.initialize(dataset, kdTree);
	List<List<Location>> clusters = new ArrayList<List<Location>>();
	for(Location current:dataset){
	    
	    if(this.visitedList.get(current) != null){
		continue;
	    }
	    
	    List<Location> neighbors = this.reportNeighborsList.get(current);
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
	    					Location report,
	    					List<Location> neighbors,
	    					HashSet<Location> dataset){
	cluster.add(report);
	this.visitedList.put(report, Status.PART_OF_CLUSTER);
	
	List<Location> seeds = new ArrayList<Location>(neighbors);
        int index = 0;
        while (index < seeds.size()) {
            final Location current = seeds.get(index);
            Status pStatus = visitedList.get(current);
            // only check non-visited points
            if (pStatus == null) {
                final List<Location> currentNeighbors = this.reportNeighborsList.get(current);
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
    
    private List<Location> merge(final List<Location> one, final List<Location> two) {
        final Set<Location> oneSet = new HashSet<Location>(one);
        for (Location item : two) {
            if (!oneSet.contains(item)) {
                one.add(item);
            }
        }
        return one;
    }
    
    /*
     * To get all neighbors of every Location.
     */
    private void initialize(HashSet<Location> dataset,KDTree kdTree){
	for(Location center:dataset){
	    Object[] neighborCandidates = SpatialSearcher.getAdjacentOnSameRoute(center, kdTree, epsilon);
	    int candidatesSize = neighborCandidates.length;

	    List<Location> neighbors = new ArrayList<Location>(Math.round(candidatesSize*0.8f));
	    for(Object neighborObject:neighborCandidates){
		Location neighbor = (Location) neighborObject;
		double distance = Gauge.distance(center, neighbor);
		if(distance <= epsilon && dataset.contains(neighbor) && !neighbor.equals(center)){
		    neighbors.add(neighbor);
		}
	    }
	    this.reportNeighborsList.put(center, neighbors);
	}
    }
}
