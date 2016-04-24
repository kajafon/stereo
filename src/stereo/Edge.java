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
    ArrayList<Integer> p = new ArrayList<Integer>();
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
        return p.get(i);
    }

    public int size()
    {
        return p.size();
    }
    
    public Edge split(int cut)
    {
        if (cut < 0 || cut >= p.size()) return null;
        
        Edge e = new Edge(this, cut);
        cutTail(cut);

        return e;
    }

    public void cutTail(int from)
    {
        for (int i=p.size()-1; i>=from; i--)
        {
            p.remove(i);
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
            p.add(source.p.get(i));
            grd.add(source.grd.get(i));
        }
        
    }
    
    public Edge(int x1, int y, int grad)
    {
        p.add(x1);
        grd.add(grad);
        startRow = y;
    }

    public boolean wantIt(int xmax)
    {
        if (alive) // already has a pixel on current scan line 
            return false;

        int d = p.get(p.size() - 1) - xmax;
        
        return d<=1 && d >=-1;
    }

    public void add(int xmax, int grad)
    {
        p.add(xmax);
        grd.add(grad);
        alive = true;
    }


}
