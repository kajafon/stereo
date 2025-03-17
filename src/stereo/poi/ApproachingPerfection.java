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
import stereo.to3d.MatchedFtr;
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
    public Greyscale gsExp;
    Greyscale gsGrad;
    public static int stampSize = 20;
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
        
        grad = gsGrad.createImage(null);
        
        gsExp = smoothCorner(gs, 5);
        maxims = calcMaxims(gsExp);

        
        features = buildFeatures(maxims, gs);
    }

    
    
    static ArrayList<int[]> calcMaxims(Greyscale src)
    {
        ArrayList<int[]> maxims = src.localMaxims(null, 90);
        return maxims;
//        maxims = gsGrad.localMaxims_FAST(4, proximity, 50);        
    
    }
    
    Greyscale smoothCorner(Greyscale src, int cornerThrs) {
        Greyscale res = new Greyscale(src.width, src.height);
        int[] tmp = new int[src.px.length];
        int min = 100000;
        int max = -10000;
        int cirRad = 5;
        CircleRaster cir = new CircleRaster(cirRad);
        int cornerLength = cir.length() * 4 / 5;
        for (int j = cirRad; j<src.height-cirRad; j++) {
            for (int i = cirRad; i<src.width-cirRad; i++) {
                int v = src.get(i, j);
                int sum = 0;
                int cornerCount = 0;
                boolean isCorner = false;
                
                for (int k=0; k<cir.length(); k++) {
                    int vk = src.get(i+cir.getX(k), j+cir.getY(k));                    
                    int pxDif = v - vk;
                    if (pxDif < 0) {
                        pxDif = -pxDif;
                    }
                    
                    if (pxDif > cornerThrs) {
                        cornerCount++;
                        if (cornerCount >= cornerLength) {
                            isCorner = true;
                        }
                    } else {
                        cornerCount = 0;
                    }
                    
                    sum += pxDif*pxDif;                    
                }               
                if (!isCorner) {
                    sum = 0;
                }
                
                tmp[j*res.width + i] = sum;                
                if (min > sum) {
                    min = sum;
                }
                if (max < sum) {
                    max = sum;
                }
            }            
        }    
        
        int range = max - min;
        
        for (int i=0; i<res.px.length; i++) {
            res.px[i] = (short)((double)(tmp[i]-min)/range*255);
        }
        
        return res;
    }
    
    static ArrayList<int[]> _calcMaxims(Greyscale gsGrad)
    {
        
        long before = System.currentTimeMillis();
        int valueTreshold = 250;
        int lengthTreshold = 5;
        
        ArrayList<Edge> edgesVerti = Numeric.vectorize(gsGrad, true, valueTreshold, lengthTreshold);
        ArrayList<Edge> edgesHoriz = Numeric.vectorize(gsGrad, false, valueTreshold, lengthTreshold);
        
        ArrayList<int[]> maxims = new ArrayList<int[]>(500);
        
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
        
        return maxims;
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
  
    
  
    public ArrayList<FtrLink> findLinks(ApproachingPerfection other)
    {
        long before = System.currentTimeMillis();
        ArrayList<FtrLink> links = new ArrayList<FtrLink>();
        System.out.println(" finding links...");
        for (Feature f:features)
        {
            TreeMap<Double, MatchedFtr> candidatesMap = new TreeMap<>();

            for (Feature f2:other.features)
            {
                double e = Feature.compare(f, f2, stampWeights);
                candidatesMap.put(e, new MatchedFtr(f2, e, 0));
                if (candidatesMap.size() > 20)
                {
                    candidatesMap.pollLastEntry();
                }
            }
            
            if (candidatesMap.size() > 0) {
                FtrLink l = new FtrLink(f, candidatesMap.firstEntry().getValue(), 0);
                for (MatchedFtr _mf2 : candidatesMap.values()) {
                    _mf2.f.numRefs++;
                    l.candidates.add(_mf2);
                }

                links.add(l);
            }
        }
        int REF_THRSH = 10;
        ArrayList<FtrLink> filteredLinks = new ArrayList<>(links.size());
        for (FtrLink link :links) 
        {
            ArrayList<MatchedFtr> filteredCndts = new ArrayList<>();
            for (MatchedFtr mf2 : link.candidates) {
                if (mf2.f.numRefs < REF_THRSH) {
                    filteredCndts.add(mf2);
                }                
            }
            link.candidates = filteredCndts;            
            if (link.candidates.size() > 0 && link.candidates.size() < REF_THRSH) {
                link.setF2(link.candidates.get(0));
                filteredLinks.add(link);                
            }             
        }
        
        System.out.println("found " + filteredLinks.size() + " links in " + (System.currentTimeMillis() - before) + " ms");
        return filteredLinks;
    }
    
    static double[][] calcWeights()
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
        double[][] stamp1 = buildStamp(gs, x0, y0, stampSize, stampWeights, null);
        
        Feature f = new Feature(x0, y0);
        f.stamp = stamp1;
        f.midValue = midVal[0];
        
        return f;
    }
    
    public static double[][] buildStamp(Greyscale g, int x0, int y0, int stampSize, double[][] stampWeights, double[][] stamp)
    {
        int x1 = x0 - stampSize/2;
        int y1 = y0 - stampSize/2;

        if (stamp == null) {
            stamp = new double[stampSize][stampSize];
        }
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
        
//        System.out.println("weight sum:" + weightSum + ", center:" + center);
        
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
        
        dev = Math.sqrt(dev)/count;
        
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
                    v -= center;
                    stamp[j][i] = v/dev*stampWeights[j][i];
                }
            }
        }
        return stamp;
    }
    
    public interface MinimumCallback {
        void run(double[][] stamp1, double[][] stamp2, int x2, int y2, double a);
    }
    
    public static void findMinimum(Greyscale gs1, int x1, int y1, Greyscale gs2, int x2, int y2, double[][] stampWeights, MinimumCallback callback) {
        System.out.println(" = settling = ");
        double[][] stamp1 = buildStamp(gs1, x1, y1, stampSize, stampWeights, null);
        double[][] stamp2 = null;
        int[][] positions = new int[5][2];
        
        Greyscale stamp2Gs = new Greyscale(2 * stampSize, 2 * stampSize);
        double target2Angle = 0;

        int xt = x2;
        int yt = y2;
        
        int stampMid = stampSize;        
        double lastErr = 10000;
        
        boolean advancing = true;
        
        for (int safety = 20; advancing && safety > 0; safety-- ){
            advancing = false;
            positions[0][0] = xt;
            positions[0][1] = yt;
            positions[1][0] = xt+1;
            positions[1][1] = yt;
            positions[2][0] = xt-1;
            positions[2][1] = yt;
            positions[3][0] = xt;
            positions[3][1] = yt+1;
            positions[4][0] = xt;
            positions[4][1] = yt-1;
            
            double emin = lastErr;
            int winnerIndex = -1;
            
            for (int i=0; i<positions.length; i++) {
                stamp2Gs.drawRotatedStamp(stampMid, stampMid, positions[i][0], positions[i][1], stampSize, target2Angle, gs2);
                stamp2 = ApproachingPerfection.buildStamp(stamp2Gs, stampMid, stampMid, stampSize, stampWeights, stamp2);
                double e = Algebra.compare(stamp1, stamp2);   
                System.out.println("e" + i + ": " + e + " / " + lastErr);
                if (e < emin) {
                    winnerIndex = i;
                    emin = e;
                }                
            }
            
            if (winnerIndex >= 0) {
                xt = positions[winnerIndex][0];
                yt = positions[winnerIndex][1];
                advancing = true;
                lastErr = emin;
                
                if (callback != null) {
                    stamp2Gs.drawRotatedStamp(stampMid, stampMid, xt, yt, stampSize, target2Angle, gs2);
                    stamp2 = ApproachingPerfection.buildStamp(stamp2Gs, stampMid, stampMid, stampSize, stampWeights, stamp2);
                    callback.run(stamp1, stamp2, xt, yt, target2Angle);
                }
            }
            
            double a = target2Angle + Math.PI/80;
            stamp2Gs.drawRotatedStamp(stampMid, stampMid, xt, yt, stampSize, a, gs2);
            stamp2 = ApproachingPerfection.buildStamp(stamp2Gs, stampMid, stampMid, stampSize, stampWeights, stamp2);
            emin = Algebra.compare(stamp1, stamp2);                
            
            double a2 = target2Angle - Math.PI/80;
            stamp2Gs.drawRotatedStamp(stampMid, stampMid, xt, yt, stampSize, a, gs2);
            stamp2 = ApproachingPerfection.buildStamp(stamp2Gs, stampMid, stampMid, stampSize, stampWeights, stamp2);
            double e2 = Algebra.compare(stamp1, stamp2);                
            
            if (e2 < emin) {
                emin = e2;
                a = a2;
            }
            
            if (emin < lastErr) {
                lastErr = emin;
                target2Angle = a;
                advancing = true;
                if (callback != null) {
                    stamp2Gs.drawRotatedStamp(stampMid, stampMid, xt, yt, stampSize, target2Angle, gs2);
                    stamp2 = ApproachingPerfection.buildStamp(stamp2Gs, stampMid, stampMid, stampSize, stampWeights, stamp2);
                    callback.run(stamp1, stamp2, xt, yt, target2Angle);
                }
            }            
        }        
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
                return ((FtrLink)o).mf2.f.x;
            }

            @Override
            public int y1(Object o)
            {
                return ((FtrLink)o).f1.y;
            }

            @Override
            public int y2(Object o)
            {
                return ((FtrLink)o).mf2.f.y;
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
            MatchedFtr f1max = null;
            MatchedFtr f2max = null;
            MatchedFtr f3max = null;
            
            for (MatchedFtr mf1:l1.candidates) {
                for (MatchedFtr mf2:l2.candidates) {
                    for (MatchedFtr mf3:l3.candidates) {
                        faceDescriptor(mf1.f.x, mf1.f.y, mf2.f.x, mf2.f.y, mf3.f.x, mf3.f.y, d2);
                        double _s2 = Algebra.size(d2);
                        if (_s2 == 0) {
                            continue;
                        }
                        
                        double w = Algebra.scalarValue(d1, d2);
                        w /= _s;                    
                        if (w > max) {
                            max = w;
                            f1max = mf1;
                            f2max = mf2;
                            f3max = mf3;
                        }
                    }
                }                
            }
            
            if (max > 0) {
                l1.setF2(f1max);
                l2.setF2(f2max);
                l3.setF2(f3max);
            }   
            
            System.out.println("face " + i + " / " + faces.size());
        }        
    }  
