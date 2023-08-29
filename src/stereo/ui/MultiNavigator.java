/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import stereo_praha.Algebra;
import stereo_praha.gui.NewStereoSolver;

/**
 *
 * @author sdsdf
 */
public class MultiNavigator implements Runnable{
    ArrayList<NewStereoSolver> solvers = new ArrayList<>();  
    ArrayList<SolutionNavigator> navigators = new ArrayList<>();  
    int MAX_STEPS_PER_SOLVER = 1000;
    int MIN_STEPS_PER_SOLVER = 100;
    
    public double[] travelSeries = new double[30];    
    
    void addTravelValue(double val) {
        for (int i=1; i<travelSeries.length; i++) {
            travelSeries[i-1] = travelSeries[i];
        }
        travelSeries[travelSeries.length-1] = val;
    } 
    
    ArrayList<Runnable> repaintListeners = new ArrayList<>();
    
    public void addRepaintListener(Runnable l) {
       repaintListeners.add(l); 
    }
    
    public void notifyRepaint() {
        for (Runnable l : repaintListeners) {
            l.run();
        }
    }
    public MultiNavigator() {
        setPhase("probing");
    }
    
    public void add(NewStereoSolver solver) {
        if (solvers.contains(solver)) {
            System.out.println("!!!! this solver already in set");
            return;
        }
        
        solvers.add(solver);        
    }

    String phase;
    boolean phaseIsInitialized = false;
    
    void setPhase(String name) {
        phase = name;
        phaseIsInitialized = false;
    }
    
    NewStereoSolver currentSolver;
    int currentStepsLeft;
    
    int currentSolverIndex;
    double distancePerRound;
    double[] lastPosition = new double[3];
    
    int raundsCount = 10;
    boolean isDone = false;
    
    public Runnable multiRelaxCallback;
    
    public boolean isDone() {
        return isDone;
    }
    
    
    @Override
    public void run() {
        if (phase == null) {
            setPhase("probing");
        }
        if (phase.equals("probing")) {
            
            /* init in place when needed */
            if (!phaseIsInitialized) {
                System.out.println("---- PROBING ------");
                
                phaseIsInitialized = true;
                navigators.clear();
                Iterator it = solvers.iterator();
                while (it.hasNext()) {
                    navigators.add(new SolutionNavigator((NewStereoSolver)it.next()));
                }
            }
            
            int doneCount = 0;            
            for (SolutionNavigator n : navigators) {
                n.run();
                n.solver.notifyRepaint();
                if (n.isDone()) {
                    doneCount++;
                }
            }            
            
            if (doneCount == navigators.size()) {
                setPhase("multirelax");
                for (SolutionNavigator n : navigators) {
                    n.finalizeProcess();
                }
            }
        } else if ("multirelax".equals(phase)) {
            
            if (!phaseIsInitialized) {
                System.out.println("---- MULTI RELAX ------");
                phaseIsInitialized = true;  
                currentSolver = null;
                currentSolverIndex = -1;   
                if (multiRelaxCallback != null) {
                    multiRelaxCallback.run();
                }
            }            
            
            if (currentSolver == null) {
                currentSolverIndex++;
                int newIndex = currentSolverIndex % solvers.size();
                if (newIndex < currentSolverIndex) {
                    distancePerRound = 0;
                    raundsCount--;
                    if (raundsCount <= 0) {
                        isDone = true;
                    }                    
                }
                
                currentSolverIndex = newIndex;
                currentSolver = solvers.get(currentSolverIndex);
                currentStepsLeft = MAX_STEPS_PER_SOLVER;
                Algebra.copy(Algebra.getPositionBase(currentSolver.getResultMatrix(), null), lastPosition);
            }
            
            currentSolver.relaxAndReconstruct(true);
            currentSolver.notifyRepaint();
            
            distancePerRound += currentSolver.getRelativeLastGoldTravel();
            currentStepsLeft--;

            if (currentStepsLeft <= 0) {
                currentSolver = null;
            }
            
            if (currentStepsLeft < (MAX_STEPS_PER_SOLVER - MIN_STEPS_PER_SOLVER) 
                    && (currentSolver.getRelativeLastGoldTravel() < 0.0001 || currentSolver.getRelativeErrorChange() < 0.003)) {
                
                System.out.println("---- round travel: " + distancePerRound + ", rel travel: " + currentSolver.getRelativeLastGoldTravel() + ", rel dE: " + currentSolver.getRelativeErrorChange());
                System.out.println("     steps: " + (MAX_STEPS_PER_SOLVER - currentStepsLeft));
                currentSolver = null;
                addTravelValue(distancePerRound);
                notifyRepaint();
            }
        }
    }    
}
