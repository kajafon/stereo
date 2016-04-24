/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.poi;

import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.util.ArrayList;
import stereo.Edge;
import stereo.Greyscale;
import stereo.Histogram;
import stereo.Numeric;
import stereo.Process;
import stereo.Triangulator;
import stereo.to3d.Face;
import stereo.to3d.FaceMetric;
import stereo.to3d.FtrLink;

/**
 *
 * @author karol presovsky
 */
public class ApproachingPerfection
{
    BufferedImage img;
    BufferedImage grad;
    ArrayList<Feature> features;
    ArrayList<int[]> maxims;
    double[][]stampWeights;
    
    Greyscale gs;
    Greyscale gsGrad;
    int stampSize = 80;
    ArrayList[][] proxiGrid;
    double proximity = 0.3;
    int gridTileSize;
    ArrayList<Face> faces;

    public void setFaces(ArrayList<Face> faces) {
        this.faces = faces;
    }
    
    public ApproachingPerfection(BufferedImage img)
    {
        stampWeights = calcWeights();
        this.img = img;
        Process proces = new Process(Greyscale.toGreyscale(img));

        Greyscale.edgesFilter(proces.gs1, proces.gs2);
        proces.switchBmp();

        proces.gs1.contrast(0, 50);
//        grad = proces.gs1.createImage(grad);
        Greyscale.smooth(proces.gs1, proces.gs2);
        proces.switchBmp();
        Greyscale.smooth(proces.gs1, proces.gs2);
        proces.switchBmp();
        
        gsGrad = proces.gs1;
        gs = Greyscale.toGreyscale(img);        
        //maxims = gsGrad.localMaxims(null, null, 200);
        
        calcMaxims();
        grad = gsGrad.createImage(null);

        
        features = buildFeatures(maxims, gs);
        
        buildProximityGrid();
    }

    
    void buildProximityGrid()
    {
        gridTileSize = (int)(img.getWidth()*proximity);
        int gridSizeX = img.getWidth()/gridTileSize+1;
        int gridSizeY = img.getHeight()/gridTileSize+1;

        ArrayList[][] grid = new ArrayList[gridSizeX][gridSizeY];
        for (int j=0;j<gridSizeY; j++)
            for (int i=0;i<gridSizeX; i++)
                grid[i][j] =  new ArrayList();
        
        for (Feature f:features)
        {
            int i = f.x/gridTileSize;
            int j = f.y/gridTileSize;
            grid[i][j].add(f);
        }
        
        proxiGrid = grid;
    }
    
    void calcMaxims()
    {
        maxims = new ArrayList<int[]>(500);
        gsGrad.localMaxims(maxims, 100);
    }
    
    void _calcMaxims()
    {
        
        long before = System.currentTimeMillis();
        int valueTreshold = 30;
        int lengthTreshold = 5;
        
        ArrayList<Edge> edgesVerti = Numeric.vectorize(gsGrad, true, valueTreshold, lengthTreshold);
        ArrayList<Edge> edgesHoriz = Numeric.vectorize(gsGrad, false, valueTreshold, lengthTreshold);
        
        maxims = new ArrayList<int[]>(500);
        
        for (Edge e:edgesVerti)
        {
            for (int i=1; i<e.size()-1; i++)
            {
                if (e.getGrad(i) > e.getGrad(i-1) && e.getGrad(i) > e.getGrad(i+1))
                {
                    int y = e.getStartRow() + i;
                    int x = e.getX(i);
                    maxims.add(new int[]{x,y});
                }
                
            }
        }
        for (Edge e:edgesHoriz)
        {
            for (int i=1; i<e.size()-1; i++)
            {
                if (e.getGrad(i) > e.getGrad(i-1) && e.getGrad(i) > e.getGrad(i+1))
                {
                    int x = e.getStartRow() + i;
                    int y = e.getX(i);
                    maxims.add(new int[]{x,y});
                }
                
            }
        }
        
        System.out.println("maxims took " + (System.currentTimeMillis() - before) + "ms");
    }
    
    public ArrayList<int[]> getMaxims()
    {
        return maxims;
    }

    
    public BufferedImage getImg()
    {
        return img;
    }

