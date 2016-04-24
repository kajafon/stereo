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
public class SampleObject {
    
    
    public static Object3D hill()
    {
        int gridSize = 9;
        double hillSize = 20;
        
        double[][] v = new double[gridSize*gridSize][3]; 
        int[][] f = new int[(gridSize-1)*(gridSize-1)][3];
        
        for (int j=0; j<gridSize; j++)
        {
            double u = (j - gridSize/2.0)/gridSize*2.0;
            double uu = u*u; 
            for (int i=0; i<gridSize; i++)
            {
                double w = (i - gridSize/2.0)/gridSize*2.0;
                double ww = w*w;
                int indx = j*gridSize + i;
                v[indx][0] = u * hillSize;
                v[indx][2] = w * hillSize;
                v[indx][1] = Math.sqrt(ww + uu)* hillSize - hillSize;
            }
        }
        for (int j=0; j<gridSize-1; j++)
        {
            int fIndex = j*(gridSize-1);
            int vIndex = j*gridSize;
            
            for (int i=0; i<gridSize-1; i++)
            {
               f[fIndex+i][0] = vIndex + i + gridSize;   
               f[fIndex+i][1] = vIndex + i;   
               f[fIndex+i][2] = vIndex + i + 1;   
            }
        }
        
        return new Object3D(v,f);
    }
    
    public static Object3D platforms(int count)
    {
        int stories = 5;
        int size = 5;
        
        double[][] orig_vertex = new double[stories*4][3];
        int[][] orig_triangles = new int[stories+4][5]; 
        
        int base_y = -stories*size/2;
        
        for (int j=0; j<stories; j++)
        {
            int x = -10;
            int z = -10;
            
            for (int i=0; i<4; i++)
            {
                orig_vertex[j*4 + i][0] = x;
                orig_vertex[j*4 + i][1] = base_y;
                orig_vertex[j*4 + i][2] = z;
                
                int tmp = -z;
                z = x;
                x = tmp;
            }
            
            base_y += size;
            
            for (int i=0; i<=4; i++)
            {
               orig_triangles[j][i] = j*4 + (i%4);
            }
        }
        
        for (int i=0; i<4; i++)
        {
            for (int j=0; j<stories; j++)
            {
                orig_triangles[stories + i][j] = i + j*4;
            }
        }
       
        return new Object3D(orig_vertex, orig_triangles);
        
    }

    
}