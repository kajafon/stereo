/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo_praha;

import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;
import stereo.ui.GuiUtils;
import static stereo_praha.Algebra.printVecLn;
import stereo_praha.gui.Object3D;
import stereo_praha.gui.stuff3D;

/**
 *
 * @author karol presovsky
 */
public class Impulse {
    
    double[] translation = new double[3];
    double[] rotation = new double[3];
    double[] hitSpot = new double[3];
    
    double[] leverage;
    double [][] vertex;
    double [][] pulls;
    int vertexCount;
    
    Aggregator vertexSum = new Aggregator(3);
    Aggregator pullsSum = new Aggregator(3);

    boolean calculated = false;
    
    public double[] getRotation(double[] target) {        
        if (!calculated) {
            calc();
        }
        
        if (target == null) {
            target = new double[rotation.length];        
        }
        
        System.arraycopy(rotation, 0, target, 0, rotation.length);        
        return target;        
    }
    
    public double[] getTranslation(double[] target) {        
        if (!calculated) {
            calc();
        }
        
        if (target == null) {
            target = new double[translation.length];        
        }
        
        System.arraycopy(translation, 0, target, 0, translation.length);        
        return target;        
    }
    
    public double[] getHitSpot(double[] target) {        
        if (!calculated) {
            calc();
        }
        
        if (target == null) {
            target = new double[translation.length];        
        }
        
        System.arraycopy(hitSpot, 0, target, 0, hitSpot.length);        
        return target;        
    }        
    
    public void print()
    {
        Algebra.printVecLn(hitSpot, "hit");
        Algebra.printVecLn(translation, "translation");
        Algebra.printVecLn(rotation, "rotation");
    }

    public Impulse() {
        
    }  
    
    public Impulse(double[] x1, double[] v1) {
        Algebra.copy(x1, hitSpot);
        Algebra.copy(v1, translation);
    }
    
    public void init(int count) {
        vertex = new double[count][3];
        pulls = new double[count][3];
    
        vertexCount = 0;        
        vertexSum.reset();
        pullsSum.reset();        
    }
    
    public void add(double[] x, double[] v){
        calculated = false;
        Algebra.copy(x, vertex[vertexCount]);
        Algebra.copy(v, pulls[vertexCount]);  
        
        vertexSum.add(x);
        pullsSum.add(v);
        
        vertexCount++;        
    }
    
    public void calc()
    {
        hitSpot = vertexSum.getAverage(null);
        translation = pullsSum.getAverage(null);
        
        double[] leverage = new double[3];
        double[] tangent = new double[3];
        double[] newRotation = new double[3];
        
        rotation = new double[3];
        
        for (int i=0; i<vertexCount;i++) {
            leverage = Algebra.difference(vertex[i], hitSpot, leverage);
            double leverageSize = Algebra.size(leverage);
            if (leverageSize < 0.000001){
                continue;
            }
            tangent = Algebra.projectToPlane(leverage, pulls[i]);
            double tangentSize = Algebra.size(tangent);
            if (tangentSize < 0.000001){
                continue;
            }
            
            double angle = Math.atan2(tangentSize, leverageSize);
            Algebra.vectorProduct(leverage, tangent, newRotation);
            double newRotationSize = Algebra.size(newRotation);
            
            if (newRotationSize < 0.000001) {
                continue;
            }
            
            Algebra.scale(newRotation, angle/newRotationSize);            
            Algebra.add(newRotation, rotation, rotation);
        }
        
        Algebra.scale(rotation, 0.9/vertexCount);        
        calculated = true;        
    }
}
