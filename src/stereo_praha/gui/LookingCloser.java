/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo_praha.gui;

/**
 *
 * @author macbook
 */
public class LookingCloser {
    
  class ProblemAdapter2D extends SteroSolver {

        public ProblemAdapter2D(Object3D obj, double ax, double ay) {
            super(obj, ax, ay);
            
            double[] vector = super.getVector();
            
            
            
            
        }

        @Override
        public void setVector(double[] vec) {
            super.setVector(vec); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public double[] getVector() {
            return super.getVector(); //To change body of generated methods, choose Tools | Templates.
        }
      
  }   
    
}
