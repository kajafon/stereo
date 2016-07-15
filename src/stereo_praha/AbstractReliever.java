package stereo_praha;

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
        for (int i= track.length-2; i>=0; i--)
        {
            System.arraycopy(track[i],0, track[i+1],0, track[i].length);
        }

        System.arraycopy(x,0, track[0],0, x.length);

    }

    boolean zippDetected;
    
    public void halfStepSize()
    {
        stepSize /= 2;
    }

    public boolean isZipp()
    {
        return stepsCount > 4 &&
               Algebra.simpleDistance(track[0], track[1]) > Algebra.simpleDistance(track[0], track[2]) &&
               Algebra.simpleDistance(track[1], track[2]) > Algebra.simpleDistance(track[1], track[3]);
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

    public abstract double getTension(double[] x);

    public double relax()
    {

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
        
        pushX();

        return val;
    }

    private void improveZip() {
        pushX();

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

        // y = x * g
        // x = y / g


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

}
