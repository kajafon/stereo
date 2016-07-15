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
public class TaskAgent extends AbstractAgent {

    double movex = 0;
    double movey = 0;
    double movez = 0;

    double ax = 0;
    double ay = 0;
    double focalLength = 10;

    Double fitness = null;
    StereoTask garage = null;

    public TaskAgent() {
    }

    public TaskAgent(StereoTask garage) {
        this.garage = garage;
    }
            
    
    
    @Override
    public double getFitness() {
        return fitness;
    }

    @Override
    public void calcFitness() {
        garage.angleX = ax;
        garage.angleY = ay;
        garage.moveX = movex;
        garage.moveY = movey;
        garage.moveZ = movez;
        garage.focalLength = focalLength;
   
        garage.applySolution();  

        
        fitness = garage.goldError;

    }

    @Override
    public void copyFrom(AbstractAgent source) {
        TaskAgent other = (TaskAgent)source;
        movex = other.movex;
        movey = other.movey;
        movez = other.movez;

        ax = other.ax;
        ay = other.ay;
        focalLength = other.focalLength;
        garage = other.garage;
    }

    @Override
    public void _mutate(double magnitude) {
        movex += Math.random()*magnitude - magnitude/2;
        movey += Math.random()*magnitude - magnitude/2;
        movez += Math.random()*magnitude - magnitude/2;
        focalLength += Math.random()*magnitude - magnitude/2;
        magnitude /= 10;
        ax += Math.random()*magnitude - magnitude/2;
        ax += Math.random()*magnitude - magnitude/2;
    }

    @Override
    public boolean isMoreFitThan(AbstractAgent other) {
        return getFitness() < other.getFitness();
    }
}


