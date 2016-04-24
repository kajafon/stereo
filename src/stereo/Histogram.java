/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo;

/**
 *
 * @author karol presovsky
 */
public class Histogram
{
    int[] graph;
    
    public double _min = Double.MAX_VALUE;
    public double _max = Double.MIN_VALUE;
    double minimum;
    double maximum;
    
    int overshooCount;
    int undershootCount;

    public Histogram(double minimum, double maximum, int size)
    {
        this.minimum = minimum;
        this.maximum = maximum;
        graph = new int[size];
    }
    
    public int get(int i)
    {
        return graph[i];
    }
    
    public int size()
    {
        return graph.length;
    }

    public double getMaximum()
    {
        return maximum;
    }

    public void setMaximum(double maximum)
    {
        this.maximum = maximum;
    }

    public double getMinimum()
    {
        return minimum;
    }

    public void setMinimum(double minimum)
    {
        this.minimum = minimum;
    }
    
    public void add(double value)
    {
        if (value < _min) _min = value;
        if (value > _max) _max = value;
        
        int index = (int)((value - minimum)/(maximum - minimum)*graph.length);
        if (index<0)
        {
            System.out.println("kokot:" + value + " -> " + index);
            undershootCount++;
            index = 0;
        }
        if (index >= graph.length)
        {
            System.out.println("kokot:" + value + " -> " + index);
            overshooCount++;
            index = graph.length-1;
        }
        
        graph[index]++;
        
    }

    public int getOvershooCount()
    {
        return overshooCount;
    }

    public int getUndershootCount()
    {
        return undershootCount;
    }

    public int[] getGraph()
    {
        return graph;
    }
    
    
    
    
}
