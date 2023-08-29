/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.ui;

import java.util.ArrayList;
import stereo_praha.Algebra;
import stereo_praha.gui.NewStereoSolver;

/**
 *
 * @author sdsdf
 */
public class SolutionNavigator implements Runnable {

    NewStereoSolver solver;
    ArrayList<double[][]> probes = new ArrayList<>();
   
    int probeCount;
    
    int safety;
    int numOfStepsInWrongDirection;
    
    static int PROBE_COUNT = 10;
    static int MAX_WRONG_STEPS = 100;
    static int MAX_PROBE_STEPS = 1000;
    
    public SolutionNavigator(NewStereoSolver solver) {
        this.solver = solver;
        init();
    }
    
    public void init() {
        this.probeCount = PROBE_COUNT;
        safety = MAX_PROBE_STEPS;
        probes.clear();
        solver.randomize();
    }
    
    public void finalizeProcess() {
        double[][] probe = getSolution();
        if (probe == null) {
            System.out.println("no solution");
            return;
        }
                
        solver.setResultMatrix(probe[0]);
        solver.reconstruct();
    }
    
    public double getError() {
        return solver.getReconstructionError();
    }
    
    public boolean isDone() {
        return (safety <= 0) || (probeCount <= 0);
    }
    
    public double[][] getSolution() {
        if (probes.size() == 0) {
            return null;
        }
        int index = 0;
        
        for (int i=1; i<probes.size(); i++) {
            if (probes.get(index)[1][0] > probes.get(i)[1][0]) {
                index = i;
            }
        }
        
        return probes.get(index);        
    }

    public void donateStepsBudget() {
        safety = MAX_PROBE_STEPS;
    } 
    
    @Override
    public void run() {
        safety--;
        solver.relaxAndReconstruct(false);
        
        double errorChange = solver.getRelativeErrorChange();
        
        if (probeCount > 0) {
            boolean needsNewProbe = safety <= 0;
            
            if (!needsNewProbe) {
                if (errorChange > 0) {
                    if (errorChange < 0.003 || solver.getRelativeLastGoldTravel() < 0.001) {
                        needsNewProbe = true;
                    }
                    numOfStepsInWrongDirection = 0;
                } else {
                    numOfStepsInWrongDirection++;
                    if (numOfStepsInWrongDirection > MAX_WRONG_STEPS) {
                        needsNewProbe = true;
                        numOfStepsInWrongDirection = 0;
                    }
                }            
            }
            if (needsNewProbe) {
                System.out.println("done probe. e : " + solver.getReconstructionError() + ", dE: " + errorChange + ", steps: " + numOfStepsInWrongDirection + ", probes left:" + probeCount);
                System.out.println("            relative travel : " + solver.getRelativeLastGoldTravel());
                
                double[][] probe = new double[][]{solver.getResultMatrix(), new double[]{solver.getReconstructionError()}};                    
                probes.add(probe);                    
                probeCount--;
                safety = MAX_PROBE_STEPS;
                solver.randomize();
            }
        }
    }
};
