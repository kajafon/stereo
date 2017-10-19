package stereo_praha;

import java.util.ArrayList;
import javax.swing.JPanel;
import stereo_praha.gui.GraphPanel;

/**
 * Created by Karol Presovsky on 8/30/14.
 */
public abstract class AbstractReliever {

    double[] target;

    double[] x;
    double[] _tmp_x;


    double stepSize;
    double[] gradient;
    double val;

    public double minVal;
    public double[] minX;
    int stepsCount;

    double[][] track;
    
    double [][] errorToStepFnc;
    
    ArrayList<double[]> path = new ArrayList<>();
    
    boolean isQuitOnZip = false;
    boolean isImproveZip = true;

    public void setIsQuitOnZip(boolean isQuitOnZip) {
        this.isQuitOnZip = isQuitOnZip;
    }

    public void setIsImproveZip(boolean isImproveZip) {
        this.isImproveZip = isImproveZip;
    }
    
    public void clearPath()
    {
        path.clear();
    }
    
    public double[][] getPath()
    {
        double[][] out = new double[path.size()][];
        for (int i=0; i<path.size(); i++) {
            double[] entry = path.get(i);
            out[i] = new double[entry.length];
            System.arraycopy(entry, 0, out[i], 0, 3);
        }
        return out;
    }
    
    public void addToPath(double e) {
        double[] step = new double[x.length + 1];
        System.arraycopy(x, 0, step, 0, x.length);
        step[x.length] = e;
        path.add(step);
    }

    public double getVal() {
        return val;
    }

    protected AbstractReliever(double[] x, double stepSize) {
        this.x = new double[x.length];
        this._tmp_x = new double[x.length];
        this.minX = new double[x.length];
        for(int i=0; i<x.length; i++) this.x[i] = x[i];
        this.stepSize = stepSize;
        gradient = new double[x.length];
        track = new double[4][x.length];
    }

    private void pushX()
    {
        if (!pushed) {
            for (int i= track.length-2; i>=0; i--)
            {
                System.arraycopy(track[i],0, track[i+1],0, track[i].length);
            }

            System.arraycopy(x,0, track[0],0, x.length);
            pushed = true;
        }
    }
    
    boolean pushed;
    boolean zippDetected;
    
    public void halfStepSize()
    {
        stepSize /= 2;
    }

    public boolean isZipp()
    {
        pushX();
        return stepsCount > 4 &&
               Algebra.simpleDistance(track[0], track[1]) > Algebra.simpleDistance(track[0], track[2]) &&
               Algebra.simpleDistance(track[1], track[2]) > Algebra.simpleDistance(track[1], track[3]);
    }
    
    private void improveZip() {
        boolean zip = isZipp();
        if (!zippDetected && zip)
        {
            zippDetected = zip;
         //  System.out.println("zipp detected. " + stepsCount);
        } else if (zippDetected && !zip)
        {
            zippDetected = zip;
            //System.out.println("zipp lost" + stepsCount);
        }

        if (zip) {
            double[] v1 = new double[x.length];
            double[] v2 = new double[x.length];
            double n1 = 0;
            double n2 = 0;

            for (int i=0; i<x.length; i++)
            {
                v1[i] = track[0][i] - track[2][i];
                v2[i] = track[0][i] - track[1][i];

                n1 += v1[i]*v1[i];
                n2 += v2[i]*v2[i];
            }

            if (n1 < n2*0.7 && n1 > 0) {
                n1 = Math.sqrt(n1);
                n2 = Math.sqrt(n2);
                n1 = n2/n1*0.7;
                for (int i=0; i<x.length; i++)
                {
                    x[i] = track[2][i] + v1[i]*n1;
                }
            }

        }
    }

    public void setZipAverage()
    {
        Aggregator z = new Aggregator(x.length);
        for (double[] v : track) {
            z.add(v);
        }        
        x = z.getAverage();
    }

    public void setTarget(double[] target) {
        this.target = new double[target.length];
        System.arraycopy(target, 0, this.target, 0, target.length);
    }

    public void init(double[] x, double stepSize) {
        System.arraycopy(x, 0, this.x, 0, x.length);
        this.stepSize = stepSize;
        init();
    }

    public void init() {
        val = getTension(x);
        minVal = Double.MAX_VALUE;
        stepsCount = 0;
    }

    public double[] getX() {
        return x;
    }

    public double getX(int i) {
        return x[i];
    }

    public void setX(double[] x) {
        System.arraycopy(x, 0, this.x, 0, this.x.length);
    }

    public void setStepSize(double stepSize) {
        this.stepSize = stepSize;
    }
    
    public abstract double getTension(double[] x);
    
    public void setErrorToStepFnc(double[][] fnc)
    {
        errorToStepFnc = fnc;        
    }
    
    public void setStepSizeFromError(double error)
    {
        if (errorToStepFnc != null) {
            stepSize = f(error, errorToStepFnc);
            if (stepSize < 0.01)
                stepSize = 0.01;
            System.out.println("step size: " + stepSize);
        }        
    }
    
