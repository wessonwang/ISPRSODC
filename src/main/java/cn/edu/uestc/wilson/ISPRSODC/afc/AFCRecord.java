/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.afc;

/**
 * @author weibornhigh
 *
 */
public class AFCRecord implements Comparable<AFCRecord>{
    private int[] infos;		//Integer[GUID,ROUTE_ID,TIME,DIRECTION]
    private Stop stop;
    
    public AFCRecord(int[] infos,Stop stop){
	this.infos = infos;
	this.stop = stop;
    }
    
    public int getGuid(){
	return this.infos[0];
    }
    
    public int getRouteId(){
	return this.infos[1];
    }
    
    public int getTime(){
	return this.infos[2];
    }
    
    public int getDirection(){
	return this.infos[3];
    }
    
    public Stop getStop(){
	return this.stop;
    }

    public int compareTo(AFCRecord target) {
	// TODO Auto-generated method stub
	return (this.getTime() - target.getTime()) > 0? 1:-1;
    }
}
