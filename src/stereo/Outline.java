/*
 * 
 * 
 */

package stereo;

import stereo.poi.Cntx;
import stereo.poi.Cntx2;
import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author Karol Presovsky
 */
public class Outline
{
    private ArrayList<Point> p = new ArrayList<Point>();
    public ArrayList<Integer> grd = new ArrayList<Integer>();
    private ArrayList<Integer> maxims = new ArrayList<Integer>();
    public ArrayList<Cntx2> points = new ArrayList<Cntx2>();

    public double weigth = 1.0;
    public boolean mark = false;
    public boolean yScan;
    
    public Point getP(int i)
    {
        if (i<0 || i>=p.size())
            return null;
        return p.get(i);
    }
    
    public Point getFirst()
    {
        return p.get(0);
    }
    
    public Point getLast()
    {
        return p.get(p.size()-1);
    }
    
    public static int dist(Point p1, Point p2)
    {
        return Math.abs(p1.x-p2.x) + Math.abs(p1.y - p2.y);    
    }
    
    public double getWeight()
    {
        return weigth;
    }

    public void setWeigth(double weigth)
    {
        this.weigth = weigth;
    }

    public Outline(Edge edge, boolean yScan)
    {
        this.mark = edge.mark;
        this.yScan = yScan;
        grd.addAll(edge.grd);
        
        int y = edge.startRow;

        for (int x:edge.x_coords)
        {
            if (yScan)
                p.add(new Point(x, y));
            else
                p.add(new Point(y, x));
            y++;
        }
        calcMaxims();
    }
    
    public void removeFirst()
    {
        if (p.isEmpty()) return;
        p.remove(0);
        grd.remove(0);
        if (maxims.size() > 0 && maxims.get(0) == 0)
            maxims.remove(0);
    }
    
    public void removeLast()
    {
        if (p.isEmpty()) return;
        if (maxims.size() > 0 && maxims.get(maxims.size()-1) == p.size()-1)
            maxims.remove(maxims.size()-1);
        p.remove(p.size()-1);
        grd.remove(grd.size()-1);
    }
    
    public boolean merge(Outline other)
    {
        int limit = 4;
        int distance = dist(getFirst(), other.getFirst());
        if (distance < limit)
        {
            if (distance == 0)
                other.removeFirst();
            
            ArrayList<Point> buffP = new ArrayList<Point>(p);
            ArrayList<Integer> buffG = new ArrayList<Integer>(grd);
            ArrayList<Integer> buffM = new ArrayList<Integer>(maxims);
            
            p.clear();
            maxims.clear();
            grd.clear();
            
            for (int i=other.size()-1; i>=0; i--)
            {
                p.add(other.p.get(i));
                grd.add(other.grd.get(i));
            }
            
            for (int i=other.maxims.size()-1; i>=0; i--)
            {
                maxims.add(other.size() - 1 - other.maxims.get(i));
            }

            p.addAll(buffP);
            grd.addAll(buffG);
            for (int c:buffM)
            {
                c += other.size();
                maxims.add(c);
            }
            ctrlMaxims();
            
            return true;
        }
        distance = dist(getFirst(), other.getLast());
        if (distance < limit)
        {
            if (distance == 0)
                other.removeLast();
            
            p.addAll     (0, other.p);
            grd.addAll   (0, other.grd);
            
            for (int i=0; i<maxims.size(); i++)
            {
                int m = maxims.get(i);
                m += other.size();
                maxims.set(i, m);
            }
            
            maxims.addAll(0, other.maxims);
            ctrlMaxims();

            return true;
        }   
        
        distance = dist(getLast(), other.getFirst());
        if (distance < limit)
        {
            if (distance == 0)
                other.removeFirst();
            int origLength = size();
            p.addAll(other.p);
            grd.addAll(other.grd);
            for(int c:other.maxims)
            {
                c += origLength;
                maxims.add(c);
            }
            ctrlMaxims();
            
            return true;
        } 
        
        distance = dist(getLast(), other.getLast());
        if ( distance < limit)
        {
            if (distance == 0)
                other.removeLast();
            
            int origLength = size();
            for(int i=other.size()-1; i>=0; i--)
            {
                p.add(other.p.get(i));
                grd.add(other.grd.get(i));
            }
            for (int i=other.maxims.size()-1; i>=0; i--)
            {
                int c = other.maxims.get(i);
                c = other.size() + origLength - 1 - c;                
                maxims.add(c);
            }
            ctrlMaxims();
            
            return true;
        }
        
        return false;
    }
    
