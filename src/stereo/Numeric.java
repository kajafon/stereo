/*
 * 
 * 
 */

package stereo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import stereo.to3d.Face;

/**
 *
 * @author Karol Presovsky
 */
public class Numeric
{
    
   
    
    public static double _rotateX(double x, double y, double cosx, double sinx)
    {
       return (x*cosx - y*sinx); 
    }
    
    public static double _rotateY(double x, double y, double cosx, double sinx)
    {
       return (x*sinx + y*cosx); 
    } 
    
    public static Matrix getLocalConstrast(int perimeter)
    {
        Matrix m = new Matrix(perimeter, perimeter);
        float r = perimeter/2;
        float rr = r*r;

        for (int j=0; j<perimeter; j++)
        {
            for (int i=0; i<perimeter; i++)
            {
                float x = r-i;
                float y = r-j;

                x = 1.1f - (x*x + y*y)/rr;
//                x *= x;
                if (x > 1) x=1;
                m.set((x>0?x:0), i, j);
            }
        }
        return m;
    }

    
    /**
     * 
     * @param width of rect
     * @param height of rect
     * @param x of a point on line
     * @param y of a point on line
     * @param ldx x of line vector
     * @param ldy y of line vector
     * @return line intersection defined by two points[ startx, starty, stopx, stopy]
     */
    public static int[] rectAndLine(double width, double height, double x, double y, double ldx, double ldy)
    {
        double xAxis;
        if (ldy == 0)
        {
            xAxis = Double.POSITIVE_INFINITY;
        } else
        {
            xAxis = - y / ldy;
            xAxis = x + xAxis*ldx;
        }
        
        double yAxis;
        if (ldx == 0)
        {
            yAxis = Double.POSITIVE_INFINITY;
        } else
        {
            yAxis = - x / ldx;
            yAxis = y + yAxis*ldy;
        }
        
        double atHeight;
        if (ldy == 0)
        {
            atHeight = Double.POSITIVE_INFINITY;
        } else
        {
            atHeight = (height - y) / ldy;
            atHeight = x + atHeight*ldx;
        }
        
        double atWidth;
        if (ldx == 0)
        {
            atWidth = Double.POSITIVE_INFINITY;
        } else
        {
            atWidth = (width - x) / ldx;
            atWidth = y + atWidth*ldy;
        }
        
        int startx;
        int starty;
        int stopx = Integer.MAX_VALUE;
        int stopy = Integer.MAX_VALUE;
        
        if (xAxis == 0 && yAxis == 0)
        {
            startx = 0;
            starty = 0;
        } else if (xAxis >= 0 && xAxis < width)
        {
            if  (yAxis >= 0 && yAxis < height)
            {
                startx = (int) xAxis;
                starty = 0;
                stopx = 0;
                stopy = (int) yAxis;
            } else 
            {
                startx = (int) xAxis;
                starty = 0;
            }
        } else if (yAxis >= 0 && yAxis < height)
        {
            starty = (int) yAxis;
            startx = 0;
        }
        else if (atHeight >= 0 && atHeight < width && atWidth >= 0 && atWidth < height)
        {
            startx = (int) atHeight;
            starty = (int)height-1;
            stopx = (int)width-1;
            stopy = (int)atWidth;            
        } else
        {
            return null;
        }

        if (stopx == Integer.MAX_VALUE)
        {
            if (atWidth >= 0 && atWidth < height)
            {
                stopx = (int)width-1;
                stopy = (int)atWidth;
            } else
            {
                stopx = (int)atHeight;
                stopy = (int)height-1;
            }
        }               
        
        return new int[]{startx, starty, stopx, stopy};
    }
    
    public static int doMatch(Greyscale text, Greyscale symbol, int x0, int y0)
    {
        if (x0 < 0 || y0 < 0) return Integer.MAX_VALUE;
        if (x0 + symbol.width > text.width || y0 + symbol.height > text.height)
            return Integer.MAX_VALUE;
        
        int accum = 0;
        for (int j=0; j<symbol.height; j++)
        {
            for (int i=0; i<symbol.width; i++)
            {
                int dif = text.get(x0 + i, y0 + j) - symbol.get(i, j);
                accum += Math.abs(dif);
            }
        }
        
        return accum;
        
    }
    