    public BufferedImage getGrad()
    {
        return grad;
    }
    
    

    
    public ArrayList<FtrLink> findLinks_withProxymity(ApproachingPerfection thing)
    {
        long before = System.currentTimeMillis();
        ArrayList<FtrLink> links = new ArrayList<FtrLink>();
        System.out.println("finding links...");
        for (Feature f:features)
        {
            FtrLink l = null;
            int gridi=f.x/thing.gridTileSize;
            int gridj=f.y/thing.gridTileSize;
            
            for (int i=gridi-1; i<gridi+1; i++)
            {
                if (i<0) continue;
                if (i>=thing.proxiGrid.length)
                    break;
                for (int j=gridj-1;j<gridj+1; j++)
                {
                    if (j<0) continue;
                    if (j>=thing.proxiGrid[0].length)
                        continue;
                    
                    if (i==12 || j==12)
                        System.out.println("kokot");
                    for (Object o:thing.proxiGrid[i][j])
                    {
                        Feature f2 = (Feature)o;
                        double e = compare(f, f2);
                        if (e < 0.5 && (l == null || e < l.e))
                        {
                            l = new FtrLink(f,f2,e);
                        }
                    }
                }
            }
            
            if (l != null)
               links.add(l);
        }
        
        System.out.println("found " + links.size() + " links in " + (System.currentTimeMillis() - before) + " ms");
        
        killMaveriks(links);
        
        
        return links;
        
    }
    
    public ArrayList<FtrLink> findLinks(ApproachingPerfection thing)
    {
        long before = System.currentTimeMillis();
        ArrayList<FtrLink> links = new ArrayList<FtrLink>();
        System.out.println(" finding links...");
        for (Feature f:features)
        {
            FtrLink l = null;
            for (Feature f2:thing.features)
            {
                double e = compare(f, f2);
                if (e < 0.5 && (l == null || e < l.e))
                {
                    l = new FtrLink(f,f2,e);
                }
            }
            if (l != null)
               links.add(l);
        }
        
        System.out.println("found " + links.size() + " links in " + (System.currentTimeMillis() - before) + " ms");
        
        killMaveriks(links);
        
        
        return links;
        
    }
    
    void killMaveriks(ArrayList<FtrLink> links)
    {
      //----- statistics to remove crazy maveriks links accross the image span
        long before = System.currentTimeMillis();
        int driftx = 0;
        int drifty = 0;
        for (FtrLink l:links)
        {
            driftx += Math.abs(l.f1.x - l.f2.x);
            drifty += Math.abs(l.f1.y - l.f2.y);
        }
        
        driftx /= links.size();
        drifty /= links.size();
        
        double devx = 0;
        double devy = 0;
        
        for (FtrLink l:links)
        {
            int v = Math.abs(l.f1.x - l.f2.x) - driftx;
            devx += v*v;
            v = Math.abs(l.f1.y - l.f2.y) - drifty;
            devy += v*v;
        }
        
        devx = Math.sqrt(devx/links.size());
        devy = Math.sqrt(devy/links.size());
        
        int countbefore = links.size();
                
        for (int i=0; i<links.size(); )
        {
            FtrLink l = links.get(i);
            double v1 = Math.abs(l.f1.x - l.f2.x) - devx;
            double v2 = Math.abs(l.f1.y - l.f2.y) - devy;
            if (v1 > devx*1.2 || v2 > devy*1.2)
                links.remove(i);
            else
                i++;
   
        }
        System.out.println("found and killed " + (countbefore - links.size()) + " maveriks in " + (System.currentTimeMillis() - before) + "ms");
    }
    
    double[][] calcWeights()
    {
        double [][] weight = new double[stampSize][stampSize];
        int m = (stampSize*stampSize-2)/4;
        for (int j=0; j<stampSize; j++)
        {
            double y = j - stampSize/2.0 + 0.5;
            for (int i=0; i<stampSize; i++)
            {
                double x = i - stampSize/2.0 + 0.5;
                x = x*x + y*y;
                x /= m;
                x = 1.0 - x;
                if (x < 0) x = 0;
                
                weight[j][i] = x;
            }
        }
        
        return weight;
    }
    
    private ArrayList<Feature> buildFeatures(ArrayList<int[]> maxims, Greyscale g)
    {
        System.out.println("building " + maxims.size() + " features.");
        ArrayList<Feature> feat = new ArrayList<Feature>();
        
        for (int[] m:maxims)
        {
            feat.add(buildFeature(m[0], m[1]));
        }
        
        return feat;
    }
    
    public Feature buildFeature(int x0, int y0)
    {
        int[] midVal = new int[1];
        int[] midVal2 = new int[1];
        double[][] stamp1 = buildStamp(gs, x0, y0, stampSize, stampWeights, midVal);
        double[][] stamp2 = buildStamp(gsGrad, x0, y0, stampSize, stampWeights, midVal2);
        
        Feature f = new Feature(x0, y0);
        f.stamp = stamp1;
        f.stamp2 = stamp2;
        f.midValue = midVal[0];
        f.midValue2 = midVal2[0];
        
        return f;
    }
    
