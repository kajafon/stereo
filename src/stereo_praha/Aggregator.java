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

    public Aggregator(int count) {
        max = new double[count];
        min = new double[count];
        sum = new double[count];
        
        for (int i=0; i<max.length; i++) {
            max[i] = Double.NEGATIVE_INFINITY;
            min[i] = Double.POSITIVE_INFINITY;            
        }
    }
    
    public void add(double[] x) {
        count++;
        
        for (int i=0; i<max.length; i++) {
           sum[i] += x[i]; 
           if (x[i] > max[i]) max[i] = x[i];
           if (x[i] < min[i]) min[i] = x[i];            
        }
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
        double v = s[0];
        for (int i=1; i<max.length; i++) {
            if (s[i] > v) v = s[i];
        }
        return v;
    }
    
}
