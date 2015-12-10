/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.gps;

import java.util.HashMap;
import java.util.Map;

/**
 * @author weibornhigh
 *
 */
public class TransitCenter {
    public String name;
    private Map<Integer,BusRouter> BusRouterMap;//[ROUTE_ID,BusRouter]
    
    public TransitCenter(String name){
	this.name = name;
	this.BusRouterMap = new HashMap<Integer,BusRouter>();
    }
    
    public void receiveReport(int route_id,int bus_id,int day,GPSReport report){
	if(this.BusRouterMap.containsKey(route_id)){
	    BusRouter busRouter = this.BusRouterMap.get(route_id);
	    busRouter.receiveReport(bus_id, day, report);
	}
	else{
	    BusRouter busRouter = new BusRouter();
	    busRouter.receiveReport(bus_id, day, report);
	    this.BusRouterMap.put(route_id, busRouter);
	}
    }
    
    public BusRouter getBusRouter(int route_id){
	return this.BusRouterMap.get(route_id);
    }
}
