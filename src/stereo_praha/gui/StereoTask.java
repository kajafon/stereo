/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo_praha.gui;

/**
 *
 * @author karol presovsky
 */
public abstract class StereoTask {
    double focalLength = 20;
    double moveX;
    double moveY;
    double moveZ;
    double angleX;
    double angleY;
    
    double goldError;

    public StereoTask() {
    }
        
    public double[] getVector()
    {
        return new double[]{angleX, angleY, moveX, moveY, moveZ, focalLength}; 
    }

    public void setVector(double[] vec)
    {
        angleX = vec[0];
        angleY = vec[1]; 
        moveX = vec[2]; 
        moveY = vec[3];
        moveZ = vec[4];
        focalLength = vec[5]; 
    }

    public double getFocalLength() {
        return focalLength;
    }
    
    
    
    public abstract void applySolution();
    
}
