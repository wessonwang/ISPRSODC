/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.gps;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 * @author weibornhigh
 *
 */
public class BRMapper {
    private Map<Integer,Integer> BusRouteMap;//[BUS_ID,ROUTE_ID];
    
    public BRMapper(String BusRouteCsvPath) throws IOException{
	this.BusRouteMap = BRMapper.mapBuilder(BusRouteCsvPath);
    }
    
    public Map<Integer,Integer> getBusRouteMap(){
	return this.BusRouteMap;
    }
    
    public List<Integer> getRouteIdList(){
	Collection<Integer> routeIdSet = this.BusRouteMap.values();
	ArrayList<Integer> RouteIdList = new ArrayList<Integer>();
	for(int routeId:routeIdSet){
	    if(!RouteIdList.contains(routeId)){
		RouteIdList.add(routeId);
	    }
	}
	
	return RouteIdList;
    }
    
    private static Map<Integer,Integer> mapBuilder(String BusRouteCsvPath) throws IOException{
	Reader BusRoutein = new FileReader(BusRouteCsvPath);
	CSVFormat BusRoutecsv = CSVFormat.DEFAULT.withHeader("bus_id","route_id").withSkipHeaderRecord();
	Iterable<CSVRecord> BusRouterecords = BusRoutecsv.parse(BusRoutein);
	
	Map<Integer,Integer> BusRouteMap = new HashMap<Integer,Integer>();
	for(CSVRecord busRoute:BusRouterecords){
	    try{
		int bus_id = Integer.parseInt(busRoute.get("bus_id"));
		int route_id = Integer.parseInt(busRoute.get("route_id"));
		BusRouteMap.put(bus_id, route_id);
	    }
	    catch(NumberFormatException e){
		continue;
	    }
	}
	
	return BusRouteMap;
    }
}
