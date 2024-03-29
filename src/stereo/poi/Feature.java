/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.poi;

import stereo_praha.Algebra;

/**
 *
 * @author karol presovsky
 */
public class Feature
{
    public int x;
    public int y;
    
    public double[][] stamp;
    public int midValue;
    
    public double score = 0;

    public Feature(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public static double compare(Feature f1, Feature f2, double[][] weights) 
    {
        double res = Double.MAX_VALUE;
        
        res = Math.min(res, Algebra.compare(f1.stamp, f1.midValue, f2.stamp, f2.midValue, weights, 0, 0));
//        res = Math.min(res, Algebra.compare(f1.stamp, f1.midValue, f2.stamp, f2.midValue, weights, 1, 0));
//        res = Math.min(res, Algebra.compare(f1.stamp, f1.midValue, f2.stamp, f2.midValue, weights, 1, 1));
//        res = Math.min(res, Algebra.compare(f1.stamp, f1.midValue, f2.stamp, f2.midValue, weights, 0, 1));
//        res = Math.min(res, Algebra.compare(f1.stamp, f1.midValue, f2.stamp, f2.midValue, weights, -1, 0));
//        res = Math.min(res, Algebra.compare(f1.stamp, f1.midValue, f2.stamp, f2.midValue, weights, -1, -1));
//        res = Math.min(res, Algebra.compare(f1.stamp, f1.midValue, f2.stamp, f2.midValue, weights, 0, -1));
        
        return res;        
    }
    
    public static double _compare(Feature f1, Feature f2)
    {
        double e_stamp = 0;
        for (int j=0; j<f1.stamp.length; j++)
        {
            for (int i=0; i<f1.stamp[0].length; i++)
            {
                double v = f1.stamp[j][i] - f2.stamp[j][i];
                e_stamp += v*v;
            }
        }
        
        e_stamp  /= f1.stamp.length*f1.stamp[0].length;
        
        double e_mid = Math.abs((double)f1.midValue - f2.midValue)/(f1.midValue + f2.midValue);
        e_stamp = 1-(1-e_stamp)*(1-e_mid);
        return e_stamp;
    }
}
