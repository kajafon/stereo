package evolve;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Karol Presovsky on 8/29/14.
 */
public class Reactor {

    ArrayList<AbstractAgent> population = new ArrayList<>();
    double fitnessSum;
    double mutationStrength;
    double newBornRatio;
    AbstractAgent localBoss;

    public double getMutationStrength() {
        return mutationStrength;
    }

    public void setMutationStrength(double mutationStrength) {
        this.mutationStrength = mutationStrength;
    }
    
    

    public AbstractAgent getLocalBoss() {
        return localBoss;
    }

    public void initPopulation(AbstractAgent etalon, int count, double initialDispersion, double mutationStrength, double newBornRatio) {
        if (newBornRatio > 1.0 || newBornRatio < 0) {
            System.out.println("newborn ratio invalid: " + newBornRatio + ", setting 0.5");
            newBornRatio = 0.5;
        }
        this.newBornRatio = newBornRatio;
        this.mutationStrength = mutationStrength;
        population.clear();

        while (count > 0) {
            AbstractAgent a = etalon.getCopy();
            a.mutate(initialDispersion);
            a.calcFitness();
            population.add(a);
            count--;
        }

        evaluatePopulation();

    }
/*
    public void prepareRoulette()
    {
        Iterator<AbstractAgent> it = population.iterator();

        fitnessSum = 0;

        while (it.hasNext())
        {
            AbstractAgent agent = it.next();
            fitnessSum += agent.getFitness();
        }
    }


    public AbstractAgent roulette()
    {
        double target = Math.random() * fitnessSum;
        AbstractAgent winner = null;
        Iterator it = population.iterator();

        while (it.hasNext())
        {
            winner = (AbstractAgent) it.next();
            target -= winner.getFitness();
            if (target <= 0)
                break;
        }

        return winner;
    }
*/

    public AbstractAgent evaluatePopulation()
    {

        AbstractAgent superAgent = null;
        Iterator it = population.iterator();
        while (it.hasNext())
        {
            AbstractAgent agent = (AbstractAgent) it.next();
            agent.calcFitness();

            if (superAgent == null || agent.isMoreFitThan(superAgent))
                superAgent = agent;
        }

        return superAgent;
    }

    public AbstractAgent tournament(int rounds)
    {
        if (rounds < 2) return null;

        AbstractAgent a = null;
        for (int i = 0; i < rounds; i++)
        {
            int index = (int) (Math.random() * population.size());
            AbstractAgent a2 = population.get(index);
            if (a == null || (a2.isMoreFitThan(a)))
                a = a2;
        }
        return a;
    }

    public void moveToNextGen()
    {

        int bodyCount = population.size();
        int surviveCount = (int)(bodyCount*(1.0-newBornRatio));
        ArrayList<AbstractAgent> parents = new ArrayList<AbstractAgent>();

        for (int i=0; i<surviveCount; i++) {
            //AbstractAgent a = tournament(4);
            AbstractAgent a = tournament(4);
            population.remove(a);
            parents.add(a);
        }

        population.clear();

        while (parents.size() + population.size() < bodyCount) {
            int indx = (int)(Math.random()*parents.size());
            AbstractAgent parent = parents.get(indx);
            AbstractAgent newborn = parent.getCopy();
            newborn.mutate(mutationStrength);
            newborn.calcFitness();
            population.add(newborn);
        }

        population.addAll(parents);
    }

    public AbstractAgent iterate()
    {
        moveToNextGen();
        return localBoss = evaluatePopulation();

    }

    public static void main(String[] args) {
        Reactor reactor = new Reactor();

        class TestAgent extends AbstractAgent {
            double x = 10;
            Double fitness = null;

            @Override
            public double getFitness() {
                calcFitness();
                return fitness;
            }

            @Override
            public void calcFitness() {
                fitness = Math.abs(x);
            }

            @Override
            public void copyFrom(AbstractAgent source) {
                this.x = ((TestAgent)source).x;
            }

            @Override
            public void _mutate(double magnitude) {
                x += Math.random()*magnitude - magnitude/2;
            }

            @Override
            public boolean isMoreFitThan(AbstractAgent other) {
                return getFitness() < other.getFitness();
            }
        }
        reactor.initPopulation(new TestAgent(), 10, 10, 3.0, 0.5);

        AbstractAgent boss = null;
        for (int i=0; i<100; i++) {
            boss = reactor.iterate();
            System.out.println("-x:" + ((TestAgent)boss).x + ", fitness:" + boss.getFitness());
        }

    }



}
