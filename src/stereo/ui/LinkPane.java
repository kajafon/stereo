/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package stereo.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import stereo.Matcher;
import stereo.poi.Cntx;
import stereo.poi.Cntx2;
import stereo.Greyscale;
import stereo.RelationProcessor;
import stereo.poi.ApproachingPerfection;
import stereo.to3d.FtrLink;
import stereo.poi.Feature;
import stereo.poi.Relation;
import stereo.to3d.Face;

/**
 *
 * @author karol presovsky
 */
public class LinkPane extends JPanel
{
    Point poi1;
    Point poi2;
    Point spot;
    int poiViewSize = 50;
    double errScale;
    double minErr;
    ApproachingPerfection thing1;
    ApproachingPerfection thing2;
    ArrayList<FtrLink> links;
    ArrayList<Face> faceList;
    FtrLink currentLink = null;    
    JLabel view1;
    JLabel view2;
    JLabel view3;
    JLabel view4;
    boolean showLinks = true;
    boolean showFaces = true;

    public void setFaceList(ArrayList<Face> faceList)
    {
        this.faceList = faceList;
    }
            
    public LinkPane(ApproachingPerfection thing1, ApproachingPerfection thing2, ArrayList<FtrLink> links)
    {
        setFocusable(true);
        view1 = new JLabel(new ImageIcon(thing1.getImg()));
        view2 = new JLabel(new ImageIcon(thing2.getImg()));
        view3 = new JLabel(new ImageIcon(thing1.getGrad()));
        view4 = new JLabel(new ImageIcon(thing2.getGrad()));
        this.links = links;
        this.thing1 = thing1;
        this.thing2 = thing2;
      
        add(view1);
        add(view2);
        add(view3);
        add(view4);
        
        addMouseMotionListener(new MouseAdapter() 
        {

            @Override
            public void mouseMoved(MouseEvent me)
            {
               spot = me.getPoint();
               
               int x = spot.x - view1.getX();
               int y = spot.y - view1.getY();
               for (FtrLink l:LinkPane.this.links)
               {
                   if (Math.abs(l.f1.x - x) + Math.abs(l.f1.y - y) < 10)
                   {
                       currentLink = l;
                       break;
                   }
               }

               repaint();
            }
        });
        
        addKeyListener(new KeyAdapter() 
        {

            @Override
            public void keyPressed(KeyEvent e)
            {
                System.out.println("key pressed");
                switch(e.getKeyCode())
                {
                    case KeyEvent.VK_S:
                        showLinks = !showLinks;
                        repaint();
                        break;
                    case KeyEvent.VK_E:
                        if (currentLink != null)
                            System.out.println("line err.: " + currentLink.e);
                        break;
                    case KeyEvent.VK_D:
                        debug();
                        break;
                    case KeyEvent.VK_F:
                        showFaces = !showFaces;
                        repaint();
                        break;                        
                }
            }
        
        });
    }
    
    
    void debug()
    {
        if (currentLink != null)
        {
            ApproachingPerfection.compare(currentLink.f1, currentLink.f2);
        }
        
    }
    
    void paintPoi(Graphics g, JComponent view, Greyscale gs, Point poi)
    {
        if (poi != null)
        {
            int x1 = view.getX();
            int y1 = view.getY() + view.getHeight()-poiViewSize;
            int gy = poi.y%(view.getHeight()/2);
          //  int x2 = view1.getWidth()-1;
           // int y2 = view1.getHeight()-1;
            drawSpot(g, gs, x1, y1, poi.x - poiViewSize/2, gy - poiViewSize/2, poiViewSize, poiViewSize);
            g.setColor(Color.red);
            g.drawRect(x1, y1, poiViewSize, poiViewSize);
        }
    }

    
    void drawSpot(Graphics g, Greyscale gs, int sx, int sy, int x, int y, int w, int h)
    {
       // System.out.println("->" + x + ", " + y);
        if (x < 0)
        {
            sx -= x;
            w += x;
            x = 0;
        }
        if (x+w > gs.width)
        {
            int d = x+w - gs.width;
            w -= d;
        }
        if (y < 0)
        {
            sy -= y;
            h += y;
            y = 0;
        }
        if (y+h > gs.height)
        {
            int d = y+h - gs.height;
            h -= d;
        }
        
        if (h <= 0 || w <= 0 || x >= gs.width || y >= gs.height)
            return;
        
        for (int j=0; j<h; j++)
        {
            int adr = (j+y)*gs.width;
            for (int i=0; i<w; i++)
            {
                short c = gs.px[adr + i + x];
                g.setColor(new Color((int)c,(int)c,(int)c));
                g.drawLine(sx+i, sy+j,sx+i, sy+j);
            }
        }
    }
    
    void paintLinks(Graphics g)
    {
        if (spot == null)
        {
            System.out.println("spot == null");
            return;
        }
        
        if (links.isEmpty())
        {
            return;
        }
        int offx1 = view1.getX();
        int offx2 = view2.getX();
        int offy1 = view1.getY();
        int offy2 = view2.getY();

        
        for (FtrLink r : links)
        {
            Feature c1 = r.f1;
            Feature c2 = r.f2;
            
            if (!showLinks)
            {
                int d = Math.abs(spot.x - c1.x) + Math.abs(spot.y - c1.y);
                if ( r != currentLink && d > 10)
                {
                    continue;
                }
                g.setColor(Color.WHITE);
            }
           
            int x1 = offx1 + c1.x;
            int y1 = offy1 + c1.y;
            int x2 = offx2 + c2.x;
            int y2 = offy2 + c2.y;
            g.drawLine(x1, y1, x2, y2);
        }
    }

    void markCurrentLink(Graphics g)
    {
        if (currentLink != null)
        {
            g.setColor(Color.YELLOW);
            int x1 = view1.getX() + currentLink.f1.x;
            int y1 = view1.getY() + currentLink.f1.y;
            int x2 = view2.getX() + currentLink.f2.x;
            int y2 = view2.getY() + currentLink.f2.y;
            
            g.drawRect(x1-2, y1-2, 4, 4);
            g.drawRect(x2-2, y2-2, 4, 4);
            
        }        
    }
    
    void paintMaxims(Graphics g, ArrayList<int[]> maxims, int x0, int y0)
    {
        for (int [] m:maxims)
        {
            g.drawLine(x0 + m[0], y0 + m[1], x0 + m[0], y0 + m[1]);
        }
    }
    
    void paintFaces(Graphics g)
    {
        int offx = view1.getX();
        int offy = view1.getY();
        
        if (faceList == null || !showFaces) return;

        g.setColor(Color.WHITE);
        
        for (Face face : faceList)
        {
            FtrLink l1 = links.get(face.r1);
            FtrLink l2 = links.get(face.r2);
            FtrLink l3 = links.get(face.r3);
        
            g.drawLine(l1.f1.x + offx, l1.f1.y + offy, l2.f1.x + offx, l2.f1.y + offy);
            g.drawLine(l2.f1.x + offx, l2.f1.y + offy, l3.f1.x + offx, l3.f1.y + offy);
            g.drawLine(l1.f1.x + offx, l1.f1.y + offy, l3.f1.x + offx, l3.f1.y + offy);
            
        }
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        
        paintFaces(g);
        g.setColor(new Color(255,255,255,50));
        paintLinks(g);
        markCurrentLink(g);
        paintMaxims(g, thing1.getMaxims(), view1.getX(), view1.getY());
        paintMaxims(g, thing2.getMaxims(), view2.getX(), view2.getY());
    }
    
}
