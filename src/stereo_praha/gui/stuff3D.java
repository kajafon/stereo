package stereo_praha.gui;

import stereo_praha.Algebra;

import java.awt.*;

/**
 * Created by Karol Presovsky on 8/10/14.
 */
public class stuff3D {

    public static void project(double[][] src, double[][] dest, double[][] projection, double[] m) {
        for (int i=0; i<src.length; i++) {
            Algebra.multiply4_4x4(m, src[i], dest[i]);
            double z = dest[i][2];
            if (z <= 0) {
                projection[i][0] = -1000000;
                projection[i][1] = -1000000;

                continue;
            }

            projection[i][0] = dest[i][0]/z;
            projection[i][1] = dest[i][1]/z;
        }
    }
    
    public static double[] toProjectionSpace(int gx, int gy, int gWidth, int gHeight, double scale, int shiftx, int shifty) {
        
        int xoffs = (gWidth)/2 + shiftx;
        int yoffs = (gHeight)/2 + shifty;            
        
        return new double[]{(gx - xoffs) / scale, (gy - yoffs) / scale};
    }

    static Color[] colorList = new Color[]{Color.red, Color.blue, Color.yellow, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.white};
    
    public static void draw(Graphics g, double scale, int [][] lines, double [][] vertex, int shiftx, int shifty) {
        Rectangle clipBounds = g.getClipBounds();
        int xoffs = (clipBounds.width)/2 + shiftx;
        int yoffs = (clipBounds.height)/2 + shifty;            
        
        if (lines == null)
        {
            for (int i=0; i<vertex.length; i++)
            {
               g.setColor(colorList[i%colorList.length]);
               g.drawRect((int)(vertex[i][0]*scale + xoffs - 5), (int)(vertex[i][1]*scale + yoffs - 5), 10, 10);
            }
        } else 
        {
            for (int[] t : lines) 
            {
                for (int j=1; j<t.length; j++) 
                {
                    int x1 = (int) (vertex[t[j-1]][0] * scale) + xoffs;
                    int y1 = (int) (vertex[t[j-1]][1] * scale) + yoffs;
                    int x2 = (int) (vertex[t[j  ]][0] * scale) + xoffs;
                    int y2 = (int) (vertex[t[j  ]][1] * scale) + yoffs;

                    g.drawLine(x1, y1,
                            x2, y2);
                }
            }
        }
    }

    public static void draw(Graphics g, double scale, int [][] lines, int linesStart, int linesStop, double [][] vertex, int shiftx, int shifty) {
        Rectangle clipBounds = g.getClipBounds();
        int xoffs = (clipBounds.width)/2;
        int yoffs = (clipBounds.height)/2;

        for (int i=linesStart; i<linesStop; i++) {
            int t[] = lines[i];

            for (int j=1; j<t.length; j++) {
                int x1 = (int) (vertex[t[j-1]][0] * scale) + xoffs + shiftx;
                int y1 = (int) (vertex[t[j-1]][1] * scale) + yoffs + shifty;
                int x2 = (int) (vertex[t[j  ]][0] * scale) + xoffs + shiftx;
                int y2 = (int) (vertex[t[j  ]][1] * scale) + yoffs + shifty;

                g.drawLine(x1, y1,
                        x2, y2);
            }
        }
    }

    public static void setRotation(double[] matrix, double angelX, double angelY, double angelZ) {
        double[] pos = Algebra.getPositionBase(matrix, null);
        Algebra.unity(matrix);
        rotate(matrix, angelX, angelY, angelZ, 0);
        Algebra.setPosition(matrix, pos);
    }
    
    public static void rotate(double[] m, double angelX, double angelY, double angelZ, double zPivot) {
        m[14] -= zPivot;

        double[] r = new double[m.length];
        
        if (angelX != 0){
            Algebra.rotation(r, Algebra.AXIS_X, angelX);
            Algebra.multiply_4x4(m, r, m);
        }        
        if (angelY != 0) {
            Algebra.rotation(r, Algebra.AXIS_Y, angelY);
            Algebra.multiply_4x4(m, r, m);
        }
        if (angelZ != 0) {
            Algebra.rotation(r, Algebra.AXIS_Z, angelZ);
            Algebra.multiply_4x4(m, r, m);
        }

        m[14] += zPivot;
    }

    public static void main(String[] args) {
        double[] m1 = Algebra.unity(null);
        double[] m2 = Algebra.unity(null);

        rotate(m1, 0.1, 0.2, 0, 20);
        rotate(m2, 0.1, 0.2, 0,  0);

        Algebra.print(m1);
        Algebra.print(m2);

    }

}
