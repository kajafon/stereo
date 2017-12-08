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
    double[] tangent1;
    double[] tangent2;

    double[] translation1;
    double[] translation2;
    double[] translation3;
    double[] _rotation;
    double [] _x1;
    double [] _x2;
    double [] _v1;
    double [] _v2;
    double [][] vertex;
    double [][] pulls;
    int vertexCount;
    
    Aggregator vertexSum;
    Aggregator pullsSum;

        
    public void print()
    {
        Algebra.printVecLn(_x1, "x1");
        Algebra.printVecLn(_x2, "x2");
        Algebra.printVecLn(tangent1, "tangent1");
        Algebra.printVecLn(tangent2, "tangent2");
        Algebra.printVecLn(translation1, "translation1");
        Algebra.printVecLn(translation2, "translation2");
        System.out.println("--------------");
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
    
    public void add2(double[] x, double[] v){
        Algebra.copy(x, vertex[vertexCount]);
        Algebra.copy(v, pulls[vertexCount]);  
        
        vertexSum.add(x);
        pullsSum.add(v);
        
        vertexCount++;
        
    }
    
    public void calc2()
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
            
            Algebra.vectorProduct(leverage, tangent, newRotation);
            double newRotationSize = Algebra.size(newRotation);
            
            if (newRotationSize < 0.000001) {
                continue;
            }
            
            Algebra.scale(newRotation, leverageSize);
            
            Algebra.combine(newRotation, rotation, rotation);

        }
        
        
    }
    
    public void add(double[] x2, double[] v2)            
    {
        double[] x1 = this.hit;
        double[] v1 = this.translation;
        
        _x1 = x1;
        _x2 = x2;
        _v1 = v1;
        _v2 = v2;
        
        leverage = Algebra.difference(x2, x1, null);
        tangent1 = Algebra.projectToPlane(leverage, v1);
        tangent2 = Algebra.projectToPlane(leverage, v2);
        
        if (!Algebra.isValid(tangent1)) {
            System.out.println("!ta1");
        }
        if (!Algebra.isValid(tangent2)) {
            System.out.println("!ta2");
        }

        
        translation1 = Algebra.difference(v1, tangent1, null);
        translation2 = Algebra.difference(v2, tangent2, null);
        translation3 = null;
        
        double[] new_rotation = null;
        
        
        
        double tangent1Size = Algebra.size(tangent1);
        double tangent2Size = Algebra.size(tangent2);
        
        if (tangent1Size + tangent2Size < 0.0000001) {
            Algebra.printVecLn(Algebra.combine(translation1, translation2, null),"result is translation: ");
            return;
        }
        
        double nonParalel = Algebra.size(Algebra.vectorProduct(tangent1, tangent2, null));
        
        if (nonParalel < 0.0000001) {
            // tangents are parallel
            
            double[] mean = translation3 = Algebra.scale(Algebra.combine(tangent1, tangent2, null), 0.5);
            Algebra.difference(tangent1, mean, tangent1);
            
            new_rotation = Algebra.vectorProduct(tangent1, leverage, null);
            Algebra.scale(new_rotation, Algebra.size(tangent1)/Algebra.size(new_rotation)/Algebra.size(leverage));
            
        } else {
            translation3 = Algebra.combine(tangent1, tangent2, null);
            
            
            double[] flatTangent = Algebra.projectToPlane(translation3, tangent1);
            if (!Algebra.isValid(flatTangent)) {
                System.out.println("!f");
            }
            if (!Algebra.isValid(tangent1)) {
                System.out.println("!1");
            }
            if (!Algebra.isValid(tangent2)) {
                System.out.println("!2");
            }
            
            new_rotation = Algebra.vectorProduct(flatTangent, leverage, null);
            double newRotationSize = Algebra.size(new_rotation);
            
            if (newRotationSize > 0.0000001) {
                if (!Algebra.isValid(new_rotation)) {
                    System.out.println("!3/2");
                }
                Algebra.scale(new_rotation, Algebra.size(flatTangent)/newRotationSize);
                if (!Algebra.isValid(new_rotation)) {
                    System.out.println("!3/2");
                }
            }
        }
        

        Algebra.combine(translation3, translation1, translation3);
        Algebra.combine(translation3, translation2, translation3);
        
        Algebra.combine(translation3, translation, translation);
        
        Algebra.combine(hit, Algebra.scale(Algebra.combine(x1, x2, null), 0.5), hit);
        Algebra.combine(rotation, new_rotation, rotation);
        

    }
    
    public static void impulseTest()
    {
        double[] x1 = new double[]{0,0,0};
        double[] x2 = new double[]{1,0,0};
        double[] x3 = new double[]{0.3,0.5,0};
        double[] v1 = new double[]{-0.5,1,0.1};
        double[] v2 = new double[]{0.5,-1,0.1};        
        double[] v3 = new double[]{0.5,0.1,0};
        
        Impulse impulse = new Impulse(x1, v1);
        impulse.add(x2, v2);
        impulse.add(x3, v3);
        
        impulse.print();
        
        System.out.println("============================================================");
        
        impulse = new Impulse(x3, v3);
        impulse.add(x1, v1);
        impulse.add(x2, v2);
        
        impulse.print();        
    }
    
    
    public static void main(String[] args) {
        impulseTest();
    }
    

    
}
