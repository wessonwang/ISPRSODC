package cn.edu.uestc.wilson.ISPRSODC.client;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.javaml.core.kdtree.KDTree;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.uestc.wilson.ISPRSODC.afc.AFCRecord;
import cn.edu.uestc.wilson.ISPRSODC.afc.Passenger;
import cn.edu.uestc.wilson.ISPRSODC.afc.PassengerAlightInfer;
import cn.edu.uestc.wilson.ISPRSODC.afc.Stop;
import cn.edu.uestc.wilson.ISPRSODC.util.Gauge;
import cn.edu.uestc.wilson.ISPRSODC.util.SpatialSearcher;

public class AlightInfer {
    private static final Logger log = LoggerFactory.getLogger(AlightInfer.class);
    private static String STOP_LIST_PATH = null;
    private static String AFC_LOCATED_PATH = null;
    private static String RESULT_ALIGHT_LIST_PATH = null;
    private static double searchRadius;

    public static void main(String[] args) throws IOException {
	// TODO Auto-generated method stub
	if(args.length != 4){
	    System.out.println("Usage: searchRadius STOP_LIST_PATH AFC_LOCATED_PATH RESULT_ALIGHT_LIST_PATH");
	    System.exit(0);
	}
	searchRadius = Double.parseDouble(args[0]);
	STOP_LIST_PATH = args[1];
	AFC_LOCATED_PATH = args[2];
	RESULT_ALIGHT_LIST_PATH = args[3];
	
	KDTree STOPKDTree = AlightInfer.STOPKDTree(STOP_LIST_PATH);
	Map<String,Passenger> AFCMap = AlightInfer.AFCMap(AFC_LOCATED_PATH, STOPKDTree);
	PassengerAlightInfer PAInfer = new PassengerAlightInfer(RESULT_ALIGHT_LIST_PATH,searchRadius,STOPKDTree);
	Set<String> passengerSet = AFCMap.keySet();
	int PCOUNT = passengerSet.size();
	int i = 0;
	long bt = System.currentTimeMillis()/1000;
	long ft;
	for(String cardId:passengerSet){
	    Passenger p = AFCMap.get(cardId);
	    PAInfer.ProcessOnePassenger(p);
	    
	    i++;
	    if(i%10000 == 0){
		ft = System.currentTimeMillis()/1000;
		log.info(i+" passengers finished. "+PCOUNT+ "in total. " +(ft-bt)+"s spent.");
	    }
	}
	
	PAInfer.close();
    }
    
    private static KDTree STOPKDTree(String STOP_LIST_PATH) throws IOException{
	Reader STOPin = new FileReader(STOP_LIST_PATH);
	CSVFormat STOPcsv = CSVFormat.DEFAULT.withHeader("stop_id","route_id","direction","sequence","lng","lat").withSkipHeaderRecord();
	Iterable<CSVRecord> STOPRecords = STOPcsv.parse(STOPin);
	
	KDTree STOPKDTree = new KDTree(4); //KEY = [ROUTE_ID,DIRECTION,LNG,LAT], VALUE = STOP. Note: STOP_ID is used for marking ONLY.
	for(CSVRecord STOP:STOPRecords){
	    try{
		int route_id = Integer.parseInt(STOP.get("route_id"));
		int direction = Integer.parseInt(STOP.get("direction"));
		int sequence = Integer.parseInt(STOP.get("sequence"));
		double[] coordination = {Double.parseDouble(STOP.get("lng")),Double.parseDouble(STOP.get("lat"))};
		double[] key = {route_id,direction,coordination[0],coordination[1]};
		Stop stop = new Stop(route_id,direction,sequence,coordination);
		STOPKDTree.insert(key, stop);
	    }
	    catch(NumberFormatException e){
		continue;
	    }
	}
	
	return STOPKDTree;
    }
    
    private static Map<String,Passenger> AFCMap(String AFC_LOCATED_PATH,KDTree STOPKDTree) throws IOException{
	Map<String,Passenger> PassengerMap = new HashMap<String,Passenger>();
	
	Reader AFCin = new FileReader(AFC_LOCATED_PATH);
	CSVFormat AFCcsv = CSVFormat.DEFAULT.withHeader("guid","card_id","route_id","bus_id",
							"day","time","direction","lng","lat").withSkipHeaderRecord();
	Iterable<CSVRecord> AFCRecords = AFCcsv.parse(AFCin);
	
	int i = 1;
	long bt = System.currentTimeMillis()/1000;
	long ft;
	for(CSVRecord afc:AFCRecords){
	    
	    i++;
	    if(i%100000 == 0){
		ft = System.currentTimeMillis()/1000;
		log.info(i+" AFCs reading finished. "+(ft-bt)+"s spent.");
	    }
	    
	    try{
		if(Integer.parseInt(afc.get("direction")) == 2)
		    continue;
		
		String card_id = afc.get("card_id");
		int route_id = Integer.parseInt(afc.get("route_id"));
		int direction = Integer.parseInt(afc.get("direction"));
		double[] coordination = {Double.parseDouble(afc.get("lng")),Double.parseDouble(afc.get("lat"))};
		int[] AFCRecordInfos = {Integer.parseInt(afc.get("guid")),
					route_id,
					Integer.parseInt(afc.get("time")),
					direction};
		Stop nearestStop = AlightInfer.nearestStop(route_id, direction, coordination, STOPKDTree, searchRadius);
		
		if(nearestStop == null)
		    continue;
		
		AFCRecord afcRecord = new AFCRecord(AFCRecordInfos,nearestStop);
		
		if(PassengerMap.containsKey(card_id)){
		    Passenger p = PassengerMap.get(card_id);
		    p.addAFCRecord(Integer.parseInt(afc.get("day")), afcRecord);
		}
		else{
		    Passenger p = new Passenger();
		    p.addAFCRecord(Integer.parseInt(afc.get("day")), afcRecord);
		    PassengerMap.put(card_id, p);
		}
	    }
	    catch(NumberFormatException e){
		continue;
	    }
	}
	return PassengerMap;
    }
    
    private static Stop nearestStop(int route_id, int direction, double[] coordination, KDTree STOPKDTree, double searchRadius){
	Object[] candidates = SpatialSearcher.getAdjacentStops(route_id, direction, coordination, STOPKDTree, searchRadius);
	Stop nearestStop = null;
	double minDis = Double.MAX_VALUE;
	for(Object candidateObject:candidates){
	    Stop candidate = (Stop) candidateObject;
	    double tmpDis = Gauge.distance(coordination[0], coordination[1], candidate.getLongitude(), candidate.getLatitude());
	    if(tmpDis < minDis){
		nearestStop = candidate;
		minDis = tmpDis;
	    }
	}
	
	return nearestStop;
    }
}
