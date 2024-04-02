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
    
    Octave octave;
    
    int stepsCount = 4;
    
    public int stepsCount() {
        return stepsCount;
    }

    static class Octave {
        Greyscale[] gaussians;
        Greyscale[] dogs;        
    }
    
    public ApproachingPerfection_sift(BufferedImage img)
    {
        gs = Greyscale.toGreyscale(img);
        
        try {
            octave = createOctave(gs);
        } catch(Exception ex) {
            System.out.println("error:" + ex.getMessage());
        }
    }
    
    public Greyscale[] getStep(int i) {
        if (octave == null) {
            return null;
        }
        
        if (i>=octave.dogs.length) {
            return null;
        }
        
        return new Greyscale[]{ octave.gaussians[i],  octave.gaussians[i+1], octave.dogs[i]};        
    }
    
    
    Octave createOctave(Greyscale src) throws Exception {

        int num = this.stepsCount+1;
        
        Octave oct = new Octave();
        oct.gaussians = new Greyscale[num];
        oct.dogs = new Greyscale[num-1];
        
        double SIGMA = 1.6;
        double K = Math.sqrt(2);
        
        double sigma = SIGMA;
        
        for (int i=0; i<num; i++, sigma *= K) {
            double[][] gauss = Algebra.createGausian(sigma);
            Greyscale gs = new Greyscale(src.width, src.height);            
            Algebra.apply(src, gs, gauss);
            oct.gaussians[i] = gs;
            if (i > 0) {
                Greyscale dog = new Greyscale(src.width, src.height);
                calcDifference(gs, oct.gaussians[i-1], dog);
                oct.dogs[i-1] = dog;
            }            
        }
        
        return oct;        
    }

    static void createHessianMap(Greyscale gs, Greyscale result) {
        double [] vals = new double[gs.px.length];
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        
        for (int j=1; j < gs.height-1; j++) {
            for (int i=1; i < gs.width-1; i++) {
                double v = Math.abs(gs.getHessianTest(i, j));
                if (v < min) {
                    min = v;
                }
                if (v>max){
                    max = v;
                }
                vals[j*gs.width + i] = v;
            }            
        }
        double scale = max - min;
        if (scale == 0) {
            return;
        }
        
        for (int i=0; i<vals.length; i++) {
            result.px[i] = (short)(Math.min(255, (vals[i] - min)/scale*255));
        }        
    }
    
    static Greyscale calcDifference(Greyscale _gs1, Greyscale _gs2, Greyscale result) {
        
        if (result == null) {
            result = new Greyscale(_gs1.width, _gs2.height);
        }
        int[] dif = new int[_gs1.px.length];
        int min = 10000000;
        for (int i=0; i<_gs2.px.length; i++) {
            int v = _gs2.px[i] - _gs1.px[i];
            if (v < min) {
                min = v;
            }
            dif[i] = v;            
        }
        for (int i=0; i<_gs1.px.length; i++) {
            dif[i] -= min;
            result.px[i] = (short)(dif[i] * 2);
        }
        return result;
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
           
    
}


