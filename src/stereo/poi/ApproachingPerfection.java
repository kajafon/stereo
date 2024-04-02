/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.poi;

import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.TreeMap;
import stereo.Edge;
import stereo.Greyscale;
import stereo.Histogram;
import stereo.Numeric;
import stereo.Process;
import stereo.Triangulator;
import stereo.to3d.Face;
import stereo.to3d.FaceMetric;
import stereo.to3d.FtrLink;
import stereo_praha.Algebra;

/**
 *
 * @author karol presovsky
 */
public class ApproachingPerfection
{
    BufferedImage img;
    BufferedImage grad;
    public ArrayList<Feature> features;
    ArrayList<int[]> maxims;
    public double[][]stampWeights;
    
    public Greyscale gs;
    Greyscale gsGrad;
    public static int stampSize = 16;
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

        proces.gs1.contrast(0, 10);
//        grad = proces.gs1.createImage(grad);
        Greyscale.smooth(proces.gs1, proces.gs2);
        proces.switchBmp();
//        Greyscale.smooth(proces.gs1, proces.gs2);
//        proces.switchBmp();
        
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
        maxims = gsGrad.localMaxims(maxims, 200);
//        maxims = gsGrad.localMaxims_FAST(4, proximity, 50);        
    }
    
    void _calcMaxims()
    {
        
        long before = System.currentTimeMillis();
        int valueTreshold = 250;
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
    
    public ArrayList<FtrLink> findLinks_withProxymity(ApproachingPerfection other)
    {
        long before = System.currentTimeMillis();
        ArrayList<FtrLink> links = new ArrayList<FtrLink>();
        System.out.println("finding links...");
        
        for (Feature f:features)
        {
            int gridi=f.x/other.gridTileSize;
            int gridj=f.y/other.gridTileSize;
            
            TreeMap<Double, Feature> localLinks = new TreeMap<>();
            
            for (int i=gridi-1; i<gridi+1; i++)
            {
                if (i<0) continue;
                if (i>=other.proxiGrid.length)
                    break;
                for (int j=gridj-1;j<gridj+1; j++)
                {
                    if (j<0) continue;
                    if (j>=other.proxiGrid[0].length)
                        continue;
                    
                    if (i==12 || j==12)
                        System.out.println("kokot");
                    for (Object o:other.proxiGrid[i][j])
                    {
                        Feature f2 = (Feature)o;
                        double e = Feature.compare(f, f2, stampWeights);
                        localLinks.put(e, f2);
                        if (localLinks.size() > 5) {
                            localLinks.pollLastEntry();
                        }
                    }
                }
            }
            
            FtrLink l = new FtrLink(f, null, 0);            
            l.candidates = new ArrayList<>();
            for (Feature _f : localLinks.values()) {
                l.candidates.add(_f);
            }
            links.add(l);
        }
        
        System.out.println("found " + links.size() + " links in " + (System.currentTimeMillis() - before) + " ms");
       
        return links;
        
    }
    
    public ArrayList<FtrLink> findLinks(ApproachingPerfection other)
    {
        long before = System.currentTimeMillis();
        ArrayList<FtrLink> links = new ArrayList<FtrLink>();
        System.out.println(" finding links...");
        for (Feature f:features)
        {
            TreeMap<Double, Feature> localLinks = new TreeMap<>();

            for (Feature f2:other.features)
            {
                double e = Feature.compare(f, f2, stampWeights);
                localLinks.put(e, f2);
                if (localLinks.size() > 5)
                {
                    localLinks.pollLastEntry();
                }
            }
            
            FtrLink l = new FtrLink(f, localLinks.firstEntry().getValue(), 0);
            l.candidates = new ArrayList<>();
            for (Feature _f : localLinks.values()) {
                l.candidates.add(_f);
            }
            
            links.add(l);
        }
        System.out.println("found " + links.size() + " links in " + (System.currentTimeMillis() - before) + " ms");
        return links;
    }
    
    void killMaveriks(ArrayList<FtrLink> links)
    {
        if (links.size() == 0) {
            return;
        }
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
        ArrayList<Feature> features = new ArrayList<Feature>();
        
        for (int[] m:maxims)
        {
            features.add(buildFeature(m[0], m[1]));
        }
        
        return features;
    }
    
    public Feature buildFeature(int x0, int y0)
    {
        int[] midVal = new int[1];
        double[][] stamp1 = buildStamp(gs, x0, y0, stampSize, stampWeights, midVal);
        
        Feature f = new Feature(x0, y0);
        f.stamp = stamp1;
        f.midValue = midVal[0];
        
        return f;
    }
    
    public static double[][] buildStamp(Greyscale g, int x0, int y0, int stampSize, double[][] stampWeights, int[] midValPtr)
    {
        int x1 = x0 - stampSize/2;
        int y1 = y0 - stampSize/2;

        double [][] stamp = new double[stampSize][stampSize];
        double center = 0;
        int count = 0;
        double weightSum = 0; 
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
                
                weightSum += stampWeights[j][i];
                center += g.get(x, y) * stampWeights[j][i];
                count ++;
            }
        }
        
        if (count == 0)
            return null;
        
        center /= weightSum;
