package cn.edu.uestc.wilson.ISPRSODC.test;

import cn.edu.uestc.wilson.ISPRSODC.gps.Location;

public class StaticTest {
    
    private static String s = "wislon";
    
    public static void main(String[] args) {
	// TODO Auto-generated method stub
	System.out.println(s);
	s = s + " wang";
	System.out.println(s);
	s = "LEE.Yanxi";
	System.out.println(s);
	
	s = (1==2? "wang" : "LEE");
	System.out.println(s);
	
	double[] a1 = {34.567,890.87};
	double[] a2 = {a1[1],a1[0]};
	
	System.out.println(a1[0]);
	
	int[] a3 = {};
	for(int i:a3){
	    System.out.println(i);
	}
	
    }

}
