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
public class VectorView extends JPanel
{
    ArrayList<Outline> vecs;

    ArrayList<Cntx2> maxims;
    ArrayList<Cntx2> neighbours;

    Image img;
    Image img2;
    Image img3;
    Greyscale gs;
    Greyscale gs_grad;
    boolean showVectors = true;
    boolean showImg = true;
    boolean showMaxims = false;
    boolean showMarked = true;
    boolean readEvents = true;
    
    int imgWidth;
    int imgHeight;
    int maxmax;
    int maxmin;
    
    Point spot;

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

    
    public void setNeighbours(ArrayList<Cntx2> neighbours)
    {
        this.neighbours = neighbours;
    }

    
    
    public VectorView showMaxims(boolean showMaxims)
    {
        this.showMaxims = showMaxims;
        return this;
    }

    public boolean isShowMaxims()
    {
        return showMaxims;
    }
    
    

    
    public void setMaxims(ArrayList<Cntx2> maxims)
    {
        this.maxims = maxims;
        maxmax = 0;
        maxmin = Integer.MAX_VALUE;
        for (Cntx2 p:maxims)
        {
            if (maxmax < p.p)
                maxmax = p.p;
            if (maxmin > p.p)
                maxmin = p.p;
        }
        
        maxmax -= maxmin;
    }

    public VectorView(Image img, ArrayList<Outline> vecs, Greyscale gs, Greyscale gs_grad)
    {
        this.vecs = vecs;
        this.gs = gs;
        this.gs_grad = gs_grad;

        this.imgWidth = img.getWidth(null);
        this.imgHeight = img.getHeight(null);
        this.setFocusable(true);
        
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(imgWidth,2*imgHeight));
        this.img = img;
        this.img2 = gs.createImage(null);
        this.img3 = gs_grad.createImage(null);
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int ctrl = e.getModifiers()&MouseEvent.CTRL_DOWN_MASK;
                System.out.println("ctrl:"+ctrl + ", readEvents:" + readEvents);
                if (!readEvents || ctrl == 0)
                    return;
                
                if (e.getPoint().y > getHeight()/2)
                    return;
                if (e.getButton() == MouseEvent.BUTTON1)
                    showVectors = !showVectors;
                else
                    showMaxims = !showMaxims;
                    
                repaint();
            }
        });
        
        addKeyListener(new KeyAdapter()
        {

            @Override
            public void keyPressed(KeyEvent e)
            {
                System.out.println("keypress");
                switch (e.getKeyCode())
                {
                    case KeyEvent.VK_I: showImg = ! showImg; break;
                    case KeyEvent.VK_V: showMaxims = ! showMaxims; break;
                }
                
                repaint();
            }
            
        });
    }


    void paintVectors(Graphics g)
    {
        if (vecs == null)
            return;
        float z = 0.2f;
        if (vecs == null) return;
        
        
        for (int k=0;k<vecs.size();k++)
        {
            Outline v = vecs.get(k);
            float w = (float)Math.pow(v.getWeight(),0.2);
/*            if (v.mark)
            {
                if (!showMarked)
                    continue;
                
                g.setColor(v.yScan?Color.yellow:Color.red);
            }
            else
                g.setColor(v.yScan?Color.GRAY:Color.DARK_GRAY);
  */          
            int offx = 0;//v.yScan?0:3;
            int offy = 0;

            g.setColor(Color.green);
            
            if (showVectors)
            {
                Point p1 = v.getP(0);

                for (int i=1;i<v.size(); i++)
                {
                    Point p2 = v.getP(i);
                    g.drawLine(p1.x+offx, p1.y+offy, p2.x+offx, p2.y+offy);
                    p1 = p2;
                }
            }
            else
            {
                for (int i=0;i<v.size(); i++)
                {
                    Point p = v.getP(i);

                    g.drawLine(p.x, p.y, p.x, p.y);
                }
            }

/*
            if (!showMaxims)
            {
                float c1 = (float)(Math.random()*0.8 + 0.2);
                float c2 = (float)(Math.random()*0.8 + 0.2);
                float c3 = (float)(Math.random()*0.8 + 0.2);
                g.setColor(new Color(c1,c2,c3));
            }

            for (int i=1;i<v.size(); i++)
            {
                Point p2 = v.getP(i);
                float c = 0;
                
                if (showMaxims)
                {
                    c = 0.3f + 0.7f*((float)i/v.size());
                    g.setColor(new Color(c,c,c));
                }
 
                g.drawLine(p1.x+offx, p1.y+offy, p2.x+offx, p2.y+offy);
                p1 = p2;
            }

 */
        }
    }

    void drawMaxims(Graphics g)
    {
        if (maxims != null)
        {
            g.setColor(Color.yellow);
            for (int i=0;i<maxims.size();i++)
            {
                Cntx p = maxims.get(i);
                
                int r = (int)(20*(p.p-maxmin)/maxmax);
                if (r < 2)
                    r = 2;
                
               // g.drawOval(p[0]-r/2, p[1]-r/2, r, r);
                g.drawLine(p.x, p.y, p.x, p.y);
            }
        }
    }
    
    void drawNeighbours(Graphics g)
    {
        if (neighbours == null) return; 
        
      //  g.setColor(Color.darkGray);
        Color nc = new Color(100,100,100,30);
        for (Cntx2 c:neighbours)
        {
            if (spot != null && Math.abs(c.x-spot.x) + Math.abs(c.y-spot.y) < 10)
                g.setColor(Color.gray);
            else
                g.setColor(nc);
                
            for (Cntx2.Neighbour n:c.neighbours)
            {
               g.drawLine(c.x, c.y, n.c.x, n.c.y);
            }
        }
        
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        
        
        if (showImg)
            g.drawImage(img, 0, 0, null);
        
        g.drawImage(img2, 0, img.getHeight(null), null);
        g.drawImage(img3, img.getWidth(null), 0, null);
        
        
        
        //drawNeighbours(g);

        if (showMaxims)
            drawMaxims(g);
        else
        {
            paintVectors(g);
            
        }

  }
}
