package cn.edu.uestc.wilson.ISPRSODC.test;

import net.sf.javaml.core.kdtree.KDTree;

public class ArrayTest {

    public static void main(String[] args) {
	// TODO Auto-generated method stub
	KDTree kdTree = new KDTree(1);
	double[] key1 = {1};
	int v1 = 2;
	kdTree.insert(key1, v1);
	double[] uppk = {5};
	double[] lowk = {1};
	Object[] objects = kdTree.range(lowk, uppk);
	for(Object object:objects){
	    System.out.println("In");
	}
    }

}