    public static void diagonalElimination(ArrayList<Edge> edges, boolean vertical)
    {
        int ctrl = edges.size();
        int count = 0;
        int D = vertical?1:-1;
        for (int k=0; k<edges.size(); k++)
        {
            Edge e = edges.get(k);
            if (e.size()<4)
                continue;
            
            int d2 = 0;
            int x = e.x_coords.get(1);
            int d = x - e.x_coords.get(0);
            int starti= d==D?0:1;
            int i;
            for (i=2; i<e.size(); i++)
            {
                int x2 = e.x_coords.get(i);
                d2 = x2 - x;
                if (d2 == D && d != d2)
                {
                    starti = i;
                } 
                else if (d2 != D && d==D && (i - starti > 2))
                {
                    // found end of a diagonal
                    break;
                }

                x = x2;
                d = d2;
            }
            
            if (d2 == D || d == D)
            {
                if (d2 == D)
                    i++;
                count++;
                if (e.size() - i > 1)
                {
                    Edge tail = e.split(i);
                    // adding new edge to the end of the list
                    // it will be processed later
                    edges.add(tail);
                }

                if (starti > 1)
                {
                    e.cutTail(starti);
                    /*
                    Edge tail = e.split(starti);
                    tail.mark = true;
                    edges.add(tail);
                    System.out.println("");
                     
                     */
                } else
                {
                      edges.remove(k);
                      k--;
                    //e.mark = true;
                }
            }
        }
        
        System.out.println("--- elimination:" + count + " cuts, " + (edges.size()-ctrl) + " new edges");
    }
    
    public static ArrayList<Outline> toOutlines(ArrayList<Outline> vcs, ArrayList<Edge> edges, boolean yScan)
    {
        if (vcs == null)
        {
            vcs = new ArrayList<Outline>();
        }

        for (Edge e:edges)
        {
            vcs.add(new Outline(e, yScan));
        }
        return vcs;
    }

    /**
     * greyscale should contain a gradient map of an image. than vectorizing it will find edges and store them as 
     * vectors - series of coordinates (Edge).
     * vertical == true means, that the scan line is horizontal, moves from top to bottom
     * and created vectors will have vertical orientation.
     * @param greyscale
     * @param vertical
     * @param treshold
     * @param lengthTrashhold
     * @return 
     */
    public static ArrayList<Edge> vectorize(Greyscale greyscale, boolean vertical, int treshold, int lengthTrashhold)
    {        
        class Bmp
        {
            Greyscale g;
            boolean vertical;

            public Bmp(Greyscale g, boolean vertical)
            {
                this.g = g;
                this.vertical = vertical;
            }
            
            public int getHeight()
            {
                return vertical?g.height:g.width;
            }
            
            public int getWidth()
            {
                return vertical?g.width:g.height;
            }
            
            public short get(int x, int y)
            {
                return vertical?g.get(x, y):g.get(y, x);
            }
            
        }
        
        Bmp g = new Bmp(greyscale, vertical);
        
        ArrayList<Edge> vectors = new ArrayList<Edge>();
        ArrayList<Edge> current = new ArrayList<Edge>();
        ArrayList<Edge> newvcrs = new ArrayList<Edge>();

        for (int j=0; j<g.getHeight(); j++)
        {
            newvcrs.clear();
            for (int i=0; i<g.getWidth(); i++)
            {
                short v = g.get(i, j);
                if (v > treshold)
                {
                    //////// find range of values above threshold and local maximum

                    short max = v;
                    int maxi = -1;
                    int starti = i;
                    
                    for(i++; i<g.getWidth() && (v = g.get(i, j)) > treshold; i++)
                    {
                        if (v>max)
                        {
                            max = v;
                            maxi = i;
                        }
                    }
                    
                    if (maxi == -1)
                    {
                        maxi = (starti + i)/2;
                    }

                    //////////// find edge to attach the pixel to 

                    Edge winner = null;
                    for (Edge e:current)
                    {
                        if (e.wantIt(maxi))
                        {
                            winner = e;
                        }
                    }

                    if (winner == null)
                    {
                        newvcrs.add(new Edge(maxi, j, max));
                       // System.out.println(" -- new edge");
                    } else
                    {
                        winner.add(maxi, max);
                    }

                }
            }
            
            for (int k = 0; k<current.size(); )
            {
                Edge v = current.get(k);
                if (v.alive)
                {
                    v.alive = false;
                    k++;
                } 
                else
                {
                    current.remove(k);
                    if (v.x_coords.size() < lengthTrashhold)
                    {
                        vectors.remove(v);
                    }
                }
            }
            vectors.addAll(newvcrs);
            current.addAll(newvcrs);
        }
        System.out.println("-- " + vectors.size());
        return vectors;
    }
}
