/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.poi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import stereo.ui.GuiUtils;

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
    
    private void calcCoords(int radius)
    {
        double sqrRad = radius * radius;

        int segment_steps_cnt = (int)Math.round(radius / Math.sqrt(2));
        
        ArrayList<int[]> seg1 = new ArrayList<>();
        ArrayList<int[]> seg2 = new ArrayList<>();
        
        
        int y_stop = 0;
        for (int x=0; x<segment_steps_cnt; x++) {
            y_stop = (int)Math.round(Math.sqrt(sqrRad - x*x));
            seg1.add(new int[]{x, -y_stop});
        }
        
        for (int y=1; y<y_stop; y++) {            
            int x = (int)Math.round(Math.sqrt(sqrRad - y*y));
            if (x < segment_steps_cnt) {
                break;
            }            
            seg2.add(new int[]{x, -y});
        }       
        
        for (int i=seg2.size()-1; i>=0; i--) {
            seg1.add(seg2.get(i));
        }

        int steps_quarter = seg1.size();
        int[][] result = new int[steps_quarter*4][2];
        for (int i=0; i<seg1.size(); i++) {            
            result[i][0] = seg1.get(i)[0]; 
            result[i][1] = seg1.get(i)[1]; 
        }
        
        for (int i=0; i<steps_quarter; i++) {            
            int _x = result[i][0];
            int _y = result[i][1];
         
            int tmp = _x;
            _x = -_y;
            _y = tmp;
            
            int index = i+steps_quarter;
            result[index][0] = _x;
            result[index][1] = _y;
            
            tmp = _x;
            _x = -_y;
            _y = tmp;
            
            index = i+2*steps_quarter;
            result[index][0] = _x;
            result[index][1] = _y;
            
            tmp = _x;
            _x = -_y;
            _y = tmp;
            
            index = i+3*steps_quarter;
            result[index][0] = _x;
            result[index][1] = _y;
        }
        
        coords = result;
    }

    public static void main(String[] args){
        
        CircleRaster cir = new CircleRaster(6);        
        
        int a = 10;
        
        final int[] index = new int[1];
        
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                int xs = getWidth()/2;
                int ys = getHeight() / 2;
                
                g.setColor(Color.gray);
                
                for (int i=0; i<cir.length(); i++) {                    
                    if (index[0] == i) {
//                        System.out.println("i:" + index[0] + ": " + circle[i][0] + ", " + circle[i][1]);
                        g.drawRect(xs + cir.getX(i)*a - 2, ys + cir.getY(i)*a - 2, a+2, a+2);                    
                    }
                    g.fillRect(xs + cir.getX(i)*a, ys + cir.getY(i)*a, a-2, a-2);                    
                }                
            }            
        };
        
        JButton b = new JButton(new AbstractAction("F!") {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                index[0] = (index[0]+1) % cir.length();
                p.repaint();
            }            
        });
        
        p.add(b);
        
        GuiUtils.frameIt(p, 400, 700, null);
    }    
}