//    void buildProximityGrid()
//    {
//        gridTileSize = (int)(img.getWidth()*proximity);
//        int gridSizeX = img.getWidth()/gridTileSize+1;
//        int gridSizeY = img.getHeight()/gridTileSize+1;
//
//        ArrayList[][] grid = new ArrayList[gridSizeX][gridSizeY];
//        for (int j=0;j<gridSizeY; j++)
//            for (int i=0;i<gridSizeX; i++)
//                grid[i][j] =  new ArrayList();
//        
//        for (Feature f:features)
//        {
//            int i = f.x/gridTileSize;
//            int j = f.y/gridTileSize;
//            grid[i][j].add(f);
//        }
//        
//        proxiGrid = grid;
//    }
//        public ArrayList<FtrLink> findLinks_withProxymity(ApproachingPerfection other)
//    {
//        long before = System.currentTimeMillis();
//        ArrayList<FtrLink> links = new ArrayList<FtrLink>();
//        System.out.println("finding links...");
//        
//        for (Feature f:features)
//        {
//            int gridi=f.x/other.gridTileSize;
//            int gridj=f.y/other.gridTileSize;
//            
//            TreeMap<Double, Feature> localLinks = new TreeMap<>();
//            
//            for (int i=gridi-1; i<gridi+1; i++)
//            {
//                if (i<0) continue;
//                if (i>=other.proxiGrid.length)
//                    break;
//                for (int j=gridj-1;j<gridj+1; j++)
//                {
//                    if (j<0) continue;
//                    if (j>=other.proxiGrid[0].length)
//                        continue;
//                    
//                    if (i==12 || j==12)
//                        System.out.println("kokot");
//                    for (Object o:other.proxiGrid[i][j])
//                    {
//                        Feature f2 = (Feature)o;
//                        double e = Feature.compare(f, f2, stampWeights);
//                        localLinks.put(e, f2);
//                        if (localLinks.size() > 5) {
//                            localLinks.pollLastEntry();
//                        }
//                    }
//                }
//            }
//            
//            FtrLink l = new FtrLink(f, null, 0);  
//            for (Feature _f : localLinks.values()) {
//                l.candidates.add(_f);
//            }
//            links.add(l);
//        }
//        
//        System.out.println("found " + links.size() + " links in " + (System.currentTimeMillis() - before) + " ms");
//       
//        return links;
//        
//    }
}


    
