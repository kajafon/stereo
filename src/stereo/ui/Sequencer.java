/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.ui;

import javax.swing.JPanel;

/**
 *
 * @author sdsdf
 */
public class Sequencer {
    Runnable runningSequence = null;
    
    int runningSequenceStep;
    JPanel panelToRepaint;
    
    public boolean isRunning() {
        return runningSequence != null;
    }
    
    public void stop() {
        runningSequence = null;
    }
    
    public void donateSteps(int amount) {
        runningSequenceStep += amount;
    }
    
    public void runSequence(Runnable runnable, JPanel panel) {
        panelToRepaint = panel;
        SolutionNavigator _navigator = null; 
        if (runnable instanceof SolutionNavigator) {
            _navigator = (SolutionNavigator)runnable;
        }
        final SolutionNavigator navigator = _navigator; 
        runningSequence = new Runnable() {
            @Override
            public void run() {                
                if (runningSequenceStep <= 0) {
                    runningSequenceStep = 100;
                }
                
                for(; runningSequenceStep >= 0 && runningSequence != null; runningSequenceStep--) {                    
                    if (navigator != null) {
                        runningSequenceStep = 100;
                        if (navigator.isDone()) {
                            System.out.println("navigator is done with probing ... ");
                            break;
                        }                        
                    } 
                    runnable.run(); 
                    if (panelToRepaint != null) {
                        panelToRepaint.repaint();
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {                                
                    }
                }                
                
                runningSequence = null;
                System.out.println("sequence finished");
                if (navigator != null) {
                    navigator.finalizeProcess();
                }
                if (panelToRepaint != null) {
                    panelToRepaint.repaint();
                }
            }
        };
        new Thread(runningSequence).start();
    }
}
