/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.afc;

import cn.edu.uestc.wilson.ISPRSODC.gps.Location;

/**
 * @author weibornhigh
 *
 */
public class Stop extends Location{
    private int route_id;
    private int direction;
    private int sequence;
    
    public Stop(int route_id,int direction,int sequence,double[] coordination){
	super(coordination[0],coordination[1]);
	this.route_id = route_id;
	this.direction = direction;
	this.sequence = sequence;
    }
    
    public int getRouteId(){
	return this.route_id;
    }
    
    public int getDirection(){
	return this.direction;
    }
    
    public int getSequence(){
	return this.sequence;
    }
    
    public double[] getCoordination(){
	return this.coordinate;
    }
}
