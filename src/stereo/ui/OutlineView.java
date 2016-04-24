/*
 * 
 * 
 */

package stereo.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JPanel;
import stereo.poi.Cntx;
import stereo.poi.Cntx2;
import stereo.Greyscale;
import stereo.Outline;

/**
 *
 * @author Karol Presovsky
 */
public class OutlineView extends JPanel
{
    ArrayList<Outline> vecs;
    ArrayList<int[]> maxims;

    Image img;
    Greyscale gs;
    boolean showVectors = true;
    boolean showImg = true;
    boolean showMaxims = true;
    boolean showMarked = true;
    boolean readEvents = true;
    
    int imgWidth;
    int imgHeight;
    int maxmax;
    int maxmin;
    
    Double angle = null;
    
    Point spot;

    public void setAngle(Double angle)
    {
        this.angle = angle;
    }

    
    public void setMaxims(ArrayList<int[]> maxims)
    {
        this.maxims = maxims;
    }

    public void setSpot(Point spot)
    {
        this.spot = spot;
    }
    
    public void setReadEvents(boolean readEvents)
    {
        this.readEvents = readEvents;
    }

    public void setShowVectors(boolean showVectors)
    {
        this.showVectors = showVectors;
    }

    
    public Image getImg()
    {
        return img;
    }
    
    public OutlineView showMaxims(boolean showMaxims)
    {
        this.showMaxims = showMaxims;
        return this;
    }

    public boolean isShowMaxims()
    {
        return showMaxims;
    }
    
    public OutlineView(Image img, ArrayList<Outline> vecs, Greyscale gs)
    {
        this.vecs = vecs;
        this.gs = gs;

        this.imgWidth = img.getWidth(null);
        this.imgHeight = img.getHeight(null);
        
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(imgWidth,imgHeight));
        this.img = img;
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        setFocusable(true);
        requestFocus();
        
        addKeyListener(new KeyAdapter()
        {

            @Override
            public void keyPressed(KeyEvent e)
            {
                switch(e.getKeyCode())
                {
                    case KeyEvent.VK_S:
                        showVectors = !showVectors;
                        repaint();
                        break;
                    case KeyEvent.VK_I:
                        showImg = !showImg;
                        repaint();
                        break;
                    case KeyEvent.VK_M:
                        showMaxims = !showMaxims;
                        repaint();
                        break;
                        
                }
            }
            
        });
        
//        addMouseListener(new MouseAdapter()
//        {
//            @Override
//            public void mouseClicked(MouseEvent e)
//            {
//                int ctrl = e.getModifiers()&MouseEvent.CTRL_DOWN_MASK;
//                System.out.println("ctrl:"+ctrl + ", readEvents:" + readEvents);
//                if (!readEvents || ctrl == 0)
//                    return;
//                
//                if (e.getPoint().y > getHeight()/2)
//                    return;
//                if (e.getButton() == MouseEvent.BUTTON1)
//                    showVectors = !showVectors;
//                else
//                    showMaxims = !showMaxims;
//                    
//                repaint();
//            }
//        });
    }


    void paintVectors(Graphics g)
    {
        if (vecs == null)
            return;
        if (vecs == null) return;
        
        
        for (int k=0;k<vecs.size();k++)
        {
            Outline v = vecs.get(k);
            
            Color c = new Color((float)Math.random()*0.4f + 0.6f, (float)Math.random()*0.4f + 0.6f, (float)Math.random()*0.4f + 0.6f);
            g.setColor(c);
          
            int offx = 0;//v.yScan?0:3;
            int offy = 0;
  
             Point p1 = v.getP(0);

            for (int i=1;i<v.size(); i++)
            {
                Point p2 = v.getP(i);
                g.drawLine(p1.x+offx, p1.y+offy, p2.x+offx, p2.y+offy);
                p1 = p2;
            }
        }
      
    }

    void drawMaxims(Graphics g)
    {
        if (maxims != null)
        {
            for (int[] m:maxims)
            {
                g.drawLine(m[0], m[1], m[0], m[1]);
            }
        }
    }
    
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        
        if (showImg)
            g.drawImage(img, 0, 0, null);
       
        if (showVectors)
        {
            g.setColor(Color.YELLOW);
            paintVectors(g);
        }
        
        if (showMaxims)
        {
            g.setColor(Color.RED);
            drawMaxims(g);
        }
        
        if (angle != null)
        {
            int dx = (int)(50*Math.cos(angle));
            int dy = (int)(50*Math.sin(angle));
            
            g.setColor(Color.WHITE);
            g.drawLine(getWidth()/2 - dx, getHeight()/2 - dy, getWidth()/2 + dx, getHeight()/2 + dy);
        }
            
        
    }
}
