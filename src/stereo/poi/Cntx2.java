/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.poi;

import java.util.ArrayList;
import stereo.Outline;
import stereo.poi.Relation;

/**
 *
 * @author karol presovsky
 */
public class Cntx2 extends Cntx
{

    public class Neighbour
    {
        public Cntx2 c;
        public int d;

        public Neighbour(Cntx2 c, int d)
        {
            this.c = c;
            this.d = d;
        }
        
    }
    public ArrayList<stereo.poi.Relation> relations = new ArrayList<Relation>();
    public ArrayList<Neighbour> neighbours = new ArrayList<Neighbour>();
    
    public Relation getRelation(int i)
    {
        return relations.get(i);
    }
    
    public double getWorstError()
    {
        if (relations.isEmpty())
            return 100000;
        return relations.get(relations.size()-1).error;
    }
    
    public Cntx2(int x, int y, int p, Outline outline)
    {
        super(x, y, p, outline);
    }

    public int size()
    {
        return neighbours.size();
    }

    public Neighbour get(int index)
    {
        return neighbours.get(index);
    }

    public void add(Cntx2 e)
    {
            Neighbour n = new Neighbour(e, 0);
            neighbours.add(n);        
    }
    
    public void add(Cntx2 e, int distance, int limit)
    {
        if (limit == 0 || neighbours.size() < limit || neighbours.get(neighbours.size()-1).d > distance)
        {
            if (limit > 0 && neighbours.size() == limit)
                neighbours.remove(neighbours.size()-1);
            
            Neighbour n = new Neighbour(e, distance);
            int i;
            for (i=0; i<neighbours.size() && neighbours.get(i).d < distance; i++);
            neighbours.add(i,n);
        }
    }
    
    public static void calcCntx(ArrayList<Cntx2> cntxList, double thrsh1, double thrsh2)
    {
        for (int i = 0; i < cntxList.size() - 1; i++)
        {
            Cntx2 cntxi = cntxList.get(i);
            for (int j = i + 1; j < cntxList.size(); j++)
            {
                Cntx2 cntxj = cntxList.get(j);
 
                cntxi.add(cntxj, thrsh1, thrsh2);
                cntxj.add(cntxi, thrsh1, thrsh2);
            }
            cntxi.finish();
        }

        cntxList.get(cntxList.size() - 1).finish();
    }

    
    /*
    public static void calcCntx(ArrayList<Cntx2> cntxList, double thrsh1, double thrsh2)
    {
        for (int i = 0; i < cntxList.size() - 1; i++)
        {
            Cntx2 cntxi = cntxList.get(i);
            for (int j = i + 1; j < cntxList.size(); j++)
            {
                Cntx2 cntxj = cntxList.get(j);
 
                cntxi.add(cntxj, thrsh1, thrsh2);
                cntxj.add(cntxi, thrsh1, thrsh2);
            }
            cntxi.finish();
        }

        cntxList.get(cntxList.size() - 1).finish();
    }
     
     */
    /*
     * collects neighboirs along. list must be sorted along y axis
     */
    public static void connectNeighbours(ArrayList<Cntx2> list, int distance, int limit)
    {
        
        ArrayList<Cntx2> current = new ArrayList<Cntx2>();
        int y = 0;
        
        for (Cntx2 c:list)
        {
            if (c.y > y)
                y = c.y;
            
            for (int i=0; i<current.size(); i++)
            {
                Cntx2 c2 = current.get(i);
                if (y - c2.y > distance && current.size() > 1)
                {
                    current.remove(i);
                    i--;
                    continue;
                }
                
                int d = Math.abs(c.x-c2.x) + Math.abs(c.y - c2.y);
                
                if (d < distance)
                {
                    if (c.neighbours.contains(c2))
                    {
                        System.out.println(" errror : already known neighbour!!!!!!!");
                    }
                    c.add(c2, d, limit);
                    c2.add(c, d, limit);
                }
            }
            current.add(c);
        }
    }
    
   
}
