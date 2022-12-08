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
    double[] sizes;
  
    boolean calculated = false;
    
    public Aggregator(int size) {
        max = new double[size];
        min = new double[size];
        sum = new double[size];
        avg = new double[size];
        sizes = new double[size];

        reset();        
    }
    
    public void add(double x) {
        calculated = false;
        count++;
        sum[0] += x; 
        if (x > max[0]) max[0] = x;
        if (x < min[0]) min[0] = x;                    
    }
    
    public void add(double[] x) {
        count++;
        calculated = false;
        
        for (int i=0; i<max.length; i++) {
           sum[i] += x[i]; 
           if (x[i] > max[i]) max[i] = x[i];
           if (x[i] < min[i]) min[i] = x[i];            
        }
    }
    
    public double getAverage(int i)
    {
        if (!calculated){
            calcAverage();
        }
        return avg[i];
    }    
    
    public double[] getAverage(double[] target) {
        if (target == null) {
            target = new double[avg.length];
        }
        
        if (!calculated) {
            calcAverage();
        }
        
        System.arraycopy(avg, 0, target, 0, avg.length);
        return target;
    }
    
    void calcAverage()
    {
        for (int i=0; i<sum.length; i++) {
            avg[i] = sum[i]/count;
        }
        calculated = true;
    }
    
    public double[] getSizes(double[] target)
    {
        if (target == null) {
            target = new double[avg.length];
        }
        for (int i=0; i<max.length; i++) {
            target[i] = (max[i]-min[i]);
        }
        return target;
    }
    
    public double getSize()
    {
        getSizes(sizes);
        double out = 0;
        for (double v : sizes) {
            out += v * v;
        }
        return Math.sqrt(out);
    }
    
    public final void reset() {
        count = 0;
        calculated = false;
        for (int i=0; i<max.length; i++) {
            max[i] = Double.NEGATIVE_INFINITY;
            min[i] = Double.POSITIVE_INFINITY;            
        }
    }    
}
