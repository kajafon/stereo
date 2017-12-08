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
    
    public double[] translation = new double[3];
    public double[] rotation = new double[3];
    public double[] hit = new double[3];
    
    double[] leverage;
    double [][] vertex;
    double [][] pulls;
    int vertexCount;
    
    Aggregator vertexSum;
    Aggregator pullsSum;

        
    public void print()
    {
        Algebra.printVecLn(hit, "hit");
        Algebra.printVecLn(translation, "translation");
        Algebra.printVecLn(rotation, "rotation");
    }

    public Impulse() {
        
    }  
    
    public Impulse(double[] x1, double[] v1) {
        Algebra.copy(x1, hit);
        Algebra.copy(v1, translation);
    }
    
    public void init(int count) {
        if (pulls == null || pulls.length != count){
            pulls = new double[count][3];
            vertex = new double[count][3];                
        }
        
        vertexCount = 0;
        vertexSum = new Aggregator(3);
        pullsSum = new Aggregator(3);
        
    }
    
    public void add(double[] x, double[] v){
        Algebra.copy(x, vertex[vertexCount]);
        Algebra.copy(v, pulls[vertexCount]);  
        
        vertexSum.add(x);
        pullsSum.add(v);
        
        vertexCount++;
        
    }
    
    public void calc()
    {
        hit = vertexSum.getAverage();
        translation = pullsSum.getAverage();
        
        double[] leverage = new double[3];
        double[] tangent = new double[3];
        double[] newRotation = new double[3];
        
        rotation = new double[3];
        
        for (int i=0; i<vertexCount;i++) {
            Algebra.difference(vertex[i], hit, leverage);
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
            
            Algebra.combine(newRotation, rotation, rotation);

        }
        
        Algebra.scale(rotation, 0.9/vertexCount);
        
//        double rotationSize = Algebra.size(rotation);
//        
//        if (rotationSize > 0.00001) {
//            rotationSize = 0.0003/Math.pow(vertexCount, 1.42);
//            Algebra.scale(rotation, rotationSize);
//        }
        
    }
    
    

    
}
