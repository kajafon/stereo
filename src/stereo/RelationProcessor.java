/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo;

import stereo.to3d.Face;
import stereo.poi.Cntx2;
import java.util.ArrayList;
import java.util.Iterator;
import stereo.poi.Relation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 *
 * @author karol presovsky
 */
public class RelationProcessor
{

    public Matcher bmpProc;
    public Matcher bmpProc2;
    public double maxError1;
    public double maxError2;
    public int relCountPerVertex;
    public Histogram histogram;
    public Histogram histogram2;
    public int firstPassCount;
    public int finalCount;

    public RelationProcessor(Matcher bmpProc, Matcher bmpProc2, double maxError1, double maxError2, int firstPassCount, int finalCount, Histogram histogram, Histogram histogram2)
    {
        this.bmpProc = bmpProc;
        this.bmpProc2 = bmpProc2;
        this.maxError1 = maxError1;
        this.maxError2 = maxError2;
        this.histogram = histogram;
        this.histogram2 = histogram2;
        this.firstPassCount = firstPassCount;
        this.finalCount = finalCount;
    }

    public static double calcError(int x1, int y1, int x2, int y2, Matcher bmpProc, Matcher bmpProc2, double[] err)
    {
        int dist = bmpProc.g1.width / 4;
        double e1 = bmpProc == null ? 0 : bmpProc.match(x1, y1, x2, y2);
        double e2 = bmpProc2 == null ? 0 : bmpProc2.match(x1, y1, x2, y2);
        double e3 = 0;
        e2 *= 0.3;
        if (Math.abs(x1 - x2) + Math.abs(y1 - y2) < dist)
        {
            e3 = 0;
        } else
        {
            e3 = 0.2;
        }
        if (err != null)
        {
            err[0] = e1;
            err[1] = e2;
            err[2] = e3;
        }
        return 1 - (1 - e1) * (1 - e2) * (1 - e3);
    }

    public ArrayList<stereo.poi.Relation> calcRelation(ArrayList<Cntx2> list1, ArrayList<Cntx2> list2)
    {
        System.out.println("calc relations on " + bmpProc.g1 + " and " + bmpProc.g2);
        ArrayList<stereo.poi.Relation> relations = new ArrayList<stereo.poi.Relation>();

        SortedList<Relation> buffer = new SortedList<Relation>(new Comparator<Relation>()
        {

            public int compare(Relation o1, Relation o2)
            {
                return (int) ((o1.error - o2.error) * 10000);
            }
        });


        double[] err = new double[3];

        for (Cntx2 c : list1)
        {
            for (Cntx2 c2 : list2)
            {
                if (Math.abs(c.x - c2.x) > bmpProc.g1.width / 2 || Math.abs(c.y - c2.y) > bmpProc.g1.height / 2)
                {
                    continue;
                }
                double e = calcError(c.x, c.y, c2.x, c2.y, bmpProc, bmpProc2, err);
                if (e == Double.NaN || e == Double.NEGATIVE_INFINITY || e == Double.POSITIVE_INFINITY)
                {
                    continue;
                }

                if (histogram != null)
                {
                    histogram.add(e);
                }

                if (e > maxError1)
                {
                    continue;
                }

                if (buffer.size() < firstPassCount || buffer.getLast().error > e)
                {
                    Relation r = new Relation(c, c2, e);
                    r.addError(err[0]);
                    r.addError(err[1]);
                    r.addError(err[2]);
                    buffer.add(r);
                    if (buffer.size() > firstPassCount)
                    {
                        buffer.removeLast();
                    }
                }
            }
        }

        System.out.println("first pass: " + buffer.size() + " relations, max error:" + buffer.getLast().error);
        // winners are sorted from best to worst
        for (Relation r : buffer)
        {
            r.c1.relations.add(r);
            r.c2.relations.add(r);
        }

        supportParallels(list1);

//        while(!buffer.isEmpty())
//        {
//            Relation r = buffer.removeFirst();
//            if (r.error < maxError2)
//                relations.add(r);
//            else
//            {
//                r.c1.relations.remove(r);
//                r.c2.relations.remove(r);
//            }
//        }



        // choose best relations of vertices from list1
        for (Cntx2 c : list2)
        {
            Relation winner = null;
            while (c.relations.size() > 0)
            {
                Relation r = c.relations.get(c.relations.size() - 1);
                c.relations.remove(c.relations.size() - 1);
                r.c2.relations.remove(r);
                if (r.error < maxError2 && (winner == null || winner.error > r.error))
                {
                    winner = r;
                    continue;
                }
            }
            if (winner != null)
            {
                winner.c1.relations.add(winner);
                winner.c2.relations.add(winner);
                //relations.add(winner);
            }

        }

        // final selection of the best relations
        for (Cntx2 c:list1)
        {
            Relation winner = null;
            for (int i=0; i<c.relations.size(); i++)
            {
                Relation r = c.relations.get(i);
                if (winner == null || winner.error > r.error)
                {
                    winner = r;
                    continue;
                }
            }
            if (winner != null)
            {
                if (histogram2 != null)
                    histogram2.add(winner.error);
                
                
                relations.add(winner);

            }
        }

        System.out.println("finaly : " + buffer.size() + " relations, max error:" + buffer.getLast().error);

        return relations;
    }

