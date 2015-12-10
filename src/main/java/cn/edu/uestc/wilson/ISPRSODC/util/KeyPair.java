/**
 * 
 */
package cn.edu.uestc.wilson.ISPRSODC.util;

/**
 * @author weibornhigh
 *
 */
public class KeyPair implements Comparable<KeyPair>{
    public int index;
    public int amount;
    
    public KeyPair(int index,int amount){
	this.index = index;
	this.amount = amount;
    }

    public int compareTo(KeyPair target) {
	// TODO Auto-generated method stub
	return (this.amount - target.amount) > 0 ? -1:1;
    }
}
