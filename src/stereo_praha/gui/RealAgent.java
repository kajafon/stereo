/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo_praha.gui;

import evolve.AbstractAgent;

/**
 *
 * @author karol presovsky
 */
public class RealAgent extends AbstractAgent {

    double[] x;

    Double fitness = null;
    StereoTask garage = null;

    public RealAgent() {
        x = new double[6];
    }

    public RealAgent(StereoTask garage) {
        this.garage = garage;
        x = garage.getVector();
    }

    @Override
    public double getFitness() {
        return fitness;
    }

    @Override
    public void calcFitness() {
   
        garage.setVector(x);
        garage.applySolution();  
        fitness = garage.goldError;

    }

    @Override
    public void copyFrom(AbstractAgent source) {
        RealAgent other = (RealAgent)source;
        garage = other.garage;
        
        if (x == null)
        {
            x = new double[6];
        }
        
        System.arraycopy(other.x, 0, x, 0, 6);
    }

    @Override
    public void _mutate(double magnitude) {
        for (int i=0; i<x.length; i++) {
           x[i] += Math.random()*magnitude - magnitude/2;
        }
    }

    @Override
    public boolean isMoreFitThan(AbstractAgent other) {
        return getFitness() < other.getFitness();
    }
}


