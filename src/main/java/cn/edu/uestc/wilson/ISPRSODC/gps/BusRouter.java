/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.gps;

import net.sf.javaml.core.kdtree.KDTree;

/**
 * @author weibornhigh
 *
 */
public class BusRouter {
    private KDTree BTKDTree;//key=[BUS_ID,DAY], value=BusTrajectory;
    private KDTree GPSKDTree;//key=[BUS_ID,DAY,TIME,LNG,LAT], value=GPSReport;
    
    public BusRouter(){
	this.BTKDTree = new KDTree(2);
	this.GPSKDTree = new KDTree(5);
    }
    
    public void receiveReport(int bus_id,int day,GPSReport report){
	double[] BTkey = {bus_id,day};
	double[] GPSkey = {bus_id,day,report.getTime(),report.getLongitude(),report.getLatitude()};
	Object BTObject = this.BTKDTree.search(BTkey);
	this.GPSKDTree.insert(GPSkey, report);
	if(BTObject != null){
	    BusTrajectory BT = (BusTrajectory) BTObject;
	    BT.addLocation(report);
	}
	else{
	    BusTrajectory BT = new BusTrajectory();
	    BT.addLocation(report);
	    this.BTKDTree.insert(BTkey, BT);
	}
    }
    
    public Object[] getAllBusTrajectory(){
	double[] uppk = {Integer.MAX_VALUE,Integer.MAX_VALUE};
	double[] lowk = {0,0};
	
	return this.BTKDTree.range(lowk, uppk);
    }
    
    public KDTree getGPSKDTree(){
	return this.GPSKDTree;
    }
}
