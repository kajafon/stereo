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
 * @author karol presovsky
 */
public abstract class Something3D {

    public double[][] transformed = null;
    public double[][] projected = null;
    public double[] matrix = Algebra.unity(null);
    
    /** after each projection this matrix contains transformation from local to global world       
    */
    public double[] tmp_matrix = Algebra.unity(null);
    public double[] _ctrl_matrix = Algebra.unity(null);
    
    double angleX;
    double angleY;
    double angleZ;  
    
    /** @deprecated */
    double[] translation = {0,0,0};
    
    public abstract void draw(Graphics g, double scale, int shiftx, int shifty);
    public abstract void project_implemented(double[] tmp_matrix); 
    
    boolean enabled = true;
    boolean visible = true;
    
    /** if object is not visible it is still transformed, projected but not drawn */
    public void setVisible(boolean val) {
        visible = val;
    }
    
    /** if object is not visible it is still transformed, projected but not drawn */
    public boolean isVisible() {
        return visible;
    }
    
    /** if object is not enabled it is not transformed, projected nor drawn */
    public void setEnabled(boolean val) {
        enabled = val;
    }
    
    /** if object is not enabled it is not transformed, projected nor drawn */
    public boolean isEnabled() {
        return enabled;
    }
    
    String name = "";

    @Override
    public String toString() {
        return name;
    }
    
    public void setInverseMatrix(double[] m)
    {
        Algebra.calcInverse(m, matrix);
    }

    public double getAngleX() {
        return angleX;
    }

    public double getAngleY() {
        return angleY;
    }

    public double getAngleZ() {
        return angleZ;
    }
    
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
        
        if (!isEnabled()) {
            return;
        }
        
        System.arraycopy(matrix, 0, tmp_matrix, 0, matrix.length);
        
        tmp_matrix[12] += translation[0];
        tmp_matrix[13] += translation[1];
        tmp_matrix[14] += translation[2];
        
        if (parent_matrix != null)
            Algebra.multiply_4x4(tmp_matrix, parent_matrix, tmp_matrix);
        
        
        project_implemented(tmp_matrix);
        
    }
    
    public void clearTransforms()
    {
        this.angleX = 0;
        this.angleY = 0;
        this.angleZ = 0;
        
        setTranslation(0, 0, 0);
        
        Algebra.unity(matrix);
    }

    /** @deprecated */
    public void setRotation(double angleX, double angleY, double angleZ) {

        this.angleX = angleX;
        this.angleY = angleY;
        this.angleZ = angleZ;

        Algebra.unity(matrix);
        stuff3D.rotate(matrix, angleX, angleY, angleZ, 0);

    }
    
    /** @deprecated */
    public void setRotation(double angleX, double angleY, double angleZ, double pivot) {
        setRotation(angleX, angleY, angleZ);
    }
    
    /** @deprecated */
    public void setTranslation(double[] v) {
       Algebra.copy(v, translation);
    }
    
    /** @deprecated */
    public void setTranslation(double x, double y, double z) {
        translation[0] = x;
        translation[1] = y;
        translation[2] = z;
    }
    
    /** @deprecated toto je gadzovina  */
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
