/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author karol presovsky
 */
public class Triangulator
{
    
    public interface LinkAccess
    {
        int x1(Object o);
        int x2(Object o);
        int y1(Object o);
        int y2(Object o);
    }
    public interface FaceCreator
    {
        Object createFace(int i1, int i2, int i3, ArrayList linkList);    
    }
    
    public interface Callback
    {
        boolean run(ArrayList<Integer> scanList);
    }

    
    public static ArrayList run(ArrayList list, Callback callback, final LinkAccess link, FaceCreator faceCreator)
    {
        Collections.sort(list, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return link.y1(o1) - link.y1(o2);
            }
        });
        
        ArrayList faces = new ArrayList();
        ArrayList<Integer> scanList = new ArrayList<Integer>();
        
        int currentIndex = 0;
        for (Object r:list)
        {
            
            int i;
            for (i=0; i<scanList.size(); i++)
            {
                Object r2 = list.get(scanList.get(i));
                if (link.x1(r2) > link.x1(r))
                    break;
            }
          
            
            if (i < scanList.size() && i > 0)
            {
                faces.add(faceCreator.createFace(currentIndex, scanList.get(i), scanList.get(i-1), list));
            }
    
   
            scanList.add(i, currentIndex);
            
            if (callback != null)
                callback.run(scanList);

            
            for (i=0; i<scanList.size()-2; i++)
            {
                int x1 = link.x1(list.get(scanList.get(i)));
                int x2 = link.x1(list.get(scanList.get(i+1)));
                int x3 = link.x1(list.get(scanList.get(i+2)));
                
                int y1 = link.y1(list.get(scanList.get(i)));
                int y2 = link.y1(list.get(scanList.get(i+1)));
                int y3 = link.y1(list.get(scanList.get(i+2)));
                
                int dx2 = x2 - x1;
                int dy2 = y2 - y1;
                int dx3 = x3 - x1;
                int dy3 = y3 - y1;
                int d2 = dx2*dx2 + dy2*dy2;
                int d3 = dx3*dx3 + dy3*dy3;
                int k2;
                int k3;
                if (dx2 == 0)
                    k2 = dy2>0?Integer.MAX_VALUE:Integer.MIN_VALUE;
                else
                    k2 = dy2/dx2;
                
                if (dx3 == 0)
                    k3 = dy3>0?Integer.MAX_VALUE:Integer.MIN_VALUE;
                else
                    k3 = dy3/dx3;
                
                if (k3 > k2 && (d3 < d2*5 || Math.abs(dx2)<Math.abs(dy2) || Math.abs(dx3)<Math.abs(dy3)))
                {
                    faces.add(faceCreator.createFace(scanList.get(i),scanList.get(i+1),scanList.get(i+2), list));
                    scanList.remove(i+1);
                }

            }
            
            if (callback != null)
                callback.run(scanList);
            
            currentIndex++;
        }
        
        return faces;
    }
    
    public static int area(int x1, int y1, int x2, int y2, int x3, int y3)
    {
        int [][] coord = new int [][]{{x1,y1},{x2,y2},{x3,y3}};
        
        int fy1 = 0;
        int fy2 = 1;
        int fy3 = 2;
        
        if (coord[fy1][1]>coord[fy2][1])
        {
            fy1 = 1;
            fy2 = 0;
        }

        if (coord[fy1][1]>coord[fy3][1])
        {
            int i = fy1;
            fy1 = fy3;
            fy3 = i;
        }
        
        if (coord[fy2][1]>coord[fy3][1])
        {
            int i = fy2;
            fy2 = fy3;
            fy3 = i;
        }       
        
        int height = coord[fy3][1] - coord[fy1][1];
        if (height == 0)
            return 0;
        
        int dy = coord[fy2][1] - coord[fy1][1];
        
        double w = (double)dy/height;
        int xs = coord[fy1][0] + (int)(w*(coord[fy3][0] - coord[fy1][0]));
        xs = Math.abs(xs - coord[fy2][0]);
        
        return xs*height/2;
    }
    
    public static int perimeter(int x1, int y1, int x2, int y2, int x3, int y3)
    {
        int dx1 = x1 - x2;
        int dx2 = x1 - x3;
        int dx3 = x2 - x3;
        int dy1 = y1 - y2;
        int dy2 = y1 - y3;
        int dy3 = y2 - y3;
        return (int)(Math.sqrt(dx1*dx1 + dy1*dy1) + Math.sqrt(dx2*dx2 + dy2*dy2) + Math.sqrt(dx3*dx3 + dy3*dy3));
    }
    
    public static int faceDirection(int x1, int y1, int x2, int y2, int x3, int y3)
    {
        int u1 = x2 - x1;
        int u2 = y2 - y1;
        int v1 = x3 - x1;
        int v2 = y3 - y1;
        
        return u1*v2 - u2*v1;
    }
    
    public static void main(String[] args)
    {

        int[][] v = new int[3][];
        
        v[0] = new int[]{0,0};
        v[1] = new int[]{10,0};
        v[2] = new int[]{0,10};
        
        System.out.println("-" + faceDirection(v[0][0], v[0][1],   v[1][0], v[1][1],    v[2][0], v[2][1]));
        System.out.println("-" + faceDirection(v[0][0], v[0][1],   v[2][0], v[2][1],    v[1][0], v[1][1]));
        

    }

}