    boolean ctrlMaxims()
    {
        int n = 0;
        for (int i = 0; i<maxims.size(); i++)
        {
            int m = maxims.get(i);
            if (m < 0 || m >= size() || n > m)
            {
                System.out.println(" problem");
                return false;
            }
            
            for (int j=i+1; j < maxims.size(); j++)
            {
                if (m == maxims.get(j))
                    System.out.println(" problem, duplikovane maxima");
            }
            n = m;
        }
        
        // ---- ctrl
        
        for (int i=0; i<p.size()-1; i++)
        {
            Point c = p.get(i);
            for (int j=i+1; j<p.size(); j++)
            {
                Point c2 = p.get(j);
                
                if (c.x == c2.x && c.y == c2.y)
                {
                    System.out.println(" problem: duplikovane body na konture, " + i + "," + j);
                    int k=0;
                }

            }            
            
        }
        // ----        
        return true;
    }
    //--v
    public Outline split(int cut)
    {
        if (cut < 0 || cut >= p.size()) return null;
        
        Outline e = new Outline(this, cut);
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
        
        for (int i=0; i<maxims.size(); i++)
        {
            if (maxims.get(i) >= p.size())
            {
                maxims.remove(i);
                i--;
            }
        }
    }
    
    public Outline(Outline source, int from)
    {
        yScan = source.yScan;
        if (from >= source.size())
            return;
        
        for (int i=from; i<source.size(); i++)
        {
            p.add(source.p.get(i));
            grd.add(source.grd.get(i));
        }
        
        for (int i=0; i<source.maxims.size(); i++)
        {
            if (source.maxims.get(i) > from)
            {
                int m = source.maxims.get(i);
                m -= from;
                maxims.add(m);
            }
        }        
        
    }    
    //--^
    
    public int size()
    {
        return p.size();
    }
    
    public class Delegate
    {
        public ArrayList<Cntx2> cntx = new ArrayList<Cntx2>();
        Cntx2 top,bottom, left, right;
        boolean connected = false;

        public Delegate()
        {
            Outline.this.collectPoints(cntx);
            int minx = 10000, miny = 10000, maxx = -10000, maxy = -10000;
            for (Cntx2 c:cntx)
            {
                if (minx>c.x) { minx=c.x; left  = c; }
                if (miny>c.y) { miny=c.y; top   = c; }
                if (maxx<c.x) { maxx=c.x; right = c; }
                if (maxy<c.y) { maxy=c.y; bottom= c; }
            }
        }
        
        public Outline getOutline()
        {
            return Outline.this;
        }
        
        public Point getHead()
        {
            return Outline.this.getFirst();
        }
        
        public Point getTail()
        {
            return Outline.this.getLast();
        }
        
        public int norm(Cntx c1, Cntx c2)
        {
            return Math.abs(c1.x - c2.x) + Math.abs(c1.y - c2.y);
        }
        
        public int[] distance(Delegate d)
        {
            // which direction d comes from
            
            //from west
            int dist = norm(getLeft(), d.getRight()) ;
            int direction = 1;
            
            //from east
            int a = norm(d.getLeft(), getRight());
            if (a < dist)
            {
                dist = a;
                direction = 2;
            }
            
            //from north
            a = norm(getTop(), d.getBottom());
            if (a < dist)
            {
                dist = a;
                direction = 3;
            }
            
            //from south
            a = norm(d.getTop(), getBottom());
            if (a < dist)
            {
                dist = a;
                direction = 4;
            }
            
            return new int[]{dist, direction};
            
        }
   /*     
        public void connect(Delegate d, int direction)
        {
            switch(direction)
            {
                case 1: getLeft().add(d.getRight()); break;
                case 2: getRight().add(d.getLeft()); break;
                case 3: getTop().add(d.getBottom()); break;
                case 4: getBottom().add(d.getTop()); break; 
                default : System.out.println("dadze hyba!!!!");
            }
            
        }
     */   
  
        public Cntx2 getBottom()
        {
            return bottom;
        }

        public Cntx2 getLeft()
        {
            return left;
        }

        public Cntx2 getRight()
        {
            return right;
        }

        public Cntx2 getTop()
        {
            return top;
        }
        
        
    }
    
    public Delegate getDelegate()
    {
        Delegate d = new Delegate();
        return d;
    }
/*
    public static void connectNeighbours(ArrayList<Delegate> list, int distance)
    {
        ArrayList<Delegate> current = new ArrayList<Delegate>();
        int y = 0;
        
        for (Delegate c:list)
        {
            if (c.getTop().y > y)
                y = c.getTop().y;
            
            int [] min = null;
            Delegate nearest = null;
            
            for (int i=0; i<current.size(); i++)
            {
                Delegate c2 = current.get(i);
                if (y - c2.getBottom().y > distance && current.size() > 1)
                {
                    current.remove(i);
                    i--;
                    continue;
                }
                int[] dst = c.distance(c2);
 
                    if (min == null || min[0] < dst[0])
                    {
                        min = dst;
                        nearest = c2;
                    }
            }
            
            if (!c.connected)
            {
                if (min == null)
                {
                    if (!current.isEmpty())
                        System.out.println("warning: unconnected delegate!");
                }
                else
                {
                    c.connect(nearest, min[1]); 
                }
            }
            current.add(c);
        }
    }        
    
  */  
    void calcMaxims()
    {
        if (p.size()<3)
        {
            maxims.add(0);            
            return;
        }

        boolean growing = true;
        int v = grd.get(0);
        for (int i=1; i<grd.size(); i++)
        {
            int v2 = grd.get(i);
            if (growing)
            {
                if (v2 < v)
                {
                    growing = false;
                    maxims.add(i-1);
                } else if (v2 == v)
                    growing = false;
            } else if (v2 > v)
            {
                growing = true;
            }
            v = v2;

        }        
    }