    public static double[][] buildStamp(Greyscale g, int x0, int y0, int stampSize, double[][] stampWeights, int[] midValPtr)
    {
        int x1 = x0 - stampSize/2;
     //   int x2 = x1 + stampSize;
        int y1 = y0 - stampSize/2;
     //   int y2 = y1 + stampSize;

        double [][] stamp = new double[stampSize][stampSize];
        int center = 0;
        int count = 0;
        
        for (int j=0; j<stampSize; j++)
        {
            int y = y1 + j;
            if (y >= g.height)
                break;
            if (y < 0) continue;
            for (int i=0; i<stampSize; i++)
            {
                int x = x1 + i;
                if (x < 0 || x >= g.width)
                    continue;
                
                center += g.get(x, y);
                count ++;
            }
        }
        
        if (count == 0)
            return null;
        
        center /= count;
        
        double dev = 0;
        
        for (int j=0; j<stampSize; j++)
        {
            int y = y1 + j;
            if (y >= g.height)
                break;
            if (y < 0) continue;
            for (int i=0; i<stampSize; i++)
            {
                int x = x1 + i;
                if (x < 0 || x >= g.width)
                    continue;
                
                double v = (g.get(x, y) - center);
                dev += v*v;
            }
        }
        
        dev = Math.sqrt(dev/count);
        
        if (dev > 0)
        {
            for (int j=0; j<stampSize; j++)
            {
                int y = y1 + j;
                if (y >= g.height)
                    break;
                if (y < 0) continue;
                for (int i=0; i<stampSize; i++)
                {
                    int x = x1 + i;
                    if (x < 0 || x >= g.width)
                        continue;

                    double v = g.get(x, y);
                    stamp[j][i] = v/dev*stampWeights[j][i];
                }
            }
        }
        if (midValPtr != null)
            midValPtr[0] = center;
        return stamp;
    }
     
    public static double compare(Feature f1, Feature f2)
    {
        double e = 0;
        double e2 = 0;
        for (int j=0; j<f1.stamp.length; j++)
        {
            for (int i=0; i<f1.stamp[0].length; i++)
            {
                double v = f1.stamp[j][i] - f2.stamp[j][i];
                e += v*v;
                v = f1.stamp2[j][i] - f2.stamp2[j][i];
                e2 += v*v;
            }
        }
        
        e  /= f1.stamp.length*f1.stamp[0].length;
        e2 /= f1.stamp.length*f1.stamp[0].length;
        
        double e3 = Math.abs((double)f1.midValue - f2.midValue)/(f1.midValue + f2.midValue);
        double e4 = Math.abs((double)f1.midValue2 - f2.midValue2)/(f1.midValue2 + f2.midValue2);
        e = 1-(1-e)*(1-e2)*(1-e3)*(1-e4);
        return e;
    }
    
    public static Histogram calcLinkAngleHistogram(ArrayList<FtrLink> list)
    {
        Histogram histogram = new Histogram(-4, 4, 200);
        for (int i=0; i<list.size()-1; i++)
        {
            FtrLink link1 = list.get(i);
            for (int j=i+1; j<list.size(); j++)
            {
                
                FtrLink link2 = list.get(j);
                int dx1 = link1.f1.x - link2.f1.x;
                int dx2 = link1.f2.x - link2.f2.x;
                int dy1 = link1.f1.y - link2.f1.y;
                int dy2 = link1.f2.y - link2.f2.y;

                double angle1 = Math.atan2(dy1, dx1);
                double angle2 = Math.atan2(dy2, dx2);
                double dif = angle1 - angle2;
                if (dif > Math.PI)
                    dif = -2*Math.PI + dif;
                if (dif < -Math.PI)
                    dif = 2*Math.PI + dif;
                
                
                histogram.add(dif);
            }
        }
        return histogram;
    }
    
    public static ArrayList<Face> triangulate(ArrayList<FtrLink> list)
    {
        Triangulator.FaceCreator fc = new Triangulator.FaceCreator() {

            public Object createFace(int i1, int i2, int i3, ArrayList linkList)
            {
                return new Face(i1, i2, i3);
            }
        };
        
        Triangulator.LinkAccess la = new Triangulator.LinkAccess() 
        {
            public int x1(Object o)
            {
                return ((FtrLink)o).f1.x;
            }

            public int x2(Object o)
            {
                return ((FtrLink)o).f2.x;
            }

            public int y1(Object o)
            {
                return ((FtrLink)o).f1.y;
            }

            public int y2(Object o)
            {
                return ((FtrLink)o).f2.y;
            }
        };
        ArrayList faceList = Triangulator.run(list, null, la, fc);
        
        return faceList;
    }
    

    
    
    
    
}
