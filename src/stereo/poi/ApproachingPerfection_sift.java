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
public class ApproachingPerfection_sift
{
    BufferedImage img;
    BufferedImage img2;
    BufferedImage img3;
    ArrayList<int[]> maxims;
    
    public Greyscale gs;
    Greyscale gs2;
    Greyscale gs3;    
    Greyscale gs4;
    Greyscale gs5;
    ArrayList[][] proxiGrid;
    double proximity = 0.3;
    int gridTileSize;
    double SIGMA = 1.6;
    double K = Math.sqrt(2);
    Octave octave;
    
    int stepsCount = 6;
    
    public int stepsCount() {
        return stepsCount;
    }

    static class Octave {
        Greyscale[] gaussians;
        int[][] dogs;        
        ArrayList<SiftStamp>[] ipoints;
    }
    
    public ApproachingPerfection_sift(BufferedImage img)
    {
        gs = Greyscale.toGreyscale(img);
        
        try {
            octave = createOctave(gs);
        } catch(Exception ex) {
            System.out.println("error:" + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public class Step {
        public Greyscale gs1;
        public Greyscale gs2;
        public int[] dif;        
        public Step(Greyscale gs1, Greyscale gs2, int[] dif) {
            this.gs1 = gs1;
            this.gs2 = gs2;
            this.dif = dif;
        }
    }
    
    public ArrayList<SiftStamp> getMaxims(int step){
        if (octave == null) {
            return null;
        }
        
        if (step>=octave.ipoints.length) {
            return null;
        }        
        
        return octave.ipoints[step];
    }
    
    public Greyscale[] getStep(int i) {
        if (octave == null) {
            return null;
        }
        
        if (i>=octave.dogs.length) {
            return null;
        }
        
        int[] dog = octave.dogs[i];
        Greyscale dog_gs = new Greyscale(octave.gaussians[0].width, octave.gaussians[0].height);
//        int max = -1000000;
//        int min =  1000000;
//        for (int k=0; k<dog.length; k++) {

//        }
//        double scale = 100;
        int max = -1000000;
        int min =  1000000;
        int sum = 0;
        for (int k=0; k<dog.length; k++) {
//            if (dog[k] < min) {
//                min = dog[k];
//            }
//            if (dog[k] > max) {
//                max = dog[k];
//            }
            int v = (dog[k] + 10) * 2;
//            if (v > 255) v = 255;
//            if (v < 0) v = 0;
            sum += v;
            if (v < min) {
                min = v;
            }
            if (v > max) {
                max = v;
            }
            dog_gs.px[k] = (short)v;        
        }
        System.out.println("min: " + min + " max: " + max + " mid:" + ((double)sum/dog_gs.px.length));
        return new Greyscale[]{octave.gaussians[i],  octave.gaussians[i+1], dog_gs};        
    }
    
    Octave createOctave(Greyscale src) throws Exception {

        int num = this.stepsCount;
        
        Octave oct = new Octave();
        oct.gaussians = new Greyscale[num];
        oct.dogs = new int[num-1][];
//        ArrayList<SiftStamp>[] _t = ;
        oct.ipoints = new ArrayList[num-3];
        
        double sigma = SIGMA;
        
        for (int i=0; i<num; i++, sigma *= K) {
            double[][] gauss = Algebra.createGausian(sigma);
            Greyscale gs = new Greyscale(src.width, src.height);            
            Algebra.apply(src, gs, gauss);
            oct.gaussians[i] = gs;
            if (i > 0) {
                int[] dog = calcDifference(gs, oct.gaussians[i-1]);
                oct.dogs[i-1] = dog;
            }            
        }
        
        for (int i=0; i<num-3; i++) {
            findInterestPoints(oct, i);
        }
        return oct;        
    }
    
    void findInterestPoints(Octave octave, int step) {
        if (step < 0 || step > octave.dogs.length-3) {
            return;
        }
        
        int[] dog1 = octave.dogs[step];
        int[] dog2 = octave.dogs[step+1];
        int[] dog3 = octave.dogs[step+2];

        double size = SIGMA*Math.pow(K, step + 1.5);
        
        ArrayList<SiftStamp> maxims = localMaxims(null, octave.gaussians[step+1], dog1, dog2, dog3, octave.gaussians[0].width, octave.gaussians[0].height, 5, size);        
        octave.ipoints[step] = maxims;
    }
    
    public static ArrayList<SiftStamp> localMaxims(ArrayList<SiftStamp> list, Greyscale gs, int[] dogLower, int[] dogMid, int[] dogUpper, int width, int height, int thrsh, double size)
    {
        if (list == null)
            list = new ArrayList<SiftStamp>();
        
        for (int j = 10; j < height-10; j++)
        {
            int y_adr = j *  width;
            for (int i = 10; i < width-10; i++)
            {
                double h = Algebra.hessianTest(i, j, dogMid, width, height);
                if (h > 14) {
                    continue;
                }
                
                int adr = y_adr + i;
                
                if (Math.abs(dogMid[adr]) < thrsh) continue; 
                boolean isMax = evalMaxim(i, j, dogLower, dogMid, dogUpper, width, height, thrsh);
                if (isMax)
                {            
                    SiftStamp stamp = new SiftStamp(i, j, 0);
                    buildSiftStamp(stamp, gs, i, j, size);
                    list.add(stamp); 
                } 
            }
        }
        return list;
    }
    
    static boolean evalMaxim(int x, int y, int[] dogLower, int[] dogMid, int[] dogUpper, int width, int height, int thrsh) {
        int adr = x + y * width;
        
        boolean r1 = 
            dogMid[adr] > dogMid[adr-1      ] &&
            dogMid[adr] > dogMid[adr-1-width] &&
            dogMid[adr] > dogMid[adr  -width] &&
            dogMid[adr] > dogMid[adr+1-width] &&
            dogMid[adr] > dogMid[adr+1      ] &&
            dogMid[adr] > dogMid[adr+1+width] &&
            dogMid[adr] > dogMid[adr  +width] &&
            dogMid[adr] > dogMid[adr-1+width] &&

            dogMid[adr] > dogUpper[adr        ] &&                        
            dogMid[adr] > dogUpper[adr-1      ] &&
            dogMid[adr] > dogUpper[adr-1-width] &&
            dogMid[adr] > dogUpper[adr  -width] &&
            dogMid[adr] > dogUpper[adr+1-width] &&
            dogMid[adr] > dogUpper[adr+1      ] &&
            dogMid[adr] > dogUpper[adr+1+width] &&
            dogMid[adr] > dogUpper[adr  +width] &&
            dogMid[adr] > dogUpper[adr-1+width] &&

            dogMid[adr] > dogLower[adr        ] &&                        
            dogMid[adr] > dogLower[adr-1      ] &&
            dogMid[adr] > dogLower[adr-1-width] &&
            dogMid[adr] > dogLower[adr  -width] &&
            dogMid[adr] > dogLower[adr+1-width] &&
            dogMid[adr] > dogLower[adr+1      ] &&
            dogMid[adr] > dogLower[adr+1+width] &&
            dogMid[adr] > dogLower[adr  +width] &&
            dogMid[adr] > dogLower[adr-1+width];
        
        
        boolean r2 = 
            dogMid[adr] < dogMid[adr-1      ] &&
            dogMid[adr] < dogMid[adr-1-width] &&
            dogMid[adr] < dogMid[adr  -width] &&
            dogMid[adr] < dogMid[adr+1-width] &&
            dogMid[adr] < dogMid[adr+1      ] &&
            dogMid[adr] < dogMid[adr+1+width] &&
            dogMid[adr] < dogMid[adr  +width] &&
            dogMid[adr] < dogMid[adr-1+width] &&

            dogMid[adr] < dogUpper[adr        ] &&                        
            dogMid[adr] < dogUpper[adr-1      ] &&
            dogMid[adr] < dogUpper[adr-1-width] &&
            dogMid[adr] < dogUpper[adr  -width] &&
            dogMid[adr] < dogUpper[adr+1-width] &&
            dogMid[adr] < dogUpper[adr+1      ] &&
            dogMid[adr] < dogUpper[adr+1+width] &&
            dogMid[adr] < dogUpper[adr  +width] &&
            dogMid[adr] < dogUpper[adr-1+width] &&

            dogMid[adr] < dogLower[adr        ] &&                        
            dogMid[adr] < dogLower[adr-1      ] &&
            dogMid[adr] < dogLower[adr-1-width] &&
            dogMid[adr] < dogLower[adr  -width] &&
            dogMid[adr] < dogLower[adr+1-width] &&
            dogMid[adr] < dogLower[adr+1      ] &&
            dogMid[adr] < dogLower[adr+1+width] &&
            dogMid[adr] < dogLower[adr  +width] &&
            dogMid[adr] < dogLower[adr-1+width];
        
        return r1 || r2;
    } 

//    static void createHessianMap(Greyscale gs, Greyscale result) {
//        double [] vals = new double[gs.px.length];
//        double min = Double.MAX_VALUE;
//        double max = -Double.MAX_VALUE;
//        
//        for (int j=1; j < gs.height-1; j++) {
//            for (int i=1; i < gs.width-1; i++) {
//                double v = Math.abs(gs.getHessianTest(i, j));
//                if (v < min) {
//                    min = v;
//                }
//                if (v>max){
//                    max = v;
//                }
//                vals[j*gs.width + i] = v;
//            }            
//        }
//        double scale = max - min;
//        if (scale == 0) {
//            return;
//        }
//        
//        for (int i=0; i<vals.length; i++) {
//            result.px[i] = (short)(Math.min(255, (vals[i] - min)/scale*255));
//        }        
//    }
    
    static int[] calcDifference(Greyscale _gs1, Greyscale _gs2) {        
       
        int[] dif = new int[_gs1.px.length];
        int max = -10000;
        int min = 10000;
        for (int i=0; i<_gs2.px.length; i++) {
            int v = _gs2.px[i] - _gs1.px[i];
            dif[i] = v;            
            v = Math.abs(v);
            if (v < min) min = v;
            if (v > max) max = v;
        }
        
        System.out.println("dog abs min:" + min + " abs max:" + max);
        return dif;
    }
    
    public ArrayList<int[]> getMaxims() {
        return maxims;
    }
    
    public BufferedImage getImg()
    {
        return img;
    }

    public BufferedImage getImg2()
    {
        return img2;
    }

    public BufferedImage getImg3()
    {
        return img3;
    }
    
    public static void buildSiftStamp(SiftStamp stamp, Greyscale gs, int xs, int ys, double scale) {
        if (xs < 9*scale || xs > gs.width - (9*scale) || ys < (9*scale) || ys > gs.height - (9*scale)) {
            return;
        }
        double[] mainHistogram = new double[8];
        double[][] quadHisto = new double[16][8];
        int[] resultHisto = new int[16*8];
        
        for (int j=0; j<16; j++) {
            int _yn = (int)(scale*(j-8-1)) + ys;
            int  y  = (int)(scale*(j-8  )) + ys;
            int _ys = (int)(scale*(j-8+1)) + ys;
            
            for (int i=0; i<16; i++) {
                int _xw = (int)(scale*(i-8-1)) + xs;
                int   x = (int)(scale*(i-8  )) + xs;
                int _xe = (int)(scale*(i-8+1)) + xs;
                
                int n = gs.px[_yn * gs.width +  x];
                int s = gs.px[_ys * gs.width +  x];
                int w = gs.px[ y  * gs.width + _xw];
                int e = gs.px[ y  * gs.width + _xe];
                
                int dx = e - w;
                int dy = s - n;
                
                double mag = Math.sqrt(dx*dx + dy*dy);
                double a = Math.atan2(dy, dx);                
                double na = (a + Math.PI) / (2*Math.PI);
                int mainI = (int)(na*(mainHistogram.length-1));
                mainHistogram[mainI] += mag;
                int hy = j/4;
                int hx = i/4;
                int hi = hy*4 + hx;
                double[] qh = quadHisto[hi];                
                int qhi = (int)(na*(qh.length-1));
                qh[qhi] += mag;                           
            }            
        }
        double max = 0;
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
        int di = (int)((double)mi / (mainHistogram.length-1) * (quadHisto[0].length-1));
//        double mainA = (double)mi / (mainHistogram.length-1) *2*Math.PI - Math.PI;
        
        for (int j=0; j<quadHisto.length; j++) {
            for (int i=0; i<quadHisto[j].length; i++) {
                int ri = i - di;
                if (ri < 0) {
                    ri = quadHisto[j].length + ri;
                }
                
                int rhi = j * 8 + ri;
                if (rhi >= resultHisto.length) {
                    System.out.println("kokot");
                }
                resultHisto[rhi] = (int)quadHisto[j][i];                
            }            
        }
        double a = (double)mi/(mainHistogram.length-1)*2*Math.PI - Math.PI;
        stamp.vector = resultHisto;
        stamp.angle = a;
    }   
    
}


