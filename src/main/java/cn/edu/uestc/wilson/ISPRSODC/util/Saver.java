/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.util;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import cn.edu.uestc.wilson.ISPRSODC.gps.Direction;
import cn.edu.uestc.wilson.ISPRSODC.gps.Station;

/**
 * @author weibornhigh
 *
 */
public class Saver {
    @SuppressWarnings("unchecked")
    public static int saveStopList(int route_id,int currentStopId, Map<Direction,ArrayList<Station>> StopsMap, String ResultStopListPath) throws IOException{
	String NEW_LINE_SEPARATOR = "\n";
	FileWriter StopListWriter = new FileWriter(ResultStopListPath,true);
	CSVFormat StopListFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
	CSVPrinter StopListPrinter = new CSVPrinter(StopListWriter,StopListFormat);
	
	Set<Direction> dicSet = StopsMap.keySet();
	    
	for(Direction dic:dicSet){
	    int dicIndex;
	    switch(dic){
		case UP : dicIndex =  0;break;
		case DOWN : dicIndex =  1;break;
		default : dicIndex = -1;break;
		}
	    
	    int sequence_id = 1;
	    ArrayList<Station> directedStopList = StopsMap.get(dic);
	    for(Station station:directedStopList){
		@SuppressWarnings("rawtypes")
		ArrayList stationRecord = new ArrayList();
		stationRecord.add(currentStopId);
		stationRecord.add(route_id);
		stationRecord.add(dicIndex);
		stationRecord.add(sequence_id);
		stationRecord.add((new BigDecimal(station.getLongitude())).setScale(7, BigDecimal.ROUND_HALF_UP).doubleValue());
		stationRecord.add((new BigDecimal(station.getLatitude())).setScale(7, BigDecimal.ROUND_HALF_UP).doubleValue());
		StopListPrinter.printRecord(stationRecord);
		sequence_id++;
		currentStopId++;
	    }
	}
	
	StopListWriter.flush();
	StopListWriter.close();
	StopListPrinter.close();
	
	return currentStopId;
    }
}