    public void supportParallels(ArrayList<Cntx2> list)
    {
        for (Cntx2 c : list)
        {
            int x = 0;
            int y = 0;
            int max = 0;
            int count = 0;
            for (Cntx2.Neighbour n : c.neighbours)
            {
                if (n.c.relations.isEmpty())
                {
                    continue;
                }

                for (Relation r : n.c.relations)
                {
                    int dx = r.c2.x - r.c1.x;
                    int dy = r.c2.y - r.c1.y;
                    x += dx;
                    y += dy;
                    count++;

                    dx = Math.abs(dx) + Math.abs(dy);
                    if (dx > max)
                    {
                        max = dx;
                    }
                }
            }

            if (count == 0)
            {
                continue;
            }
            x /= count;
            y /= count;

            for (Relation r : c.relations)
            {
                double diff = Math.abs(x - r.c2.x + r.c1.x) + Math.abs(y - r.c2.y + r.c1.y);
                diff /= max;
                diff *= 0.3;
                if (diff > 1.0)
                {
                    diff = 1.0;
                }
                r.addError(diff);
                r.error = 1.0 - (1.0 - r.error) * (1.0 - diff);
            }
        }

    }
    
        
    public ArrayList<stereo.poi.Relation> calcRelation_outlines(ArrayList<Outline> list1, ArrayList<Outline> list2, int nbrDistance, int nbrLimit)
    {

        for (int i=0; i<list1.size(); i++)
        {
            Outline o = list1.get(i);
            o.maximsToCntx();
            if (o.points.size() == 0)
            {
                list1.remove(i);
                i--;
            }
        }
        
        for (int i=0; i<list2.size(); i++)
        {
            Outline o = list2.get(i);
            o.maximsToCntx();
            if (o.points.size() == 0)
            {
                list2.remove(i);
                i--;
            }
        }        
//        for (Outline o:list2)
//            o.verticesToCntx();
//        
        /**
          in both collections of outlines array of "points" is of different meaning
          - in first it is array of maxims
          - in second it is array of vertices of outlines
         */
        
        ArrayList<Cntx2> points = new ArrayList<Cntx2>();
        for (Outline o:list1)
        {
            points.addAll(o.points);
        }
        
        Cntx2.connectNeighbours(points, nbrDistance, nbrLimit);
        
        for (Outline o:list1)
        {
            int size1 = o.points.size();
            for (Outline o2:list2)
            {
                double[][] matrix = comparisonMatrix(o, o2);

                /* convolution 1
                   start position: o.last ~ o2.first
                   stop position:  o.first ~ o2.last
                 */
                
             //   System.out.println("o:" + o.size() + ", o2:" + o2.size());
                
                // !!!!!

                int size2 = o2.points.size();
                
                for (int i=0; i<size1+size2-2; i++)
                {
                    int indx1 = size1 - 1 - i;
                    int indx2 = i - size1 + 1;
                    if (indx2 < 0)
                        indx2 = 0;
                    
                    if (indx1 < 0)
                        indx1 = 0;
                    
                //    System.out.println("-" + i);
                    
                    _convolution_internal(o, indx1, o2, indx2, matrix);
                }
            }
        }

        supportParallels(points);
                
        ArrayList<stereo.poi.Relation> relations = new ArrayList<stereo.poi.Relation>();
        
        for (Cntx2 c : points)
        {
            Relation winner = null;
            for (int i = 0; i < c.relations.size(); i++)
            {
                Relation r = c.relations.get(i);
                if (winner == null || winner.error > r.error)
                {
                    winner = r;
                    continue;
                }
            }
            if (winner != null)
            {
                if (histogram2 != null)
                {
                    histogram2.add(winner.error);
                }


                relations.add(winner);

            }
        }
        
        return relations;
    }
    
