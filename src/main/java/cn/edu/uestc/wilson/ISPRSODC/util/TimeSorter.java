/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.util;

import java.util.Comparator;

import cn.edu.uestc.wilson.ISPRSODC.gps.GPSReport;

/**
 * @author weibornhigh
 *
 */
public class TimeSorter implements Comparator<GPSReport>{

    /**
     * sort the time of {@link GPSReport} from small to large.
     */
    public int compare(GPSReport p1, GPSReport p2) {
	return (p1.getTime()-p2.getTime()) > 0 ? 1:-1;
    }
}
