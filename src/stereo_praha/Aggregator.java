/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo_praha;

/**
 *
 * @author macbook
 */
public class Aggregator {
    public double[] max;
    public double[] min;
    public double[] sum;
    public int count = 0;
    public double[] avg;

    public Aggregator(int size) {
        max = new double[size];
        min = new double[size];
        sum = new double[size];
        
        for (int i=0; i<max.length; i++) {
            max[i] = Double.NEGATIVE_INFINITY;
            min[i] = Double.POSITIVE_INFINITY;            
        }
    }
    
    public void add(double x) {
        count++;
        sum[0] += x; 
        if (x > max[0]) max[0] = x;
        if (x < min[0]) min[0] = x;            
        
    }
    
    public void add(double[] x) {
        count++;
        
        for (int i=0; i<max.length; i++) {
           sum[i] += x[i]; 
           if (x[i] > max[i]) max[i] = x[i];
           if (x[i] < min[i]) min[i] = x[i];            
        }
    }
    
    public double getAverage(int i)
    {
        if (avg == null){
            avg = getAverage();
        }
        return avg[i];
    }
            
    
    public double[] getAverage()
    {
        double[] av = new double[sum.length];
        for (int i=0; i<sum.length; i++) {
            av[i] = sum[i]/count;
        }
        return av;
    }
    
    public double[] getSizes()
    {
        double[] v = new double[max.length];
        for (int i=0; i<max.length; i++) {
            v[i] = (max[i]-min[i]);
        }
        return v;
    }
    
    public double getSize()
    {
        double[] s = getSizes();
        double out = 0;
        for (double v : s) {
            out += v * v;
        }
        return Math.sqrt(out);
    }
    
}
