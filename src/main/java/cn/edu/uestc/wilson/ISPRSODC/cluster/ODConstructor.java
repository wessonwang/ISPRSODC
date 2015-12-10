/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.cluster;

import java.util.List;
import java.util.PriorityQueue;

import cn.edu.uestc.wilson.ISPRSODC.gps.Location;
import cn.edu.uestc.wilson.ISPRSODC.gps.Station;
import cn.edu.uestc.wilson.ISPRSODC.util.Centroid;
import cn.edu.uestc.wilson.ISPRSODC.util.KeyPair;

/**
 * @author weibornhigh
 *
 */
public class ODConstructor {
    public static Station[] construct(List<List<Location>> clusters){
	if(clusters.size()>=2){
	    int[] ODIndexes = ODConstructor.getODClusterIndex(clusters);
	    List<Location> S1Cluster = clusters.get(ODIndexes[0]);
	    List<Location> S2Cluster = clusters.get(ODIndexes[1]);
	    double[] S1Coordination = Centroid.compute(S1Cluster);
	    double[] S2Coordination = Centroid.compute(S2Cluster);
	    Station[] terminals = {new Station(S1Coordination[0],S1Coordination[1]),
		    		new Station(S2Coordination[0],S2Coordination[1])};
	    
	    return terminals;
	}
	
	return null;
    }
    
    private static int[] getODClusterIndex(List<List<Location>> clusters){
	PriorityQueue<KeyPair> indexAmountList = new PriorityQueue<KeyPair>();
	int index = 0;
	for(List<Location> cluster:clusters){
	    int amount = cluster.size();
	    KeyPair keyPair = new KeyPair(index,amount);
	    indexAmountList.add(keyPair);
	    index++;
	}
	
	KeyPair S1 = indexAmountList.poll();
	KeyPair S2 = indexAmountList.poll();
	int[] ODIndexes = {S1.index,S2.index};
	return ODIndexes;
    }
}