    double[][] comparisonMatrix(Outline o1, Outline o2)
    {
        double[][] matrix = new double[o1.points.size()][o2.points.size()];
        
        for (int i=0; i<o1.points.size(); i++)
        {
            Cntx2 c = o1.points.get(i);
            for (int j=0; j<o2.points.size(); j++)
            {
                Cntx2 c2 = o2.points.get(j);
                matrix[i][j] = calcError(c.x, c.y, c2.x, c2.y, bmpProc, bmpProc2, null);
                histogram.add(matrix[i][j]);
            }
        }
        
        return matrix;
    }
    
    boolean _compareInRow_internal(double e, Cntx2 c1, Cntx2 c2, ArrayList<Relation> row)
    {
        if (e == Double.NaN || e == Double.NEGATIVE_INFINITY || e == Double.POSITIVE_INFINITY 
           || e > maxError1)
        {
            
            // end of region under error threshold
            if (!row.isEmpty())
            {
                double scale = Math.exp(-row.size() / 2.0);
                for (Relation r : row)
                {
                    r.error *= scale;
                    //r.addError((1-scale));
                }

                row.clear();
            }
            
            return false;
        }
        
        Relation r = new Relation(c1, c2, e);
        c1.relations.add(r);
        c2.relations.add(r);
        row.add(r);
        return true;
    }
    
    void _convolution_internal(Outline o, int indx1, Outline o2, int indx2, double[][] matrix)
    {
        ArrayList<Relation> rowA = new ArrayList<Relation>();
        ArrayList<Relation> rowB = new ArrayList<Relation>();
        do
        {
            try
            {
                Cntx2 c1 = o.points.get(indx1);
                Cntx2 cA = o2.points.get(indx2);
                Cntx2 cB = o2.points.get(o2.points.size()-1-indx2);

                double eA = matrix[indx1][indx2];
                double eB = matrix[indx1][o2.points.size()-1-indx2];

                _compareInRow_internal(eA, c1, cA, rowA);
                _compareInRow_internal(eB, c1, cB, rowB);

                indx1++;
                indx2++;
            }
            catch(Exception e)
            {
                System.out.println("kokot");
            }

        } while (indx1 < o.points.size() && indx2 < o2.points.size());

        // finish rows
        _compareInRow_internal(Double.POSITIVE_INFINITY, null, null, rowA);
        _compareInRow_internal(Double.POSITIVE_INFINITY, null, null, rowB);

    }
    
    static Face createFace(int i1, int i2, int i3, ArrayList<Relation> list)
    {
        Relation r1 = list.get(i1);
        Relation r2 = list.get(i2);
        Relation r3 = list.get(i3);
        
        r1.c1.add(r2.c1);
        r1.c1.add(r3.c1);
        
        r2.c1.add(r1.c1);
        r2.c1.add(r3.c1);
        
        r3.c1.add(r1.c1);
        r3.c1.add(r2.c1);
        
        r1.c2.add(r2.c2);
        r1.c2.add(r3.c2);
        
        r2.c2.add(r1.c2);
        r2.c2.add(r3.c2);
        
        r3.c2.add(r1.c2);
        r3.c2.add(r2.c2);        
        
        return new Face(i1, i2, i3);
    }