//        center /= count;
        
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
            midValPtr[0] = (int)center;
        return stamp;
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
            @Override
            public int x1(Object o)
            {
                return ((FtrLink)o).f1.x;
            }

            @Override
            public int x2(Object o)
            {
                return ((FtrLink)o).f2.x;
            }

            @Override
            public int y1(Object o)
            {
                return ((FtrLink)o).f1.y;
            }

            @Override
            public int y2(Object o)
            {
                return ((FtrLink)o).f2.y;
            }
        };
        ArrayList faceList = Triangulator.run(list, null, la, fc);
        
        return faceList;
    }
    
    public static void faceDescriptor(int x1, int y1, int x2, int y2, int x3, int y3, double[] target) {
        double s1 = Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
        double s2 = Math.sqrt((x3-x2)*(x3-x2) + (y3-y2)*(y3-y2));
        double s3 = Math.sqrt((x3-x1)*(x3-x1) + (y3-y1)*(y3-y1));
        double s = s1 + s2 + s3;
        if (s == 0) {
            target[0] = target[1] = target[2] = 0;
            return;
        }
        
        target[0] = s1/s;
        target[1] = s2/s;
        target[2] = s3/s;        
    }
    
    public static void pickWinners( ArrayList<FtrLink> links, ArrayList<Face> faces) {
        
        double[] d1 = new double[3];
        double[] d2 = new double[3];
        
        for (int i=0; i<faces.size(); i++) {
            Face face = faces.get(i);
            
            FtrLink l1 = links.get(face.r1);
            FtrLink l2 = links.get(face.r2);
            FtrLink l3 = links.get(face.r3);
            
            faceDescriptor(l1.f1.x, l1.f1.y, l2.f1.x, l2.f1.y, l3.f1.x, l3.f1.y, d1);
            
            double _s = Algebra.size(d1);
            if (_s == 0) {
                continue;
            }
            
            Algebra.scale(d1, 1.0/_s);
            
            double max = 0;
            Feature f1max = null;
            Feature f2max = null;
            Feature f3max = null;
            
            for (Feature f1:l1.candidates) {
                for (Feature f2:l2.candidates) {
                    for (Feature f3:l3.candidates) {
                        faceDescriptor(f1.x, f1.y, f2.x, f2.y, f3.x, f3.y, d2);
                        double _s2 = Algebra.size(d2);
                        if (_s2 == 0) {
                            continue;
                        }
                        
                        double w = Algebra.scalarValue(d1, d2);
                        w /= _s;                    
                        if (w > max) {
                            max = w;
                            f1max = f1;
                            f2max = f2;
                            f3max = f3;
                        }
                    }
                }                
            }
            
            if (max > 0) {
                l1.f2 = f1max;
                l2.f2 = f2max;
                l3.f2 = f3max;
            }   
            
            System.out.println("face " + i + " / " + faces.size());
        }        
    }
    
    public static SiftStamp buildSiftStamp(Greyscale gs, int xs, int ys) {
        if (xs < 5 || xs > gs.width - 5 || ys < 5 || ys > gs.height - 5) {
            return null;
        }
        int[] mainHistogram = new int[32];
        int[][] quadHisto = new int[4][];
        int[] resultHisto = new int[4*16];
        for (int i=0; i<quadHisto.length; i++) {
            quadHisto[i] = new int[16];
        }
        
        for (int j=0; j<8; j++) {
            int adr = (ys - 4 + j)*gs.width;
            for (int i=0; i<8; i++) {
                int pxAdr = adr + xs - 4 + i;                
                int n = gs.px[pxAdr - gs.width];
                int s = gs.px[pxAdr + gs.width];
                int w = gs.px[pxAdr - 1];
                int e = gs.px[pxAdr + 1];
                
                int dx = e - w;
                int dy = s - n;
                
                double a = Math.atan2(dy, dx);                
                double na = (a + Math.PI) / (2*Math.PI);
                int mainI = (int)(na*(mainHistogram.length-1));
                mainHistogram[mainI] += 1;
                int hy = 2*j/8;
                int hx = 2*i/8;
                int hi = hy*2 + hx;
                int[] qh = quadHisto[hi];                
                int qhi = (int)(na*(qh.length-1));
                qh[qhi] += 1;                           
            }            
        }
        int max = 0;
        int mi = 0;
        for (int i=0; i<mainHistogram.length; i++) {
            if (max < mainHistogram[i]) {
                max = mainHistogram[i];
                mi = i;
            }
        }
        
        /* principal angle in index dimension of smaller histogram of a quadrant.
           detected principal rotation will be eliminated by shifting values of quadrant histograms 
           by "id" bin positions
        */
        int id = (int)((double)mi / (mainHistogram.length-1) * (quadHisto[0].length-1));
//        double mainA = (double)mi / (mainHistogram.length-1) *2*Math.PI - Math.PI;
        
        for (int j=0; j<quadHisto.length; j++) {
            for (int i=0; i<quadHisto[j].length; i++) {
                int ri = i - id;
                if (ri < 0) {
                    ri = quadHisto[j].length + ri;
                }
                
                int rhi = j * 16 + ri;
                if (rhi >= resultHisto.length) {
                    System.out.println("kokot");
                }
                resultHisto[rhi] = quadHisto[j][i];                
            }            
        }
        double a = (double)mi/(mainHistogram.length-1)*2*Math.PI - Math.PI;
        return new SiftStamp(resultHisto, a);        
    }
}


