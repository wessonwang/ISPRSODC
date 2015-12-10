/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.gps;

/**
 * @author weibornhigh
 *
 */
public class Station extends Location{
    public double sequence;
    
    public Station(double lng,double lat){
	super(lng,lat);
	this.sequence = 1;
    }
}
