package evolve;

/**
 * Created by Karol Presovsky on 8/29/14.
 */
public abstract class AbstractAgent {
    public abstract double getFitness();
    public abstract void calcFitness();
    public abstract void copyFrom(AbstractAgent source);
    public abstract void _mutate(double magnitude);
    public abstract boolean isMoreFitThan(AbstractAgent other);

    public AbstractAgent mutate(double magnitude) {
        _mutate(magnitude);
        return this;
    }

    public AbstractAgent getCopy() {
        try {
            AbstractAgent newOne = this.getClass().newInstance();
            newOne.copyFrom(this);
            return newOne;
        } catch(Exception e) {
            System.out.println(e);
            return null;
        }
    }

}
