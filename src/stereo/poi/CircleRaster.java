/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.poi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author karol presovsky
 */
public class CircleRaster
{
   
    int radius;
    
    int [][] coords;

    public CircleRaster(int radius)
    {
        this.radius = radius;
        calcCoords(radius);
    }

    public int getRadius()
    {
        return radius;
    }
    
    public int length()
    {
        return coords.length;
    }
    
    public int[] get(int i)
    {
        return coords[i];
    }
    
    public int getX(int i)
    {
        return coords[i][0];
    }
    public int getY(int i)
    {
        return coords[i][1];
    }
    
    private void setCoord(int i, int x, int y)
    {
        coords[i][0] = x;
        coords[i][1] = y;
    }
    
    private void calcCoords(int r)
    {
        ArrayList<int[]> temp = new ArrayList<int[]>();
        int x = r, y = 0;
        int xChange = 1 - (r << 1);
        int yChange = 0;
        int radiusError = 0;

        
        while (x >= y)
        {
            temp.add(new int[]{x, y});
            
            y++;
            radiusError += yChange;
            yChange += 2;
            if (((radiusError << 1) + xChange) > 0)
            {
                x--;
                radiusError += xChange;
                xChange += 2;
            }
           
        }  
        
        int arcSize = temp.size();
        int doubleArc = 2*arcSize-1 - (r&1);
        coords = new int[4*doubleArc][2];
        for(int i=0; i<temp.size(); i++)
        {
            int[] c = temp.get(i);
            x = c[0];
            y = c[1];
            
            setCoord(i, x, y);
            
            if (i>0) setCoord(doubleArc - i, y, x);
            setCoord(doubleArc + i, -y, x);

            if (i>0) setCoord(2*doubleArc - i, -x, y);
            setCoord(2*doubleArc + i, -x, -y);
            
            
            if (i>0) setCoord(3*doubleArc - i, -y, -x);
            setCoord(3*doubleArc + i, y, -x);

            if (i>0) setCoord(4*doubleArc - i, x, -y);
            
        }

        
    }
    
    public static void main(String[] args)
    {
        
        final CircleRaster raster = new CircleRaster(11);
        final int[] indx = new int[1];
        final JPanel panel = new JPanel()
        {
            

            @Override
            protected void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                

                
                int size = getWidth()/(raster.radius*2+5);
                if (size == 0)
                    size = 1;
                if (size > 20)
                    size = 20;
                
                int x0 = raster.radius*size + size;
                int y0 = raster.radius*size + size;
                    
                
                for (int i=0; i<raster.length(); i++)
                {
                    int s = size;
                    if (i==indx[0]%raster.length())
                    {
                        g.setColor(Color.RED);
                        s+=2;
                    }
                    else
                        g.setColor(Color.BLACK);
                    
                    int x = raster.getX(i)*size + x0;
                    int y = raster.getY(i)*size + y0;
                    
                    g.drawRect(x - s/2, y - s/2, s, s);
                }
            }
        };
        
        panel.addMouseListener(new MouseAdapter()
        {

            @Override
            public void mousePressed(MouseEvent e)
            {
                indx[0]++;
                panel.repaint();
            }
            
        });
                   
        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new BorderLayout());
//        frame.getContentPane().add(new JLabel(new ImageIcon(img)));
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocation(100, 100);
        frame.setSize(new Dimension(100, 100));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
    }
    
    
}
