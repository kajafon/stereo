/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo;

import java.security.InvalidParameterException;
import java.util.ArrayList;

/**
 *
 * @author karol presovsky
 */
public class Function
{
    ArrayList<double[]> parts = new ArrayList<double[]>();

    public Function()
    {
        
    }
    
    public Function(double[][] definition)
    {
        for (int i=0; i<definition.length; i++)
        {
            add(definition[i][0], definition[i][1]);
        }
    }
    
    public void add(double x, double y)
    {
        for(int i=0; i<parts.size(); i++)
        {
            if (parts.get(i)[0] > x)
            {
                parts.add(i, new double[]{x,y});
                return;
            } else if (parts.get(i)[0] == x)
            {
                throw new InvalidParameterException();
            }
        }
        
        parts.add(new double[]{x,y});
    }
    
    public double getY(double x)
    {
        if (parts.isEmpty())
            return Double.NaN;
        if (parts.size() == 1)
            return parts.get(0)[1];
        
        if (parts.get(0)[0] > x)
            return parts.get(0)[1];
        
        double[] before = parts.get(0);
        
        for(int i=1; i<parts.size(); i++)
        {
            double[] v = parts.get(i);
            if (v[0] > x)
            {
                return (v[1] - before[1])/(v[0] - before[0])*(x-before[0]) + before[1];
                
            } 
            before = v;
        }
        
        return before[1];
    }
    
    public static void main(String[] args)
    {
        Function f = new Function(new double[][]{{5,40},{4,10},{3,9},{2,7}});
        
        for (double x = -1; x<6; x+=0.5)
        {
            System.out.println("" + x + " -> " + f.getY(x));
        }
    }
    
}
