/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.afc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author weibornhigh
 *
 */
public class Passenger {
    private Map<Integer,ArrayList<AFCRecord>> records; //Map<DAY_ID,ArrayList<AFCRecord>>
    public int dayCount;
    
    public Passenger(){
	this.dayCount = 0;
	this.records = new HashMap<Integer,ArrayList<AFCRecord>>();
    }
    
    public void addAFCRecord(int day,AFCRecord record){
	if(this.records.containsKey(day)){
	    ArrayList<AFCRecord> dayList = this.records.get(day);
	    dayList.add(record);
	}
	else{
	    ArrayList<AFCRecord> dayList = new ArrayList<AFCRecord>();
	    dayList.add(record);
	    this.records.put(day, dayList);
	    dayCount++;
	}
    }
    
    public Map<Integer,ArrayList<AFCRecord>> getDAYRecords(){
	return this.records;
    }
}