    public static ArrayList<Face> triangulate(ArrayList<Relation> list, Triangulator.Callback callback)
    {
        
        Triangulator.FaceCreator faceCreator = new Triangulator.FaceCreator() 
        {
            public Object createFace(int i1, int i2, int i3, ArrayList linkList)
            {
                return RelationProcessor.createFace(i1, i2, i3, linkList);
            }
        };
        
        Triangulator.LinkAccess link = new Triangulator.LinkAccess() {

            public int x1(Object o)
            {
                return ((Relation)o).c1.x;
            }

            public int x2(Object o)
            {
                return ((Relation)o).c2.x;
            }

            public int y1(Object o)
            {
                return ((Relation)o).c1.y;

            }

            public int y2(Object o)
            {
                return ((Relation)o).c2.y;
            }
        };
        
        ArrayList objList = Triangulator.run(list, callback, link, faceCreator);
       

        return (ArrayList<Face>)objList;
    }
    
    public static void calcZet(ArrayList<Relation> list, int centerx, int centery)
    {
        if (list.isEmpty()) return;
        
        for (Relation r:list)
        {
            int dx = r.c2.x - r.c1.x;
            int dy = r.c2.y - r.c1.y;
            
            int sumx = 0;
            int sumy = 0;
            for (Cntx2.Neighbour n : r.c1.neighbours)
            {
                Relation nbRel = n.c.getRelation(0);
                
                sumx = nbRel.c2.x - nbRel.c1.x;
                sumy = nbRel.c2.y - nbRel.c1.y;
            }
            
            sumx /= r.c1.neighbours.size();
            sumy /= r.c1.neighbours.size();
            
            dx = Math.abs(dx - sumx);
            dy = Math.abs(dy - sumy);
          
            double d = Math.sqrt(dy*dy + dx*dx);
            
            if (d == 0) continue;
            r.z3d = 1000/d;
            r.x3d = (r.c1.x-centerx);
            r.y3d = (r.c1.y-centery);
        }
    }
    
    public static void _calcZet(ArrayList<Relation> list, int centerx, int centery)
    {
        if (list.isEmpty()) return;
        
        
        
        for (Relation r:list)
        {
            int dx = r.c2.x - r.c1.x;
            int dy = r.c2.y - r.c1.y;
          
            double d = Math.sqrt(dy*dy + dx*dx);
            
            if (d == 0) continue;
            r.z3d = 1000/d;
            r.x3d = (r.c1.x-centerx)/d;
            r.y3d = (r.c1.y-centery)/d;
        }
    }

}




    /**
     * This class is a List implementation which sorts the elements using the
     * comparator specified when constructing a new instance.
     * 
     * @param <T>
     */
    class SortedList<T> extends LinkedList<T>
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;
        /**
         * Comparator used to sort the list.
         */
        private Comparator<? super T> comparator = null;

        /**
         * Construct a new instance with the list elements sorted in their
         * {@link java.lang.Comparable} natural ordering.
         */
        public SortedList()
        {
        }

        /**
         * Construct a new instance using the given comparator.
         * 
         * @param comparator
         */
        public SortedList(Comparator<? super T> comparator)
        {
            this.comparator = comparator;
        }

        /**
         * Add a new entry to the list. The insertion point is calculated using the
         * comparator.
         * 
         * @param paramT
         */
        @Override
        public boolean add(T paramT)
        {
            int insertionPoint = Collections.binarySearch(this, paramT, comparator);
            super.add((insertionPoint > -1) ? insertionPoint : (-insertionPoint) - 1, paramT);
            return true;
        }

        /**
         * Adds all elements in the specified collection to the list. Each element
         * will be inserted at the correct position to keep the list sorted.
         * 
         * @param paramCollection
         */
        @Override
        public boolean addAll(Collection<? extends T> paramCollection)
        {
            boolean result = false;
            for (T paramT : paramCollection)
            {
                result |= add(paramT);
            }
            return result;
        }

        /**
         * Check, if this list contains the given Element. This is faster than the
         * {@link #contains(Object)} method, since it is based on binary search.
         * 
         * @param paramT
         * @return <code>true</code>, if the element is contained in this list;
         * <code>false</code>, otherwise.
         */
        public boolean containsElement(T paramT)
        {
            return (Collections.binarySearch(this, paramT, comparator) > -1);
        }
    }
