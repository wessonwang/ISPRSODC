/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.afc;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import net.sf.javaml.core.kdtree.KDTree;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import cn.edu.uestc.wilson.ISPRSODC.util.Gauge;
import cn.edu.uestc.wilson.ISPRSODC.util.SpatialSearcher;

/**
 * @author weibornhigh
 *
 */
public class PassengerAlightInfer {
    private static String NEW_LINE_SEPARATOR = "\n";
    private FileWriter StopListWriter;
    private CSVPrinter StopListPrinter;
    private double searchRadius;
    private KDTree STOPKDTree;
    
    public PassengerAlightInfer(String RESULT_ALIGHT_LIST_PATH,double searchRadius,KDTree STOPKDTree) throws IOException{
	this.StopListWriter = new FileWriter(RESULT_ALIGHT_LIST_PATH);
	this.StopListPrinter = new CSVPrinter(StopListWriter,CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR));
	this.searchRadius = searchRadius;
	this.STOPKDTree = STOPKDTree;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void ProcessOnePassenger(Passenger P) throws IOException{
	Map<Integer,ArrayList<AFCRecord>> DAYRecords = P.getDAYRecords();
	Set<Integer> DAYSet = DAYRecords.keySet();
	AFCRecord thisAFC = null;
	AFCRecord nextAFC = null;
	
	for(int DAY:DAYSet){
	    ArrayList<AFCRecord> DAYTrip = DAYRecords.get(DAY);
	    if(DAYTrip.size() <2)
		continue;
	    
	    Collections.sort(DAYTrip);
	    int tripCount = DAYTrip.size();
	    
	    for(int i=0; i<tripCount; i++){
		thisAFC = DAYTrip.get(i);
		if(i == tripCount-1){
		    nextAFC = DAYTrip.get(0);
		}
		else{
		    nextAFC = DAYTrip.get(i+1);
		}
		
		Stop alight = PassengerAlightInfer.getAlight(thisAFC.getRouteId(), thisAFC.getDirection(), nextAFC.getStop(), STOPKDTree, searchRadius);
		if(alight == null)
		    continue;
		
		ArrayList alightResult = new ArrayList();
		alightResult.add(thisAFC.getGuid());
		alightResult.add(alight.getLongitude());
		alightResult.add(alight.getLatitude());
		this.StopListPrinter.printRecord(alightResult);
	    }
	}
    }
    
    private static Stop getAlight(int route_id, int direction, Stop nextStart, KDTree STOPKDTree, double searchRadius){
	Object[] candidates = SpatialSearcher.getAdjacentStops(route_id, direction, nextStart.getCoordination(), STOPKDTree, searchRadius);
	Stop nearestStop = null;
	double minDis = Double.MAX_VALUE;
	for(Object candidateObject:candidates){
	    Stop candidate = (Stop) candidateObject;
	    double tmpDis = Gauge.distance(candidate,nextStart);
	    if(tmpDis < minDis){
		nearestStop = candidate;
		minDis = tmpDis;
	    }
	}
	
	return nearestStop;
    }
    
    public void close() throws IOException{
	this.StopListWriter.flush();
	this.StopListWriter.close();
	this.StopListPrinter.close();
    }
}
