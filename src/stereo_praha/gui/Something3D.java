/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo_praha.gui;

import java.awt.Graphics;
import stereo_praha.Algebra;

/**
 *
 * @author macbook
 */
public abstract class Something3D {

    public double[][] transformed = null;
    public double[][] projected = null;
    public double[] matrix = Algebra.unity(null);
    public double[] tmp_matrix = Algebra.unity(null);
    
    double angleX;
    double angleY;
    double angleZ;    
    double[] translation = {0,0,0};
    
    public abstract void draw(Graphics g, double scale, int shiftx, int shifty);
    public abstract void project_implemented(double[] tmp_matrix); 

    
    String name = "";
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void project()
    {
        project(null);
    }
    
    public void project(double[] parent_matrix) {
        System.arraycopy(matrix, 0, tmp_matrix, 0, matrix.length);
        
        tmp_matrix[12] += translation[0];
        tmp_matrix[13] += translation[1];
        tmp_matrix[14] += translation[2];
        
        if (parent_matrix != null)
            Algebra.multiply_4x4(tmp_matrix, parent_matrix, tmp_matrix);
        
        
        project_implemented(tmp_matrix);
        
    }

    public void setRotation(double angleX, double angleY, double angleZ) {

        this.angleX = angleX;
        this.angleY = angleY;
        this.angleZ = angleZ;

        Algebra.unity(matrix);
        stuff3D.rotate(matrix, angleX, angleY, angleZ, 0);

    }
    
    public void setRotation(double angleX, double angleY, double angleZ, double pivot) {
        setRotation(angleX, angleY, angleZ);
    }
    
    public void setTranslation(double x, double y, double z) {
        translation[0] = x;
        translation[1] = y;
        translation[2] = z;
    }
    
    public void rotate(double vax, double vay, double vaz) {
        angleX += vax;
        angleY += vay;
        angleZ += vaz;
        Algebra.unity(matrix);
        stuff3D.rotate(matrix, angleX, angleY, angleZ, 0);

    }
    
    public double getM12()
    {
        return matrix[12] + translation[0];
    }
    public double getM13()
    {
        return matrix[13] + translation[1];
    }
    public double getM14()
    {
        return matrix[14] + translation[2];
    }

    
}