    /*
     * collect only sharp maxims: their neighbours are lower. not interested in flat regions
     * 
     */
    public ArrayList<Cntx2> collectMaxims(ArrayList<Cntx2> list)
    {
        ArrayList<Cntx2> buff = new ArrayList<Cntx2>();
        
        for (int m:maxims)
        {
            if (m >= p.size())
                System.out.println("");
            Point point = p.get(m);
            buff.add(new Cntx2(point.x, point.y, grd.get(m), this));
        }
        
        list.addAll(buff);

        return list;
    }
    
    public ArrayList<Cntx2> verticesToCntx()
    {
        collectPoints(points);
        return points;
    }
    
    public ArrayList<Cntx2> maximsToCntx()
    {
        collectMaxims(points);
        return points;
    }
    
    public ArrayList<Cntx2> collectPoints(ArrayList<Cntx2> list)
    {
        Cntx2 c = new Cntx2(getP(0).x, getP(0).y, grd.get(0), this);
        list.add(c);
        for (int i=1; i<size(); i++)
        {
            Cntx2 c2 = new Cntx2(getP(i).x, getP(i).y, grd.get(i), this);
            c.add(c2,0,0);
            list.add(c2);
            c = c2;
        }

        return list;
    }
    
    public static ArrayList<Cntx2> collectPoints(ArrayList<Outline> list, ArrayList<Cntx2> points)
    {
        if (points == null)
            points = new ArrayList<Cntx2>();
        for (Outline o:list)
        {
            o.collectPoints(points);
        }
        return points;
    }
    
    public void dilute(int step)
    {
        int counter = 1;
        int maxIndex = 0;
        
        
        if (maxims.size() > 0 && maxims.get(maxIndex) == 0)
            maxIndex = 1;
        
        int i=1;
        
        while(i<size()-1)
        {
            for (int k=step; k>0 && i<size()-1; k--, counter++)
            {
                if (maxIndex < maxims.size() && counter>maxims.get(maxIndex))
                    System.out.println("pozor!!!");
                
                if (maxIndex < maxims.size() && counter==maxims.get(maxIndex))
                {
                    int _m = maxims.get(maxIndex);

                    maxims.set(maxIndex, i);
                    maxIndex++;
                    if (maxIndex < maxims.size() && maxims.get(maxIndex) < counter)
                        System.out.println("pozor 3 !!!!");

                } else 
                {
                    p.remove(i);
                    grd.remove((int)i);
                }
            }
            
            if (maxIndex < maxims.size() && counter == maxims.get(maxIndex))
            {
                int _m = maxims.get(maxIndex);
                maxims.set(maxIndex, i);
                maxIndex++;
                if (maxIndex < maxims.size() && maxims.get(maxIndex) < counter)
                    System.out.println("pozor 2 : " );
            }

            i+=1;
            counter++;
        }
        
        while(maxIndex < maxims.size() && maxims.get(maxIndex)>p.size())
        {
            maxims.remove(maxIndex);
        }
    }
    
    public static void dilute(ArrayList<Outline> list1, int step)
    {
        for (Outline o:list1)
            o.dilute(step);
        
    }

    public static void reconnect(ArrayList<Outline> list1, ArrayList<Outline> list2)
    {
        // first merge from one list to other.
        // remove merged from second list
        for (Outline o:list1)
        {
            for (int i=0;i<list2.size();i++)
            {
                Outline o2 = list2.get(i);
                if (o.merge(o2))
                {
                    list2.remove(o2);
                    i--;
                }
            }
        }
        
        // merge merged second time with rest of first list
        reconnect(list1);
        reconnect(list2);

    }
    
    public static void reconnect(ArrayList<Outline> list)
    {
        for (int i=0; i<list.size()-2; i++)
        {
            Outline o = list.get(i);
            for (int j=i+1; j<list.size(); j++)
            {
                Outline o2 = list.get(j);
                if (o.merge(o2))
                {
                    list.remove(o2);
                    j--;
                }
            }
        }            
    }
}