    public static double f(double in, double[][] fnc)
    {
        int i = 0;
        
        while (i < fnc.length && fnc[i][0] < in)
            i++;
        
        if (i >= fnc.length - 1)
            return fnc[fnc.length-1][1];
        
        double d = fnc[i][1];
        double x = in - fnc[i][0];
        double k = (fnc[i+1][1] - fnc[i][1]) 
            / (fnc[i+1][0] - fnc[i][0]);
        
        double v = d + x * k; 
        if (Double.isNaN(v) || Double.isInfinite(v))
            System.out.println("pici!");
        
        return v;
    }
    
    public double getTension()
    {
        return getTension(x);        
    }

    public double relax()
    {
        pushed = false;
        double _stepSize = stepSize*Math.exp(-0.01*stepsCount);
        for (int i=0; i<x.length; i++)
            _tmp_x[i] = x[i];

        double probeStep = _stepSize / 100;

        for (int i=0; i<x.length; i++) {

            _tmp_x[i] = x[i] - probeStep;
            double t1 = getTension(_tmp_x);

            _tmp_x[i] = x[i] + probeStep;
            double t2 = getTension(_tmp_x);

            gradient[i] = (t2 - t1)/probeStep;

            _tmp_x[i] = x[i]; // restore value
        }

        double sum = 0;
        for (int i=0; i<x.length; i++) {
            sum += gradient[i]*gradient[i];
        }
        sum = Math.sqrt(sum);
        for (int i=0; i<x.length; i++) {
            gradient[i] *= _stepSize/sum;
            x[i] -= gradient[i];
        }

        val = getTension(x);
        
        if (val == Double.POSITIVE_INFINITY || val == Double.NEGATIVE_INFINITY ||
            val == Double.NaN)
        {
            for (int i=0; i<x.length; i++)
                x[i] = _tmp_x[i];
            val = getTension(x);
            return Double.NaN;
        }

        if (minVal < val) {
            minVal = val;
            System.arraycopy(x, 0, minX, 0, x.length);
        }

        stepsCount++;
        
        return val;
    }
    
    public static AbstractReliever relax_routine(AbstractReliever reliever, JPanel panel, GraphPanel graphPanel)
    {
        ArrayList<Double> err_series = new ArrayList<>();

        int i;
        
        graphPanel.clearGraphs();
        graphPanel.clearMarks();
        reliever.clearPath();

        err_series.add(reliever.getTension());
        Aggregator agr_err = new Aggregator(1);
        
        for (i=0; i<50; i++) 
        {
            double error = reliever.relax();
            reliever.addToPath(error);
            agr_err.add(error);
            if (Double.isNaN(error)) {
                System.out.println("error is NaN. quitting");
                break;
            }
            panel.repaint();
            reliever.setStepSizeFromError(error);
            err_series.add(reliever.stepSize);
            if (reliever.isQuitOnZip && reliever.isZipp()) {
                System.out.println("reliever: zip detected. quitting.");
                break;
            }
            if (reliever.isImproveZip) {
                reliever.improveZip();
            }
        }      
        
        graphPanel.addGraph(err_series, "E");
        
        panel.repaint();
        graphPanel.repaint();
        
        return reliever;
    }

    public double newton()
    {

        for (int i=0; i<x.length; i++)
            _tmp_x[i] = x[i];

        for (int i=0; i<x.length; i++) {

            _tmp_x[i] = x[i] - stepSize;
            double t1 = getTension(_tmp_x);

            _tmp_x[i] = x[i] + stepSize;
            double t2 = getTension(_tmp_x);

            gradient[i] = t2 - t1;

            _tmp_x[i] = x[i]; // restore value

        }

        for (int i=0; i<x.length; i++) {
            double k = gradient[i]/(stepSize *2);
            x[i] -= val / k;
        }

        val = getTension(x);

        return val;

    }

    public void heal(double health) {
        int i;
        for (i=0; i<100; i++) {
            if (health > relax()) {
                break;
            }
        }
        System.out.println("heald in " + i + ", " + val);
    }
 
//    public static void main(String[] args) {
//        double [][] fnc = new double[][]{
//           {0.001, 0.1},
//           {0.01, 0.5},
//           {0.1, 1},
//           {1.0, 1.5},
//           {2.0, 2.0}
//        };
//        
//        double e = 3;
//        System.out.println("-" + e + " -> " + getStep(e, fnc));
//        e = 1;
//        System.out.println("-" + e + " -> " + getStep(e, fnc));
//        e = 0.5;
//        System.out.println("-" + e + " -> " + getStep(e, fnc));
//        e = 0.4;
//        System.out.println("-" + e + " -> " + getStep(e, fnc));
//        e = 0.051;
//        System.out.println("-" + e + " -> " + getStep(e, fnc));
//        e = 0.0171;
//        System.out.println("-" + e + " -> " + getStep(e, fnc));
//        e = 0.0001;
//        System.out.println("-" + e + " -> " + getStep(e, fnc));
//        
//    }
 
}
