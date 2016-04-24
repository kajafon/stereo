package stereo_praha;

import evolve.AbstractAgent;

/**
 * Created by Karol Presovsky on 8/30/14.
 */
public class StereoAgent extends AbstractAgent {


    double angleX = 0;
    double angleY = 0;
    double angleZ = 0;

    TowardsGlory task = null;
    double[] fitness = null;

    public double getAngleX() {
        return angleX;
    }

    public double getAngleY() {
        return angleY;
    }

    public double getAngleZ() {
        return angleZ;
    }

    public StereoAgent()
    {
    }

    public StereoAgent(TowardsGlory task) {
        this.task = task;
    }

    @Override
    public boolean isMoreFitThan(AbstractAgent other) {
        return getFitness() < other.getFitness();
    }

    @Override
    public double getFitness() {
        if (fitness == null)
            calcFitness();
        return fitness[0];
    }

    @Override
    public void calcFitness() {
        fitness = task.calcError(angleX, angleY, angleZ);
    }

    @Override
    public void copyFrom(AbstractAgent source) {
        StereoAgent that = (StereoAgent)source;
        this.angleX = that.angleX;
        this.angleY = that.angleY;
        this.angleZ = that.angleZ;
        this.task = that.task;
    }

    @Override
    public void _mutate(double magnitude) {
        angleX += magnitude * (Math.random() - 0.5);
        angleY += magnitude * (Math.random() - 0.5);
      //  angleZ += magnitude/20 * (Math.random() - 0.5);
    }
}