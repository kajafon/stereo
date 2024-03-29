/*
 * 
 * 
 */
package stereo;

import java.awt.Point;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 *
 * @author Karol Presovsky
 */
public class Edge
{
    ArrayList<Integer> x_coords = new ArrayList<Integer>();
    ArrayList<Integer> grd = new ArrayList<Integer>();
    int startRow;
    
    public boolean mark = false;

    
    // alive means that edge already has a pixel on current scan line 
    // so it is not accepting new pixels
    boolean alive = false;

    public int getStartRow()
    {
        return startRow;
    }
    
    public int getGrad(int i)
    {
        return grd.get(i);
    }

    public int getX(int i)
    {
        return x_coords.get(i);
    }

    public int size()
    {
        return x_coords.size();
    }
    
    public Edge split(int cut)
    {
        if (cut < 0 || cut >= x_coords.size()) return null;
        
        Edge e = new Edge(this, cut);
        cutTail(cut);

        return e;
    }

    public void cutTail(int from)
    {
        for (int i=x_coords.size()-1; i>=from; i--)
        {
            x_coords.remove(i);
            grd.remove(i);
        }        
    }
    
    public Edge(Edge source, int from)
    {
        if (from >= source.size())
            return;
        
        startRow = source.startRow + from;
        for (int i=from; i<source.size(); i++)
        {
            x_coords.add(source.x_coords.get(i));
            grd.add(source.grd.get(i));
        }
        
    }
    
    public Edge(int x1, int y, int grad)
    {
        x_coords.add(x1);
        grd.add(grad);
        startRow = y;
    }

    public boolean wantIt(int xmax)
    {
        if (alive) // already has a pixel on current scan line 
            return false;

        int distance = x_coords.get(x_coords.size() - 1) - xmax;
        
        return distance<=1 && distance >=-1;
    }

    public void add(int xmax, int grad)
    {
        x_coords.add(xmax);
        grd.add(grad);
        alive = true;
    }


}
