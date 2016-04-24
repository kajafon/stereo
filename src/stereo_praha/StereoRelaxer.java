package stereo_praha;

/**
 * Created by Karol Presovsky on 8/30/14.
 */
public class StereoRelaxer extends AbstractReliever {

    TowardsGlory problem;

    public StereoRelaxer(TowardsGlory problem, double stepSize) {
        super(new double[]{0,0,0}, stepSize);
        this.problem = problem;
        init();
    }

    @Override
    public double getTension(double[] x) {
        return problem.calcError(x[0], x[1], x[2])[0];
    }
}
