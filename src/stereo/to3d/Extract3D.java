/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.to3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author karol presovsky
 */
public class Extract3D
{

    public static void main(String[] args)
    {
        mesh();
    }
        
    public static void mesh()        
    {

        int focus = 500;
        Mesh mesh = new Mesh(10, 30);
        
        
        int[][] screen1 = mesh.projection(focus);
        mesh.translate(2, 0, 0);
        int[][] screen2 = mesh.projection(focus);
        
        mesh.maxMin();
        
        double[] zets = new double[screen1.length];
        
        double maxz = -10000;
        double minz = 10000;
        for (int i=0; i<screen1.length; i++)
        {
            double d = screen2[i][0] - screen1[i][0];
            int dy = screen2[i][1] - screen1[i][1];
            d = Math.sqrt(d*d + dy*dy);
            if (d != 0)
            {
                d = 1.0/d;
                zets[i] = d;
                if (d < minz) minz = d;
                if (d > maxz) maxz = d;
            }
        }
        
        maxz -= minz;
        if (maxz == 0)
        {
            System.out.println(" error, maxz == 0");
            return;
        }
        double scale = (mesh.maxz - mesh.minz)/maxz;
        ArrayList<double[]> list = new ArrayList<double[]>();
        for (int i=0; i<zets.length; i++)
        {
            zets[i] = (zets[i] - minz)*scale + mesh.minz;
            list.add(new double[]{zets[i], mesh.v[i][2]+mesh.z});
        }
        
        Collections.sort(list, new Comparator()
        {

            public int compare(Object o1, Object o2)
            {
                return (int)((((double[])o1)[0] - ((double[])o2)[0])*10);
            }
            
        });
        
        for (double[] v:list)
        {
            System.out.println(v[0] + " -> " + v[1]);
        }
                
    }
}

class Mesh
{

    int verticeNum;
    double[][] v;
    double x = 0;
    double y = 0;
    double z = 50;
    
    double maxz;
    double minz;

    public Mesh(int verticeNum, int meshSize)
    {
        this.verticeNum = verticeNum;
        v = new double[verticeNum][3];

        for (int i = 0; i < verticeNum; i++)
        {
            v[i][0] = (Math.random() - 0.5) * meshSize;
            v[i][1] = (Math.random() - 0.5) * meshSize;
            v[i][2] = (Math.random() - 0.5) * meshSize;
        }
    }

    public void translate(double offsx, double offsy, double offsz)
    {
        x += offsx;
        y += offsy;
        z += offsz;
    }
    
    public int[][] projection(int focus)
    {
        int[][] screen = new int[v.length][2];
        for (int i = 0; i < verticeNum; i++)
        {
            screen[i][0] = (int) (focus * (v[i][0] + x) / (v[i][2] + z));
            screen[i][1] = (int) (focus * (v[i][1] + y) / (v[i][2] + z));
        }
        return screen;
    }
    
    public void maxMin()
    {
        minz = 100000;
        maxz = -100000;
        
        for (int i=0; i<v.length; i++)
        {
            double _z = v[i][2] + z;
            if (_z > maxz)
                maxz = _z;
            if (_z < minz)
                minz = _z;
        }
    }
}
