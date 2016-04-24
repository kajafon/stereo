package stereo_praha.gui;

import stereo_praha.ProblemInterface;

/**
 * Created by Karol Presovsky on 12/6/14.
 */
public class RidgeGraph implements ProblemInterface {

    @Override
    public double[] calcError(double angelX, double angelY, double angelZ) {
        return new double[]{Math.abs(angelX) + angelY*0.1};
    }
}
